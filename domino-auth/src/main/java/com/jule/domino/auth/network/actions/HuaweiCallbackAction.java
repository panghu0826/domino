package com.jule.domino.auth.network.actions;

import com.alibaba.fastjson.JSON;
import com.jule.domino.auth.config.Config;
import com.jule.domino.auth.dao.DBUtil;
import com.jule.domino.auth.dao.bean.Payment;
import com.jule.domino.auth.model.BaseAction;
import com.jule.domino.auth.model.Response;
import com.jule.domino.auth.service.LogService;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.auth.utils.RSA;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.log.service.LogReasons;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * 华为支付状态同步回调
 *
 * @author
 *
 * @since 2018年7月23日11:20:59
 *
 */
public class HuaweiCallbackAction extends BaseAction {
    private static final String CHARSET = "utf-8";

    private static final String LINK = "&";

    private static final String EQUAL = "=";

    private static final String FLAG = "<=>";


    @Override
    public void handleGet( ChannelHandlerContext ctx, Map<String, List<String>> parameter, boolean isKeepAlive) throws Exception {
        logger.info("华为支付测试");
    }

    @Override
    public void handlePost(ChannelHandlerContext ctx, byte[] payload, boolean iskeepAlive) throws Exception {
        try {
            String paramStr = new String(payload);
            if (paramStr == null){
                logger.error("received huawei post json == null");
                return;
            }

            logger.info("received huawei post request ->" + paramStr);
            Map<String , Object> map = getParamsMap(paramStr);
            RequestParams params = getParams(map);

            dohandler(ctx , params, map, iskeepAlive);
        }catch (Exception e){
            logger.error("华为支付回调处理异常{}", e);
        }
    }

    private void dohandler(ChannelHandlerContext ctx, RequestParams params, Map<String , Object> map , boolean iskeepAlive){
        logger.info("hander http request");
        ResultDomain result = new ResultDomain();
        try {
            String paymentId = params.getOrderId();
            //防止短时间内刷单
            if (StoredObjManager.exists(ORDER_CACHE_PREFOX+paymentId)){
                //订单正在处理
                ctx.writeAndFlush(Response.build(convertJsonStyle(result).getBytes(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
                return;
            }
            //对订单加锁
            StoredObjManager.setEx(ORDER_CACHE_PREFOX+paymentId,paymentId,30);

            if (!"0".equals(params.getResult())){
                logger.error("订单未支付成功 result = {}",params.getResult());
                result.setResult(3);
                ctx.writeAndFlush(Response.build(convertJsonStyle(result).getBytes(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
                //订单解锁
                StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX+paymentId);
                return;
            }

            if (!RSA.rsaDoCheck(map, params.getSign(), Config.HUAWEI_PAY_PUBLIC_KEY, params.getSignType())) {
                logger.info("订单验证失败");
                //查询失败
                ctx.writeAndFlush(Response.build(convertJsonStyle(result).getBytes(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
                //订单解锁
                StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX+paymentId);
                return;
            }
            String myStatement = params.getRequestId();

            Payment payment = DBUtil.selectByStatement(myStatement);
            if (payment == null) {
                result.setResult(3);
                ctx.writeAndFlush(Response.build(convertJsonStyle(result).getBytes(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
                //订单解锁
                StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX+paymentId);
                return;
            }
            logger.info("payment->" + payment.toString());
            //检查是否已经完成充值
            //支付成功发货也成功
            if (payment.getState() == 1 && payment.getSyn_state().equals("1")) {
                logger.info("already payed");
                result.setResult(0);
                ctx.writeAndFlush(Response.build(convertJsonStyle(result).getBytes(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
                //订单解锁
                StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX+paymentId);
                return;
            }

            payment.setUpdate_time(new Date());
            payment.setChannel_statement(paymentId);
            payment.setState(1);

            //支付成功
            if (payment.getState() == 0) {
                User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + payment.getReserved2(), User.class);
                if (user == null) {
                    user = DBUtil.selectByPrimaryKey(payment.getReserved2());
                }
                if (user == null) {
                    payment.setSyn_state("2");
                    DBUtil.updateByPrimaryKey(payment);

                    result.setResult(0);
                    ctx.writeAndFlush(Response.build(convertJsonStyle(result).getBytes(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
                    //订单解锁
                    StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX+paymentId);
                    return;
                }

                double before = user.getMoney();
                user.setMoney(user.getMoney() + Integer.parseInt(payment.getReserved1()));

                if (DBUtil.updateByPrimaryKey(user) == 0) {
                    payment.setSyn_state("2");
                    DBUtil.updateByPrimaryKey(payment);
                    ctx.writeAndFlush(Response.build(convertJsonStyle(result).getBytes(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
                    //订单解锁
                    StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX+paymentId);
                    return;
                }

                payment.setSyn_state("1");
                payment.setReserved3("huawei");
                DBUtil.updateByPrimaryKey(payment);

                /**保存玩家信息到缓存*/
                StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
                logger.info("save userInfo->" + user.toString());

                //发送订单完成日志
                LogService.OBJ.sendUserOrderedCompletedLog(user, payment);
                LogService.OBJ.sendMoneyLog(user,before,user.getMoney(),Long.parseLong(payment.getReserved1()), LogReasons.CommonLogReason.PAY_COMPLETED);
                if (DBUtil.isFirstPay(user.getId())) {
                    LogService.OBJ.sendUserFirstOrderedCompletedLog(user, payment);
                }

                //更新玩家牌桌
                noticeGame(user.getId(), user.getMoney(), Integer.parseInt(payment.getReserved1()));
                result.setResult(0);
            } else {
                DBUtil.updateByPrimaryKey(payment);
            }
            ctx.writeAndFlush(Response.build(convertJsonStyle(result).getBytes(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
            //订单解锁
            StoredObjManager.deleteExistsObj(ORDER_CACHE_PREFOX+paymentId);
        }catch (Exception e){
            logger.error("exception -> {}", e);
            result.setResult(94);
            ctx.writeAndFlush(Response.build(convertJsonStyle(result).getBytes(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private String convertJsonStyle(Object resultMessage){
        ObjectMapper mapper = new ObjectMapper();
        Writer writer = new StringWriter();
        try {
            if ( null != resultMessage) {
                mapper.writeValue(writer, resultMessage);
            }
        } catch (Exception e) {
            logger.error("转换json异常"+e.getMessage());
        }
        return writer.toString();
    }

    /**
     * 华为回调参数
     */
    private static Map<String,Object> getParamsMap(String paramStr){
        try {
            if (StringUtils.isEmpty(paramStr)){
                return null;
            }

            String[] paramStrs = paramStr.split(LINK);
            if (paramStrs == null){
                return null;
            }

            Map<String,Object> map = new HashMap<>();
            for (String param : paramStrs){
                if (!param.contains(EQUAL)){
                    continue;
                }

                String tmp = param.replaceFirst(EQUAL,FLAG);
                String[] p = tmp.split(FLAG);
                if (p.length < 2){
                    continue;
                }

                if ("sign".equals(p[0]) && null != p[1]){
                    map.put(p[0], URLDecoder.decode(p[1], CHARSET));
                    continue;
                }
                if ("extReserved".equals(p[0]) && null != p[1]){
                    map.put(p[0], URLDecoder.decode(p[1], CHARSET));
                    continue;
                }
                if ("sysReserved".equals(p[0]) && null != p[1]){
                    map.put(p[0], URLDecoder.decode(p[1], CHARSET));
                    continue;
                }
                map.put(p[0],p[1]);
            }
            return map;
        }catch (Exception e){
            return null;
        }
    }

    private static RequestParams getParams(Map<String,Object> map){
        String str = JSON.toJSONString(map);
        RequestParams requestParams = JSON.parseObject(str,RequestParams.class);
        return requestParams;
    }

}


class RequestParams{
    //返回结果“0”，表示支付成功
    private String result ;
    //开发者社区用户名或联盟用户编号
    private String userName ;
    //商品名称
    private String productName;
    //支付类型
    private int payType ;
    //商品支付金额
    private String amount ;
    //国标货币
    private String currency ;
    //华为订单号
    private String orderId ;
    //通知时间 unix时间戳毫秒
    private String notifyTime ;
    //开发者支付请求ID
    private String requestId ;
    //银行编码-支付通道信息
    private String bankId ;
    //下单时间 yyyy-MM-dd hh:mm:ss
    private String orderTime ;
    //交易/退款时间 yyyy-MM-dd hh:mm:ss
    private String tradeTime ;
    //接入方式：仅在sdk中指定了urlver为2时有效。
    //0: 移动
    //1: PC-Web
    //2: Mobile-Web
    //3: TV
    private String accessMode ;
    //渠道开销
    private String spending ;
    //商户侧保留信息
    private String extReserved ;
    //商户侧保留信息
    private String sysReserved ;
    //签名类型，不参与签名，默认值为RSA256
    private String signType ;
    //RSA签名
    private String sign ;

    public String getResult() {
        return result;
    }

    public void setResult( String result ) {
        this.result = result;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName( String userName ) {
        this.userName = userName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName( String productName ) {
        this.productName = productName;
    }

    public int getPayType() {
        return payType;
    }

    public void setPayType( int payType ) {
        this.payType = payType;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount( String amount ) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency( String currency ) {
        this.currency = currency;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId( String orderId ) {
        this.orderId = orderId;
    }

    public String getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime( String notifyTime ) {
        this.notifyTime = notifyTime;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId( String requestId ) {
        this.requestId = requestId;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId( String bankId ) {
        this.bankId = bankId;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime( String orderTime ) {
        this.orderTime = orderTime;
    }

    public String getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime( String tradeTime ) {
        this.tradeTime = tradeTime;
    }

    public String getAccessMode() {
        return accessMode;
    }

    public void setAccessMode( String accessMode ) {
        this.accessMode = accessMode;
    }

    public String getSpending() {
        return spending;
    }

    public void setSpending( String spending ) {
        this.spending = spending;
    }

    public String getExtReserved() {
        return extReserved;
    }

    public void setExtReserved( String extReserved ) {
        this.extReserved = extReserved;
    }

    public String getSysReserved() {
        return sysReserved;
    }

    public void setSysReserved( String sysReserved ) {
        this.sysReserved = sysReserved;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType( String signType ) {
        this.signType = signType;
    }

    public String getSign() {
        return sign;
    }

    public void setSign( String sign ) {
        this.sign = sign;
    }
}

class ResultDomain implements Serializable{
    //结果
    private int result = 1;

    public int getResult() {
        return result;
    }

    public void setResult( int result ) {
        this.result = result;
    }
}
