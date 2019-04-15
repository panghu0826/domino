package com.jule.domino.gate.vavle.room;

import com.jule.domino.gate.network.GateFunctionFactory;
import com.jule.domino.gate.network.protocol.Ack;
import com.jule.domino.gate.network.protocol.Req;
import com.jule.domino.gate.vavle.net.ChannelManageCenter;
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
public class RoomMsgHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final static Logger logger = LoggerFactory.getLogger(RoomMsgHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if(RoomConnectPool.CHANNELS.size() >= RoomConnectPool.size){
            ctx.close();
            return;
        }
        RoomConnectPool.CHANNELS.add(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        RoomConnectPool.CHANNELS.remove(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        int functionId = msg.readInt();
        if ((functionId & 0x00FFFFFF) == 1) {
            //心跳
            return;
        }
        int gameId = msg.readInt();
        int gameSvrId = msg.readInt();
        boolean isAsync = msg.readInt() == 1;
        int reqNum = msg.readInt();
        long targetChannelId = msg.readLong();
        byte[] playLoad = new byte[msg.readableBytes()];
        msg.readBytes(playLoad);
        Req.ReqHeader header = new Req.ReqHeader(functionId, gameId, gameSvrId, isAsync, reqNum);

        ChannelHandlerContext channelHandlerContext = ChannelManageCenter.getInstance().getChannel(targetChannelId);
        if (channelHandlerContext == null || !channelHandlerContext.channel().isActive()) {
            logger.debug("client link closed");
            return;
        }

        int funcId = header.functionId & 0x00FFFFFF;
        funcId = funcId / 10000;
        funcId = funcId | 0x08000000;

        Ack ack = GateFunctionFactory.getInstance().getResponse(funcId, playLoad);
        if (ack != null) {
            ack.setFunctionId(header.functionId);
            ack.send(channelHandlerContext, header);
        }
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
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        RoomConnectPool.CHANNELS.remove(ctx);
        ctx.close();
    }
}
