package com.jule.domino.notice.network.protocol.acks;

import com.google.protobuf.MessageLite;
import com.jule.domino.notice.network.ServerOps;
import com.jule.domino.notice.network.protocol.ClientAck;
import com.jule.domino.notice.network.protocol.ClientHeader;

public class DummyAck_01 extends ClientAck {

    /**
     * @param messageLite
     */
    public DummyAck_01(MessageLite messageLite, ClientHeader header) {
        super(messageLite, header);
        setFunctionId(ServerOps.getOpCode(this.getClass()));
    }
}
