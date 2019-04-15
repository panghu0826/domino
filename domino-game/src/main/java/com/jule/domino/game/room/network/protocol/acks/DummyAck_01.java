package com.jule.domino.game.room.network.protocol.acks;

import com.google.protobuf.MessageLite;
import com.jule.domino.game.network.protocol.ClientAck;
import com.jule.domino.game.network.protocol.ClientHeader;
import com.jule.domino.game.room.network.ServerOps;

public class DummyAck_01 extends ClientAck {

    /**
     * @param messageLite
     */
    public DummyAck_01(MessageLite messageLite, ClientHeader header) {
        super(messageLite, header);
        setFunctionId(ServerOps.getOpCode(this.getClass()));
    }
}
