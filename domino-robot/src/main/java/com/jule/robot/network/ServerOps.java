package com.jule.robot.network;

import com.jule.robot.network.protocol.ClientAck;
import com.jule.robot.network.protocol.acks.*;

import java.util.HashMap;
import java.util.Map;

public class ServerOps {
    private static Map<Class<? extends ClientAck>, Integer> opcodes = new HashMap<Class<? extends ClientAck>, Integer>();

    static {
        opcodes.put(DummyAck_01.class, 0xFF000000 | 01);
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
