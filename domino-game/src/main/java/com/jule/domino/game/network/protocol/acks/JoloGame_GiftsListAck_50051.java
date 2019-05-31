package com.jule.domino.game.network.protocol.acks;

import com.google.protobuf.MessageLite;
import com.jule.domino.game.network.ServerOps;
import com.jule.domino.game.network.protocol.ClientAck;
import com.jule.domino.game.network.protocol.ClientHeader;

/**
 * @author
 * @since 2018/9/11 18:53
 */
public class JoloGame_GiftsListAck_50051 extends ClientAck {
    /**
     * @param messageLite
     * @param header
     */
    public JoloGame_GiftsListAck_50051(MessageLite messageLite, ClientHeader header) {
        super(messageLite, header);
        setFunctionId(ServerOps.getOpCode(this.getClass()));
    }
}
