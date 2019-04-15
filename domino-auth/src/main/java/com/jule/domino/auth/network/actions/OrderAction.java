package com.jule.domino.auth.network.actions;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.auth.dao.DBUtil;
import com.jule.domino.auth.dao.bean.Payment;
import com.jule.domino.auth.model.BaseAction;
import com.jule.domino.auth.model.Response;
import com.jule.domino.auth.service.LogService;
import com.jule.domino.auth.service.ProductionService;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * @author xujian 2018-01-09
 * 获取货币列表
 */
public class OrderAction extends BaseAction {
    private static final Logger logger = LoggerFactory.getLogger(OrderAction.class);

    @Override
    public void handlePost(ChannelHandlerContext ctx, byte[] payload, boolean iskeepAlive) throws Exception {
        JoloAuth.JoloCommon_OrderReq req = JoloAuth.JoloCommon_OrderReq.parseFrom(payload);
        logger.info("received ->" + req.toString());
        Map<String, JoloAuth.JoLoCommon_ProtocInfo> map = ProductionService.getInstance().getProducts(req.getAppId());
        JoloAuth.JoLoCmmon_OrderAck.Builder builder = JoloAuth.JoLoCmmon_OrderAck.newBuilder();
        if (map == null) {
            builder.setResult(0);
            builder.setResultMsg("app_id config not exist");
            ctx.writeAndFlush(Response.build(builder.build().toByteArray(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        JoloAuth.JoLoCommon_ProtocInfo protocInfo = map.get(req.getPid());
        if (protocInfo == null) {
            builder.setResult(0);
            builder.setResultMsg("pid not exist");
            logger.error("pid not exist pid:{}", req.getPid());
            ctx.writeAndFlush(Response.build(builder.build().toByteArray(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        String uuid = UUID.randomUUID().toString().replace("-", "");
        // 订单入库
        Payment payment = new Payment();
        payment.setApp_id(req.getAppId());
        payment.setChannel(protocInfo.getPayChannel());
        payment.setChannel_statement("");
        payment.setCreate_time(new Date());
        payment.setPid(req.getPid());
        payment.setPrice(protocInfo.getPrice());
        payment.setState(0);
        payment.setSub_state("");
        payment.setSyn_state("");
        payment.setStatement(uuid);
        if (protocInfo.getTotalReward() > 0) {
            payment.setReserved1(protocInfo.getTotalReward() + "");
        }
        payment.setReserved5(protocInfo.getType());
        payment.setReserved2(req.getUserId());
        if (protocInfo.getItemTmpId() > 0) {
            payment.setReserved4("" + protocInfo.getItemTmpId());
        }
        if (DBUtil.insertPayment(payment) != 1) {
            builder.setResult(0);
            builder.setResultMsg("db error");
            ctx.writeAndFlush(Response.build(builder.build().toByteArray(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        builder.setResult(1);
        builder.setStatement(uuid);
        logger.info("send ->" + builder.toString());
        //发送下单日志
        User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + req.getUserId(), User.class);
        if (user == null) {
            user = DBUtil.selectByPrimaryKey(req.getUserId());
        }


        LogService.OBJ.sendUserOrderedLog(user, payment);

        ctx.writeAndFlush(Response.build(builder.build().toByteArray(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
    }

}
