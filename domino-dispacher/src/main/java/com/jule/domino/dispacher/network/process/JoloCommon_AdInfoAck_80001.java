package com.jule.domino.dispacher.network.process;

import com.jule.domino.dispacher.network.protocol.Ack;

public class JoloCommon_AdInfoAck_80001 extends Ack {
    /**
     * @param functionId
     */
    public JoloCommon_AdInfoAck_80001( int functionId) {
        super(functionId);
    }
}
