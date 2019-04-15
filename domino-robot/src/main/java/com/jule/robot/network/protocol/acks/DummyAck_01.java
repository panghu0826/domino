package com.jule.robot.network.protocol.acks;

import com.google.protobuf.MessageLite;
import com.jule.robot.network.ServerOps;
import com.jule.robot.network.protocol.ClientAck;
import com.jule.robot.network.protocol.ClientHeader;

public class DummyAck_01 extends ClientAck {

    /**
     * @param messageLite
     */
    public DummyAck_01(MessageLite messageLite, ClientHeader header) {
        super(messageLite, header);
        setFunctionId(ServerOps.getOpCode(this.getClass()));
    }
}
