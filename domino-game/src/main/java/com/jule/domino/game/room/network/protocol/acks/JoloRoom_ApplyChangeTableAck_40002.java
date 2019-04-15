package com.jule.domino.game.room.network.protocol.acks;

import com.google.protobuf.MessageLite;
import com.jule.domino.game.network.protocol.ClientAck;
import com.jule.domino.game.network.protocol.ClientHeader;
import com.jule.domino.game.room.network.ServerOps;

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
