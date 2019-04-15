package com.jule.domino.game.gate.network.protocol;

import com.jule.core.network.ChannelHandler;
import com.jule.domino.game.gate.network.GateFunctionFactory;
import com.jule.domino.game.gate.pool.net.ChannelManageCenter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xujian on 2017/5/16 0016.
 */
public abstract class Req implements Cloneable, Runnable {
    private final static Logger logger = LoggerFactory.getLogger(Req.class);

    protected ChannelHandlerContext ctx;
    protected int functionId;
    protected ReqHeader reqHeader;
    protected String userId;

    public Req(int functionId) {
        this.functionId = functionId;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.userId = ChannelManageCenter.getInstance().getSessionUID(ChannelHandler.getSesseionId(ctx));
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
    @ToString
    public static final class ReqHeader{
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
    }

    /**
     * 返回消息
     */
    protected void sendResponse(int functionId, byte[] bytes){
        GateFunctionFactory.getInstance().getResponse(functionId, bytes).send(ctx, reqHeader);;
    }

    /**
     * 返回消息
     */
    protected void sendResponseAndClose(int functionId, byte[] bytes, boolean isClose){
        GateFunctionFactory.getInstance().getResponse(functionId, bytes).send(ctx, reqHeader);;
    }
}
