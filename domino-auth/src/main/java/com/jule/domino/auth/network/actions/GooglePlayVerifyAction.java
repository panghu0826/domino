package com.jule.domino.auth.network.actions;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.alibaba.fastjson.JSONObject;
import com.jule.domino.auth.config.Config;
import com.jule.domino.auth.dao.DBUtil;
import com.jule.domino.auth.dao.bean.Currency;
import com.jule.domino.auth.dao.bean.Payment;
import com.jule.domino.auth.model.BaseAction;
import com.jule.domino.auth.model.PurchaseData;
import com.jule.domino.auth.model.Response;
import com.jule.domino.auth.service.LogService;
import com.jule.domino.auth.utils.GooglePlayApi;
import com.jule.domino.auth.utils.RSASignature;
import com.jule.domino.auth.utils.ThreadPool;
import com.jule.domino.base.bean.ItemConfigBean;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.service.ItemServer;
import com.jule.domino.log.service.LogReasons;
import com.jule.core.jedis.StoredObjManager;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xujian 2018-01-09
 * 支付验证
 */
public class GooglePlayVerifyAction extends BaseAction {

    public static final String ORDER_CACHE_PREFOX = "pay_order_prefix";

    private static final Logger logger = LoggerFactory.getLogger(GooglePlayVerifyAction.class);

    /**
     * {
     * "kind": "androidpublisher#productPurchase",
     * "purchaseTimeMillis": long,
     * "purchaseState": integer,
     * "consumptionState": integer,
     * "developerPayload": string,
     * "orderId": string,
     * "purchaseType": integer
     * }
     *
     * @param ctx
     * @param payload
     * @param isKeepAlive
     * @throws Exception
     */
    @Override
    public void handlePost(ChannelHandlerContext ctx, byte[] payload, boolean isKeepAlive) throws Exception {

        JoloAuth.JoloCommon_GoogleVerifyReq req = JoloAuth.JoloCommon_GoogleVerifyReq.parseFrom(payload);
        JoloAuth.JoloCommon_GoogleVerifyAck.Builder ackBuild = JoloAuth.JoloCommon_GoogleVerifyAck.newBuilder().setResult(1);
        logger.info("process post request ->" + req.toString());
        String statement = req.getGoogleStatement();//我们的订单号
        String packageName = req.getPackageName();
        String token = req.getPayToken();
        String productId = req.getProductId();
        String inAppPurchaseData = req.getInAppPurchaseData();
        String inAppSignature = req.getInAppSignature();

        /*if (!packageName.equalsIgnoreCase(Config.GOOGLE_APP_PACKAGE)) {
            ctx.writeAndFlush(Response.build(ackBuild.setResult(0).setResultMsg(ErrorCodeEnum.DISPACHER_600004_2.getCode()).build().toByteArray(), isKeepAlive)).addListener(ChannelFutureListener.CLOSE);
            return;
        }*/

        //防止短时间内刷单
        if (StoredObjManager.exists(ORDER_CACHE_PREFOX + statement)) {
            //订单正在处理
            ctx.writeAndFlush(Response.build(ackBuild.setResult(0).setResultMsg(ErrorCodeEnum.DISPACHER_600004_4.getCode()).build().toByteArray(), isKeepAlive)).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        //对订单加锁
        StoredObjManager.setEx(ORDER_CACHE_PREFOX + statement, statement, 30);

        Payment payment = DBUtil.selectByStatement(statement);
        if (payment == null) {
            ctx.writeAndFlush(Response.build(ackBuild.setResult(0).setResultMsg(ErrorCodeEnum.DISPACHER_600004_3.getCode()).build().toByteArray(), isKeepAlive)).addListener(ChannelFutureListener.CLOSE);
            //订单解锁
            StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX + statement);
            return;
        }

        if (payment.getState() == 1 && payment.getSyn_state().equalsIgnoreCase("1")) {
            ctx.writeAndFlush(Response.build(ackBuild.setResult(0).setResultMsg(ErrorCodeEnum.DISPACHER_600004_4.getCode()).build().toByteArray(), isKeepAlive)).addListener(ChannelFutureListener.CLOSE);
            //订单解锁
            StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX + statement);
            return;
        }

        payment.setReserved9(inAppPurchaseData);
        //验证支付结果
        //if (!vilidate_API(packageName,productId,token)) {
        if (!vilidate_RAS(inAppPurchaseData, inAppSignature, statement, payment.getPid())) {
            //保存订单信息
            DBUtil.updateByPrimaryKey(payment);
            ctx.writeAndFlush(Response.build(ackBuild.setResult(0).setResultMsg(ErrorCodeEnum.DISPACHER_600004_7.getCode()).build().toByteArray(), isKeepAlive)).addListener(ChannelFutureListener.CLOSE);
            //订单解锁
            StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX + statement);
            return;
        }

        //成功
        payment.setChannel_statement(statement);
        payment.setState(1);

        //加钱
        User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + payment.getReserved2(), User.class);
        if (user == null) {
            user = DBUtil.selectByPrimaryKey(payment.getReserved2());
        }
        if (user == null) {
            payment.setSyn_state("2");
            DBUtil.updateByPrimaryKey(payment);
            ctx.writeAndFlush(Response.build(ackBuild.setResult(0).setResultMsg(ErrorCodeEnum.DISPACHER_600004_8.getCode()).build().toByteArray(), isKeepAlive)).addListener(ChannelFutureListener.CLOSE);
            //订单解锁
            StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX + statement);
            return;
        }

        double org = user.getMoney();

        payment.setSyn_state("1");
        payment.setReserved3("googleplay");
        DBUtil.updateByPrimaryKey(payment);

        int num = 0;
        if (StringUtils.isNotEmpty(payment.getReserved1())) {
            num = Integer.parseInt(payment.getReserved1());
        }
        if (num > 0 && payment.getReserved5().equals(GameConst.GOODS_GOLD)) {
            user.setMoney(user.getMoney() + num);
            DBUtil.updateByPrimaryKey(user);
            /**保存玩家信息到缓存*/
            logger.info("6save userInfo->" + user.toString());
            StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
            //更新玩家牌桌
            noticeGame(user.getId(), user.getMoney(), Integer.parseInt(payment.getReserved1()));
            ackBuild.setType(1);//充值
            LogService.OBJ.sendMoneyLog(user, org, user.getMoney(), Long.parseLong("" + num), LogReasons.CommonLogReason.PAY_COMPLETED);
            if (DBUtil.isFirstPay(user.getId())) {
                LogService.OBJ.sendUserFirstOrderedCompletedLog(user, payment);
            }
        }
        if (StringUtils.isNotEmpty(payment.getReserved4()) && payment.getReserved5().equals(GameConst.GOODS_ITEM)) {
            int itemTmpId = Integer.parseInt(payment.getReserved4());
            ItemServer.OBJ.addUnit(Config.GAME_ID, payment.getReserved2(), itemTmpId, num, "GooglePlayVerifyAction");
            ackBuild.setItemTmpId(itemTmpId).setUserId(payment.getReserved2()).setType(2);//2购买物品
            try {
                ItemConfigBean bean = ItemServer.OBJ.getTemplateByItemId(Config.GAME_ID, itemTmpId);
                LogService.OBJ.sendItemLog(user, num, bean, LogReasons.CommonLogReason.GAME_BUY_IN);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }


        //发送订单完成日志
        LogService.OBJ.sendUserOrderedCompletedLog(user, payment);

        ctx.writeAndFlush(Response.build(ackBuild.setCurrentMoney(user.getMoney()).build().toByteArray(), isKeepAlive)).addListener(ChannelFutureListener.CLOSE);

        //订单解锁
        StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX + statement);

        //将玩家此操作产生的金额变动记录插入到数据库
        if (Config.CURRENCY && payment.getReserved5().equals(GameConst.GOODS_GOLD)) {
            Currency curr = new Currency();
            curr.setTable_id("");
            curr.setPlayer_id(user.getId());
            curr.setNick_name(user.getNick_name());
            curr.setOperation("top_up");//'sit_down','stand_up','buy_in','giving','top_up','bet','settlement'
            curr.setAmount(Long.parseLong(payment.getReserved1()));//操作的货币量
            curr.setGame_order_id("");//游戏唯一订单号
            curr.setBet(0l);//下注
            curr.setPoundage(0l);//手续费
            curr.setWin_jetton(0l);//赢的筹码
            curr.setLose_jetton(0l);//输的筹码
            curr.setJetton(0l);//玩家目前的筹码
            curr.setMoney(user.getMoney());//玩家目前的货币(总钱数减去筹码数)
            curr.setUniversal("googleplay top-up");
            ThreadPool.pool(curr);//插入操作交给其它线程
        }
    }

    /**
     * 使用googleAPI验证
     *
     * @param packageName
     * @param productId
     * @param token
     * @return
     */
    private boolean vilidate_API(String packageName, String productId, String token) {
        JSONObject ret = GooglePlayApi.verify(packageName, productId, token);
        if (ret == null) {
            return false;
        }

        String developerPayload = ret.getString("developerPayload");

        int purchaseState = ret.getIntValue("purchaseState");//0 成功 1失败
        int purchaseType = ret.getIntValue("purchaseType");//0 是测试沙河账号
        String orderId = purchaseType == 0 ? "" : ret.getString("orderId");
        if (!orderId.equalsIgnoreCase(developerPayload)) {
            return false;
        }

        if (purchaseState != 0) {
            return false;
        }
        return true;
    }

    /**
     * 使用非对称加密校验支付结果
     *
     * @param inAppPurchaseData
     * @param inAppSignature
     * @return
     */
    private boolean vilidate_RAS(String inAppPurchaseData, String inAppSignature, String orderId, String productId) {
        if (StringUtils.isEmpty(inAppPurchaseData)
                || StringUtils.isEmpty(inAppSignature)) {
            return false;
        }
        logger.info(" inAppPurchaseData= " + inAppPurchaseData + ",inAppSignature= " + inAppSignature);

        //解析inAppPurchaseData
        JSONObject jsonObject = JSONObject.parseObject(inAppPurchaseData);
        if (jsonObject == null) {
            return false;
        }
        PurchaseData purchase = JSONObject.parseObject(inAppPurchaseData, PurchaseData.class);
        if (purchase == null) {
            return false;
        }

        //稍作验证
        if (!orderId.equals(purchase.getDeveloperPayload())) {
            return false;
        }
        if (!productId.equals(purchase.getProductId())) {
            return false;
        }
        if (purchase.getPurchaseState() != 0) {
            return false;
        }

        return RSASignature.doCheck(inAppPurchaseData, inAppSignature, Config.GOOGLE_PAY_PUBLIC_KEY);
    }

}
