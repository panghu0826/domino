package com.jule.domino.game.gate.network.process;

import com.jule.domino.game.gate.network.protocol.Ack;

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
