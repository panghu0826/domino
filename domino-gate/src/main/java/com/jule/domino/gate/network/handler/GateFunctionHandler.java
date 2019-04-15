package com.jule.domino.gate.network.handler;

import com.jule.domino.gate.network.protocol.Req;
import com.jule.domino.gate.service.UserService;
import com.jule.domino.gate.vavle.net.ChannelManageCenter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xujian 2117-12-15
 */
@Slf4j
public class GateFunctionHandler extends SimpleChannelInboundHandler<Req> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //ClientConnectManager.getInstance().addConnect(ctx);
        ChannelManageCenter.getInstance().addChannel(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //ClientConnectManager.getInstance().delConnect(ctx);
        ChannelManageCenter.getInstance().removeTempSession(ctx);
        UserService.getInstance().onUserBreak(ctx,false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Req msg) {
        msg.run();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //ClientConnectManager.getInstance().delConnect(ctx);
        ChannelManageCenter.getInstance().removeTempSession(ctx);
        UserService.getInstance().onUserBreak(ctx,true);
    }
}
