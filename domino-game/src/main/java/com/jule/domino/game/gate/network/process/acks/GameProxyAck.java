package com.jule.domino.game.gate.network.process.acks;

import com.jule.domino.game.gate.network.protocol.Ack;

/**
 */
public class GameProxyAck extends Ack {
    /**
     * @param functionId
     */
    public GameProxyAck(int functionId) {
        super(functionId);
    }
}
