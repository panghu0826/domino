package com.jule.domino.notice.network.protocol.acks;

import com.google.protobuf.MessageLite;
import com.jule.domino.notice.network.ServerOps;
import com.jule.domino.notice.network.protocol.ClientAck;
import com.jule.domino.notice.network.protocol.ClientHeader;

public class JoloNotice_SendGamePlayMsgAck_10001 extends ClientAck {

    /**
     * @param messageLite
     */
    public JoloNotice_SendGamePlayMsgAck_10001(MessageLite messageLite, ClientHeader header) {
        super(messageLite, header);
        setFunctionId(ServerOps.getOpCode(this.getClass()));
    }
}
