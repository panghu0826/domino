package com.jule.domino.auth.network.actions;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.auth.config.Config;
import com.jule.domino.auth.dao.DBUtil;
import com.jule.domino.auth.dao.bean.Currency;
import com.jule.domino.auth.dao.bean.Payment;
import com.jule.domino.auth.model.BaseAction;
import com.jule.domino.auth.model.Response;
import com.jule.domino.auth.service.LogService;
import com.jule.domino.auth.utils.FaceBookApi;
import com.jule.domino.auth.utils.ThreadPool;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.log.service.LogReasons;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xujian 2018-01-09
 * facebook 支付状态同步回调
 */
public class FaceBookCallbackAction extends BaseAction {
    private static final Logger logger = LoggerFactory.getLogger(FaceBookCallbackAction.class);
    private static final String VERIFY_TOKEN = "dfgdgdfg3453453#$%345";


    @Override
    public void handleGet(ChannelHandlerContext ctx, Map<String, List<String>> parameter, boolean isKeepAlive) throws Exception {
        logger.info("received facebook verify ->" + parameter.toString());
        //{hub.mode=[subscribe], hub.challenge=[1244737911], hub.verify_token=[dfgdgdfg3453453#$%345]}
        String hub_challenge = parameter.get("hub.challenge").get(0);
        String hub_verify_token = parameter.get("hub.verify_token").get(0);

        if (!hub_verify_token.equalsIgnoreCase(VERIFY_TOKEN)) {
            ctx.writeAndFlush(Response.build400()).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        FullHttpResponse ret = Response.build(hub_challenge.getBytes("utf-8"), isKeepAlive);
        logger.info("send ->" + ret.toString());
        ctx.writeAndFlush(ret).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * {"object":"payments","entry":[{"id":"1178546678942295","time":1516681971,"changed_fields":["actions"]}]}
     *
     * @param ctx
     * @param payload
     * @param iskeepAlive
     * @throws Exception
     */
    @Override
    public void handlePost(ChannelHandlerContext ctx, byte[] payload, boolean iskeepAlive) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(new java.lang.String(payload));
        logger.info("received facebook payment ->" + jsonObject.toString());

        String paymentId = (jsonObject.getJSONArray("entry")).getJSONObject(0).getString("id");//facebook payment_id
        String field = (jsonObject.getJSONArray("entry")).getJSONObject(0).getJSONArray("changed_fields").getString(0);//actions

        if (!field.equalsIgnoreCase("actions")) {
            logger.debug("not charge action ignore!");
            ctx.writeAndFlush(Response.build("success".getBytes(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        //防止短时间内刷单
        if (StoredObjManager.exists(ORDER_CACHE_PREFOX+paymentId)){
            //订单正在处理
            ctx.writeAndFlush(Response.build("success".getBytes(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        //对订单加锁
        StoredObjManager.setEx(ORDER_CACHE_PREFOX+paymentId,paymentId,30);

        JSONObject _payment = FaceBookApi.getFaceBookPaymentInfo(paymentId);
        if (_payment == null) {
            //查询失败
            ctx.writeAndFlush(Response.build400()).addListener(ChannelFutureListener.CLOSE);
            //订单解锁
            StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX+paymentId);
            return;
        }
        logger.info("verify facebook payment ->" + _payment.toString());
        String myStatement = _payment.getString("request_id");

        Payment payment = DBUtil.selectByStatement(myStatement);
        if (payment == null) {
            ctx.writeAndFlush(Response.build("success".getBytes(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
            //订单解锁
            StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX+paymentId);
            return;
        }
        logger.info("payment->" + payment.toString());
        //检查是否已经完成充值
        //支付成功发货也成功
        if (payment.getState() == 1 && payment.getSyn_state().equals("1")) {
            logger.info("already payed");
            ctx.writeAndFlush(Response.build("success".getBytes(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
            //订单解锁
            StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX+paymentId);
            return;
        }

        payment.setUpdate_time(new Date());
        payment.setChannel_statement(paymentId);
        payment.setState(isComplete(_payment));

        //支付成功
        if (payment.getState() == 1) {
            User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + payment.getReserved2(), User.class);
            if (user == null) {
                user = DBUtil.selectByPrimaryKey(payment.getReserved2());
            }
            if (user == null) {
                payment.setSyn_state("2");
                ctx.writeAndFlush(Response.build("success".getBytes(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
                //订单解锁
                StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX+paymentId);
                return;
            }

            double before = user.getMoney();
            user.setMoney(user.getMoney() + Integer.parseInt(payment.getReserved1()));

            //将玩家此操作产生的金额变动记录插入到数据库
            if(Config.CURRENCY) {
                Currency curr = new Currency();
                curr.setTable_id("");
                curr.setPlayer_id(user.getId());
                curr.setNick_name(user.getNick_name());
                curr.setOperation("top_up");//'sit_down','stand_up','buy_in','giving','top_up','bet','settlement'
                curr.setAmount(before);//操作的货币量
                curr.setGame_order_id("");//游戏唯一订单号
                curr.setBet(0l);//下注
                curr.setPoundage(0l);//手续费
                curr.setWin_jetton(0l);//赢的筹码
                curr.setLose_jetton(0l);//输的筹码
                curr.setJetton(0l);//玩家目前的筹码
                curr.setMoney(user.getMoney());//玩家目前的货币(总钱数减去筹码数)
                curr.setUniversal("facebook top-up");
                ThreadPool.pool(curr);//插入操作交给其它线程
            }

            if (DBUtil.updateByPrimaryKey(user) == 0) {
                payment.setSyn_state("2");
                ctx.writeAndFlush(Response.build("success".getBytes(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
                //订单解锁
                StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX+paymentId);
                return;
            }

            payment.setSyn_state("1");
            payment.setReserved3("facebook");
            DBUtil.updateByPrimaryKey(payment);

            /**保存玩家信息到缓存*/
            StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
            logger.info("7save userInfo->" + user.toString());

            //发送订单完成日志
            LogService.OBJ.sendUserOrderedCompletedLog(user, payment);
            LogService.OBJ.sendMoneyLog(user,before,user.getMoney(),Long.parseLong(payment.getReserved1()), LogReasons.CommonLogReason.PAY_COMPLETED);
            if (DBUtil.isFirstPay(user.getId())) {
                LogService.OBJ.sendUserFirstOrderedCompletedLog(user, payment);
            }

            //更新玩家牌桌
            noticeGame(user.getId(), user.getMoney(), Integer.parseInt(payment.getReserved1()));

        } else if (payment.getState() == 2) {
            DBUtil.updateByPrimaryKey(payment);
        }
        ctx.writeAndFlush(Response.build("success".getBytes(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);

        //订单解锁
        StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX+paymentId);
    }

    /**
     * 检查是否支付成功
     * 1 成功 2 失败 0 未支付
     *
     * @param jsonObject
     * @return
     */
    private static int isComplete(JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONArray("actions");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            if (jsonObject1.getString("type").equalsIgnoreCase("charge")) {
                if (jsonObject1.getString("status").equalsIgnoreCase("completed")) {
                    return 1;
                } else if (jsonObject1.getString("status").equalsIgnoreCase("failed")) {
                    return 2;
                } else if (jsonObject1.getString("status").equalsIgnoreCase("initiated ")) {
                    return 0;
                }
            }
        }
        return 0;
    }

    /**
     *
     */
    public static void main(String[] args) {
        JSONObject jsonObject = JSONObject.parseObject("{\"object\":\"payments\",\"entry\":[{\"id\":\"1178546678942295\",\"time\":1516681971,\"changed_fields\":[\"actions\"]}]}");
        String paymentId = (jsonObject.getJSONArray("entry")).getJSONObject(0).getString("id");//facebook payment_id
        String field = (jsonObject.getJSONArray("entry")).getJSONObject(0).getString("changed_fields");//actions
        System.out.println(paymentId);
        System.out.println(field);

    }
}
