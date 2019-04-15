package com.jule.robot.network.protocol;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
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
            buffer.writeBytes(body.toByteArray());

            if (body != null) {
                logger.debug("发送数据 " + functionId + " ->" + body.toString());
            }
            return true;
        } catch (Exception e) {
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
