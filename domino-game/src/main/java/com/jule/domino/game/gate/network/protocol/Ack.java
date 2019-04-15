package com.jule.domino.game.gate.network.protocol;

import com.jule.core.network.msg.GateCoder;
import com.jule.core.network.msg.MessagePack;
import com.jule.domino.game.config.Config;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xujian on 2017/5/16 0016.
 */
public class Ack implements Cloneable {

    private final static Logger logger = LoggerFactory.getLogger(Ack.class);
    protected int functionId;
    protected Req.ReqHeader reqHeader;
    protected byte[] buf;

    public void send(final ChannelHandlerContext channelHandlerContext, Req.ReqHeader reqHeader) {
        send(channelHandlerContext, reqHeader, false);
    }

    public void send(final ChannelHandlerContext ctx, Req.ReqHeader reqHeader, boolean isclose) {
        try {
            MessagePack pack = new MessagePack(functionId, reqHeader.gameId, reqHeader.gameServerId, reqHeader.isAsync, reqHeader.reqNum,0, buf);
            ByteBuf byteBuf = GateCoder.OBJ.encode(ctx, pack, Config.GATE_MSG_REVERSAL);

            BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame(byteBuf);
            if (isclose) {
                ctx.writeAndFlush(binaryWebSocketFrame).addListener(ChannelFutureListener.CLOSE);
            } else {
                ctx.writeAndFlush(binaryWebSocketFrame);
            }
        } catch (Exception e) {
            logger.error("", e);
        }

        if (buf != null) {
            logger.debug(String.format("send-> 0x%02X %d %s", functionId, functionId,functionId| 0x08000000));
        }
    }

    public void setBuf(byte[] buf) {
        this.buf = buf;
    }

    /**
     * @param functionId
     */
    public Ack(int functionId) {
        this.functionId = functionId;
    }

    /**
     * @param reqHeader
     */
    public void setReqHeader(Req.ReqHeader reqHeader) {
        this.reqHeader = reqHeader;
    }

    public void setFunctionId(int functionId) {
        this.functionId = functionId;
    }

    /**
     * @return
     */
    public Ack clone() {
        try {
            return (Ack) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
