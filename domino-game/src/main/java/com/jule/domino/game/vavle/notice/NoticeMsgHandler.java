package com.jule.domino.game.vavle.notice;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xujian
 */
public class NoticeMsgHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final static Logger logger = LoggerFactory.getLogger(NoticeMsgHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (!NoticeConnectPool.getInstance().onChannelActive(ctx)) {
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("game's notice inactive");
        NoticeConnectPool.getInstance().onChannelInActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        int functionId = msg.readInt();
        logger.info("game's notice channelRead0,functionId->:"+functionId);
        if (functionId == 1) {
            return;
        }
//        int functionId = msg.readInt();
//        int gameId = msg.readInt();
//        int gameSvrId = msg.readInt();
//        boolean isAsync = msg.readInt() == 1;
//        int reqNum = msg.readInt();
//        long targetChannelId = msg.readLong();
//        byte[] playLoad = new byte[msg.readableBytes()];
//        msg.readBytes(playLoad);
//        Req.ReqHeader header = new Req.ReqHeader(functionId, gameId, gameSvrId, isAsync, reqNum);
//
//        ChannelHandlerContext channelHandlerContext = ClientConnectManager.getInstance().getConnect(targetChannelId);
//        if (channelHandlerContext == null || !channelHandlerContext.channel().isActive()) {
//            logger.debug("客户端连接已经关闭");
//            return;
//        }
//
//        int funcId = header.functionId & 0x00FFFFFF;
//        funcId = funcId / 10000;
//        funcId = funcId | 0xff000000;
//
//        Ack ack = GateFunctionFactory.getInstance().getResponse(funcId, playLoad);
//        if (ack != null) {
//            ack.setFunctionId(header.functionId);
//            ack.send(channelHandlerContext, header);
//        }
    }

    /**
     * 心跳 op=1 内容为空
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
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
                byteBuf.writeLong(0);
                ctx.writeAndFlush(byteBuf);
                //logger.info("send heartbeat msg to notice"+ctx.channel().remoteAddress());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        NoticeConnectPool.getInstance().onChannelInActive(ctx);
        ctx.close();
    }
}
