package com.jule.domino.dispacher.network.protocol;

import com.google.protobuf.GeneratedMessageV3;
import com.jule.domino.dispacher.network.DispacherFunctionFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xujian on 2017/5/16 0016.
 */
public abstract class Req implements Cloneable, Runnable {
    private final static Logger logger = LoggerFactory.getLogger(Req.class);

    protected ChannelHandlerContext ctx;
    @Getter
    protected int functionId;
    @Getter
    protected ReqHeader reqHeader;

    public Req(int functionId) {
        this.functionId = functionId;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public void setReqHeader(ReqHeader reqHeader) {
        this.reqHeader = reqHeader;
    }

    public void setFunctionId(int functionId) {
        this.functionId = functionId;
    }

    public boolean readPayLoad(ByteBuf buf) {
        try {
            readPayLoadImpl(buf);
        } catch (Exception e) {
            logger.error("io error", e);
            return false;
        }
        return true;
    }

    public abstract void readPayLoadImpl(ByteBuf buf) throws Exception;

    @Override
    public void run() {
        try {
            processImpl();
        } catch (Exception e) {
            logger.error("业务处理异常", e);
        }
    }

    public abstract void processImpl() throws Exception;

    public Req clone() {
        try {
            return (Req) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * 消息头
     */
    public static final class ReqHeader {
        public int functionId;
        public int gameId;
        public int gameServerId;
        public boolean isAsync;
        public int reqNum;

        public ReqHeader(int functionId, int gameId, int gameServerId, boolean isAsync, int reqNum) {
            this.functionId = functionId;
            this.gameId = gameId;
            this.gameServerId = gameServerId;
            this.isAsync = isAsync;
            this.reqNum = reqNum;
        }

        @Override
        public String toString() {
            return "functionId:" + functionId + ",gameId:" + gameId + ",gameServerId:" + gameServerId + ",isAsync:" + isAsync +
                    ",reqNum:" + reqNum;
        }
    }

    /**
     * 发送返回消息
     *
     * @param ack
     */
    public void sendAcqMsg(GeneratedMessageV3.Builder ack) {
        try {
            DispacherFunctionFactory.getInstance().
                    getResponse(functionId | 0x08000000, ack.build().toByteArray()).send(ctx, reqHeader);
        } catch (Exception e) {
            logger.error(e.getMessage(), e.fillInStackTrace());
        }
    }

}
