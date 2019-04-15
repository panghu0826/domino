package com.jule.domino.dispacher.network.handler;

import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.service.UserService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xujian 2117-12-15
 */
@Slf4j
public class DispacherHandler extends SimpleChannelInboundHandler<Req> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        UserService.getInstance().onUserBreak(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //log.info("exceptionCaught()");
        UserService.getInstance().onUserBreak(ctx);
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Req msg) {
        if (msg != null) {
            msg.run();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            ctx.close();
        }
    }
}
