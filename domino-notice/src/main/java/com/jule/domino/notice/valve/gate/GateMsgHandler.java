package com.jule.domino.notice.valve.gate;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xujian 2012-12-15
 */
public class GateMsgHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final static Logger logger = LoggerFactory.getLogger(GateMsgHandler.class);
    private GateConnectPool gameConnectPool;

    public GateMsgHandler(GateConnectPool gameConnectPool) {
        this.gameConnectPool = gameConnectPool;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if(this.gameConnectPool.canAdd(gameConnectPool.getGateServerId())) {
            this.gameConnectPool.addChannel(gameConnectPool.getGateServerId(),ctx);
        }else {
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.gameConnectPool.delConnect(gameConnectPool.getGateServerId(),ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        int functionId = msg.readInt();
        if (functionId == 1) {
            return;
        }
        logger.info("notice's notice channelRead0,functionId->:"+functionId+" ip"+ctx.channel().remoteAddress());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE || event.state() == IdleState.WRITER_IDLE || event.state() == IdleState.ALL_IDLE) {
                ByteBuf byteBuf = ctx.alloc().buffer(28);
                byteBuf.writeInt(1);
                byteBuf.writeInt(0);
                byteBuf.writeInt(0);
                byteBuf.writeInt(0);
                byteBuf.writeInt(0);
                byteBuf.writeInt(0);
                byteBuf.writeInt(0);
                ctx.writeAndFlush(byteBuf);
                //logger.info("send heartbeat msg to gateway"+ctx.channel().remoteAddress());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.gameConnectPool.delConnect(gameConnectPool.getGateServerId(),ctx);
        ctx.close();
    }
}
