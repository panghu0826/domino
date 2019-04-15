package com.jule.domino.gate.network.process;

import com.jule.domino.gate.network.protocol.Ack;

public class PongAck_60000 extends Ack {
    /**
     * @param functionId
     */
    public PongAck_60000(int functionId) {
        super(functionId);
    }
}
