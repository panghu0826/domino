package com.jule.domino.auth.network.actions;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.domino.auth.model.BaseAction;
import com.jule.domino.auth.model.Response;
import com.jule.domino.auth.service.ProductionService;
import com.jule.domino.base.enums.GameConst;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author xujian 2018-01-09
 * 获取货币列表
 */
public class ProductAction extends BaseAction {
    private static final Logger logger = LoggerFactory.getLogger(ProductAction.class);

    @Override
    public void handlePost(ChannelHandlerContext ctx, byte[] payload, boolean iskeepAlive) throws Exception {

        JoloAuth.JoLoCommon_ProductReq req = JoloAuth.JoLoCommon_ProductReq.parseFrom(payload);
        logger.info("received ->" + req.toString());
        Map<String, JoloAuth.JoLoCommon_ProtocInfo> list = ProductionService.getInstance().getProducts(req.getAppId(), GameConst.GOODS_GOLD);
        JoloAuth.JoLoCommon_ProductAck.Builder builder = JoloAuth.JoLoCommon_ProductAck.newBuilder();
        if (list == null) {
            builder.setAppId(req.getAppId());
            builder.setResult(0);
            builder.setResultMsg("app shop config not exist");
            ctx.writeAndFlush(Response.build(builder.build().toByteArray(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        builder.setAppId(req.getAppId());
        builder.setResult(1);
        builder.addAllProductInfos(list.values());
        logger.info("send ->" + builder.toString());
        ctx.writeAndFlush(Response.build(builder.build().toByteArray(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
    }

}
