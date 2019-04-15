package com.jule.domino.dispacher.network.process;

import com.jule.domino.dispacher.network.protocol.Ack;

public class PongAck_60000 extends Ack {
    /**
     * @param functionId
     */
    public PongAck_60000(int functionId) {
        super(functionId);
    }
}
