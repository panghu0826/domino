package com.jule.domino.dispacher.network.process;

import com.jule.domino.dispacher.network.protocol.Ack;

/**
 * @author lyb 2018-06-21
 */
public class MailProxyAck extends Ack {
    /**
     * @param functionId
     */
    public MailProxyAck(int functionId) {
        super(functionId);
    }
}
