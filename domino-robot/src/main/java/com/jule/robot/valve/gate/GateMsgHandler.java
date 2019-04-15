package com.jule.robot.valve.gate;

import com.jule.robot.network.protocol.Req;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
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
        this.gameConnectPool.addChannel(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.gameConnectPool.delConnect(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        int functionId = msg.readInt();
        int gameId = msg.readInt();
        int gameSvrId = msg.readInt();
        boolean isAsync = msg.readInt() == 1;
        int reqNum = msg.readInt();
        long targetChannelId = msg.readLong();
        byte[] playLoad = new byte[msg.readableBytes()];
        msg.readBytes(playLoad);
        Req.ReqHeader header = new Req.ReqHeader(functionId, gameId, gameSvrId, isAsync, reqNum);

        ChannelHandlerContext channelHandlerContext = ClientConnectManager.getInstance().getConnect(targetChannelId);
        if (channelHandlerContext == null || !channelHandlerContext.channel().isActive()) {
            logger.debug("客户端连接已经关闭");
            return;
        }

        int funcId = header.functionId & 0x00FFFFFF;
        funcId = funcId / 10000;
        funcId = funcId | 0xff000000;

//        Ack ack = FunctionFactory.getInstance().getResponse(funcId, playLoad);
//        if (ack != null) {
//            ack.setFunctionId(header.functionId);
//            ack.send(channelHandlerContext, header);
//        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            ByteBuf byteBuf = ctx.alloc().buffer(1024);
            byteBuf.writeInt(1);
            byteBuf.writeInt(0);
            byteBuf.writeInt(0);
            byteBuf.writeInt(0);
            byteBuf.writeInt(0);
            byteBuf.writeInt(0);
            byteBuf.writeInt(0);
            if (event.state() == IdleState.READER_IDLE) {
                logger.debug(ctx.toString() + " read idle close connection");
                ctx.writeAndFlush(byteBuf);
            } else if (event.state() == IdleState.WRITER_IDLE) {
                logger.debug(ctx.toString() + " write idle");
                ctx.writeAndFlush(byteBuf);
            } else if (event.state() == IdleState.ALL_IDLE) {
                logger.debug(ctx.toString() + " all idle");
                ctx.writeAndFlush(byteBuf);
            }
        }
    }
}
