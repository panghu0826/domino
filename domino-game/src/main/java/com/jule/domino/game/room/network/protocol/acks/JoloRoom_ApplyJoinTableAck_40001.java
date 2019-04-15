package com.jule.domino.game.room.network.protocol.acks;

import com.google.protobuf.MessageLite;
import com.jule.domino.game.network.protocol.ClientAck;
import com.jule.domino.game.network.protocol.ClientHeader;
import com.jule.domino.game.room.network.ServerOps;

/**
 * 申请入桌
 */
public class JoloRoom_ApplyJoinTableAck_40001 extends ClientAck {

    /**
     * @param messageLite
     */
    public JoloRoom_ApplyJoinTableAck_40001(MessageLite messageLite, ClientHeader header) {
        super(messageLite, header);
        setFunctionId(ServerOps.getOpCode(this.getClass()));
    }
}
