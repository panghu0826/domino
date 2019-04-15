package com.jule.domino.dispacher.network.process;

import com.jule.domino.dispacher.network.protocol.Ack;

/**
 * @author
 * @since 2018/9/11 17:03
 */
public class JoloRoom_RoomListAck_80005 extends Ack {
    /**
     * @param functionId
     */
    public JoloRoom_RoomListAck_80005(int functionId) {
        super(functionId);
    }
}
