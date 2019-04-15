package com.jule.domino.notice.network;

import com.jule.domino.notice.network.protocol.ClientAck;
import com.jule.domino.notice.network.protocol.acks.DummyAck_01;
import com.jule.domino.notice.network.protocol.acks.JoloNotice_SendGamePlayMsgAck_10001;
import com.jule.domino.notice.network.protocol.acks.JoloNotice_SendNormalMsgAck_10000;

import java.util.HashMap;
import java.util.Map;

public class ServerOps {
    private static Map<Class<? extends ClientAck>, Integer> opcodes = new HashMap<Class<? extends ClientAck>, Integer>();

    static {
        opcodes.put(DummyAck_01.class, 0x08000000 | 01);
        opcodes.put(JoloNotice_SendNormalMsgAck_10000.class, 0x08000000 | 10000);
        opcodes.put(JoloNotice_SendGamePlayMsgAck_10001.class, 0x08000000 | 10001);
    }

    /**
     * @param clazz
     * @return
     */
    public static int getOpCode(Class<? extends ClientAck> clazz) {
        if (!opcodes.containsKey(clazz)) {
            throw new IllegalArgumentException("opCode not find");
        }
        return opcodes.get(clazz);
    }
}
