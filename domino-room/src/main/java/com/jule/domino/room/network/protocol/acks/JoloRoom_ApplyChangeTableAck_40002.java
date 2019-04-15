package com.jule.domino.room.network.protocol.acks;

import com.google.protobuf.MessageLite;
import com.jule.domino.room.network.ServerOps;
import com.jule.domino.room.network.protocol.ClientAck;
import com.jule.domino.room.network.protocol.ClientHeader;

/**
 * 申请换桌
 */
public class JoloRoom_ApplyChangeTableAck_40002 extends ClientAck {
    /**
     * @param messageLite
     * @param header
     */
    public JoloRoom_ApplyChangeTableAck_40002(MessageLite messageLite, ClientHeader header) {
        super(messageLite, header);
        setFunctionId(ServerOps.getOpCode(this.getClass()));
    }
}
