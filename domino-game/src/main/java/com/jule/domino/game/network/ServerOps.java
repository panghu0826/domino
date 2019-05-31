package com.jule.domino.game.network;

import com.jule.domino.game.network.protocol.ClientAck;
import com.jule.domino.game.network.protocol.acks.*;
import com.jule.domino.game.service.holder.FunctionIdHolder;

import java.util.HashMap;
import java.util.Map;

public class ServerOps {
    private static Map<Class<? extends ClientAck>, Integer> opcodes = new HashMap<>();

    static {

        opcodes.put(GatePongAck_01.class, 0x08000000 | 1);
        opcodes.put(JoloGame_ApplySitDownAck_50001.class, FunctionIdHolder.Game_ACK_ApplySiteDown);
        opcodes.put(JoloGame_ApplyStandUpAck_50002.class, FunctionIdHolder.Game_ACK_ApplyStandUp);
        opcodes.put(JoloGame_ApplyBetAck_50005.class, FunctionIdHolder.Game_ACK_ApplyBet);
        opcodes.put(JoloGame_ApplyFoldAck_50012.class, FunctionIdHolder.Game_Ack_ApplyFold);
        opcodes.put(JoloGame_ReadyAck_50018.class, FunctionIdHolder.Game_ACK_ReadyType);


        opcodes.put(JoloGame_ApplyJoinTableAck_50000.class, FunctionIdHolder.Game_ACK_ApplyJoinTable);
        opcodes.put(JoloGame_ApplyLeaveAck_50003.class, FunctionIdHolder.Game_ACK_ApplyLeave);
        opcodes.put(JoloGame_SpecialFunctionAck_50015.class, FunctionIdHolder.Game_Ack_SpecialFunction);
        opcodes.put(JoloGame_OtherPlayerInfoAck_50014.class, FunctionIdHolder.Game_ACK_OtherUserInfo);
        opcodes.put(JoloGame_ReconnectAck_50016.class, FunctionIdHolder.Game_ACK_Reconnect);
        opcodes.put(JoloGame_PlayRecordsAck_50063.class,FunctionIdHolder.Game_ACK_GameRecords);
        opcodes.put(JoloGame_GiftsListAck_50051.class,FunctionIdHolder.Game_ACK_ChatMesgSend);
        opcodes.put(JoloGame_UnlockAck_50055.class,FunctionIdHolder.Game_ACK_Item);

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
