package com.jule.domino.dispacher.network.protocol;

import io.netty.buffer.ByteBuf;
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

        try {
            ByteBuf byteBuf = channelHandlerContext.alloc().buffer(1024);
            byteBuf.writeInt(28 + buf.length);
            byteBuf.writeInt(functionId);
            byteBuf.writeInt(reqHeader.gameId);
            byteBuf.writeInt(reqHeader.gameServerId);
            byteBuf.writeInt(reqHeader.isAsync ? 1 : 0);
            byteBuf.writeInt(reqHeader.reqNum);
            byteBuf.writeInt(0);
            byteBuf.writeInt(0);
            byteBuf.writeBytes(buf);
            BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame(byteBuf);
            channelHandlerContext.writeAndFlush(binaryWebSocketFrame);
        } catch (Exception e) {
        }

        if (buf != null) {
            logger.debug(String.format("send function-> 0x%02X(%d)", functionId, functionId & 0x00FFFFFF));
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
