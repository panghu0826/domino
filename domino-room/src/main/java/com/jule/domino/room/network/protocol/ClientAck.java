package com.jule.domino.room.network.protocol;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xujian on 2017/5/16 0016.
 */
public class ClientAck {

    private final static Logger logger = LoggerFactory.getLogger(ClientAck.class);
    protected int functionId;
    protected final ClientHeader header;
    protected MessageLite body;

    public boolean encode(final ByteBuf buffer) {
        try {
            buffer.writeInt(functionId);
            buffer.writeInt(header.gameId);
            buffer.writeInt(header.gameServerId);
            buffer.writeInt(header.isAsync ? 1 : 0);
            buffer.writeInt(header.reqNum);
            buffer.writeLong(header.channelId);

            if (body != null) {
                buffer.writeBytes(body.toByteArray());
                logger.debug("send functionId -> " + (functionId & 0x00FFFFFF) + " body->" + body.toString());
            }
            return true;
        } catch (Exception e) {
            logger.error("encode",e);
            ReferenceCountUtil.release(buffer);
            return false;
        }
    }

    /**
     * @param messageLite
     */
    public ClientAck(MessageLite messageLite, ClientHeader header) {
        this.body = messageLite;
        this.header = header;
    }

    public void setFunctionId(int functionId) {
        this.functionId = functionId;
    }
}
