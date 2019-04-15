package com.jule.domino.gate.network.process;

import com.jule.domino.gate.network.protocol.Ack;

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
