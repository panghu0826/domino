package com.jule.domino.room.network;


import com.jule.domino.room.network.protocol.ClientAck;
import com.jule.domino.room.network.protocol.acks.DummyAck_01;
import com.jule.domino.room.network.protocol.acks.JoloRoom_ApplyChangeTableAck_40002;
import com.jule.domino.room.network.protocol.acks.JoloRoom_ApplyJoinTableAck_40001;

import java.util.HashMap;
import java.util.Map;

public class ServerOps {
    private static Map<Class<? extends ClientAck>, Integer> opcodes = new HashMap<Class<? extends ClientAck>, Integer>();

    static {
        opcodes.put(DummyAck_01.class, 0x08000000 | 01);
//        opcodes.put(JoloRoom_GetRoomListAck_40001.class, 0xFF000000 | 40001);
        opcodes.put(JoloRoom_ApplyChangeTableAck_40002.class, 0x08000000 | 40002);
//        opcodes.put(JoloRoom_GetTableStatusInfoAck_40003.class, 0xFF000000 | 40003);
//        opcodes.put(JoloRoom_ApplyJoinTableAck_40004.class, 0xFF000000 | 40004);
        opcodes.put(JoloRoom_ApplyJoinTableAck_40001.class, 0x08000000 | 40001);
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
