package com.jule.domino.game.gate.network.process.reqs;

import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.gate.pool.room.RoomConnectPool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class RoomProxyReq extends Req {
    private final static Logger logger = LoggerFactory.getLogger(RoomProxyReq.class);
    public RoomProxyReq(int functionId) {
        super(functionId);
    }

    private byte[] bytes;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
    }

    @Override
    public void processImpl() throws Exception {

        ChannelHandlerContext context = RoomConnectPool.getConnection();
        if (context != null) {
            //临时写到预留字段里面
            ByteBuf byteBuf = context.alloc().buffer(bytes.length + 28);
            byteBuf.writeInt(reqHeader.functionId);
            byteBuf.writeInt(reqHeader.gameId);
            byteBuf.writeInt(reqHeader.gameServerId);
            byteBuf.writeInt(reqHeader.isAsync ? 1 : 0);
            byteBuf.writeInt(reqHeader.reqNum);
            byteBuf.writeLong(Long.valueOf(ctx.channel().id().toString(), 16));
            byteBuf.writeBytes(bytes);
            context.writeAndFlush(byteBuf);
        }else {
            logger.info("ChannelHandlerContext is null");
        }
    }
}
