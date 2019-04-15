package com.jule.domino.room.network.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xujian on 2017/12/07.
 */
public abstract class ClientReq implements Cloneable, Runnable {
    private final static Logger logger = LoggerFactory.getLogger(ClientReq.class);

    protected ChannelHandlerContext ctx;
    protected int functionId;
    protected ClientHeader header;

    public ClientReq(int functionId) {
        this.functionId = functionId;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public boolean readPayLoad(ByteBuf byteBuf) {
        try {
            int gameId = byteBuf.readInt();
            int gameSvrId = byteBuf.readInt();
            boolean isAsync = byteBuf.readInt() == 1;
            int reqNum = byteBuf.readInt();
            long channelId = byteBuf.readLong();
            this.header = new ClientHeader(functionId, gameId, gameSvrId, isAsync, reqNum, channelId);
            readPayLoadImpl(byteBuf);
        } catch (Exception e) {
            logger.error("unpack packet error", e);
            return false;
        }

        return true;
    }

    public abstract void readPayLoadImpl(ByteBuf byteBuf) throws Exception;

    @Override
    public void run() {
        try {
            processImpl();
        } catch (Exception e) {
            logger.error("业务处理异常", e);
        }
    }

    public abstract void processImpl() throws Exception;

    public ClientReq clone() {
        try {
            return (ClientReq) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public int getFunctionId() {
        return functionId;
    }
}
