package com.jule.domino.game.notice.network.protocol.acks;

import com.google.protobuf.MessageLite;
import com.jule.domino.game.network.protocol.ClientAck;
import com.jule.domino.game.network.protocol.ClientHeader;
import com.jule.domino.game.notice.network.ServerOps;

public class JoloNotice_SendNormalMsgAck_10000 extends ClientAck {

    /**
     * @param messageLite
     */
    public JoloNotice_SendNormalMsgAck_10000(MessageLite messageLite, ClientHeader header) {
        super(messageLite, header);
        setFunctionId(ServerOps.getOpCode(this.getClass()));
    }
}