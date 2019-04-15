package com.jule.domino.game.network.protocol.acks;

import com.google.protobuf.MessageLite;
import com.jule.domino.game.network.ServerOps;
import com.jule.domino.game.network.protocol.ClientAck;
import com.jule.domino.game.network.protocol.ClientHeader;

public class JoloGame_ReadyAck_50018 extends ClientAck {
    /**
     * @param messageLite
     * @param header
     */
    public JoloGame_ReadyAck_50018(MessageLite messageLite, ClientHeader header) {
        super(messageLite, header);
        setFunctionId(ServerOps.getOpCode(this.getClass()));
    }
}
