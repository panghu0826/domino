package com.jule.domino.gate.network.process;

import com.jule.domino.gate.network.protocol.Ack;

/**
 * 客户端获取房间列表回复
 */
public class RoomProxyAck extends Ack {
    /**
     * @param functionId
     */
    public RoomProxyAck(int functionId) {
        super(functionId);
    }
}
