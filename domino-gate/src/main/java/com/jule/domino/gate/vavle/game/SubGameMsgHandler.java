package com.jule.domino.gate.vavle.game;

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
 * @author xujian 2012-12-15
 */
public class SubGameMsgHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final static Logger logger = LoggerFactory.getLogger(SubGameMsgHandler.class);
    private SubGameConnectPool gameConnectPool;

    public SubGameMsgHandler(SubGameConnectPool gameConnectPool) {
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

        if((0x08000000 | 1) == header.functionId){
            //心跳不用转发
            return;
        }
        int funcId = header.functionId & 0x00FFFFFF;
        funcId = funcId / 10000;
        funcId = funcId | 0x08000000;

        ChannelHandlerContext channelHandlerContext = ChannelManageCenter.getInstance().getChannel(targetChannelId);
        if (channelHandlerContext == null || !channelHandlerContext.channel().isActive()) {
            logger.debug("Client Link Closed!");
            return;
        }

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
        ctx.close();
    }
}
