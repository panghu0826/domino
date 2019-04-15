package com.jule.domino.game.gate.network.process;

import com.jule.domino.game.gate.network.protocol.Ack;

public class PongAck_60000 extends Ack {
    /**
     * @param functionId
     */
    public PongAck_60000(int functionId) {
        super(functionId);
    }
}
