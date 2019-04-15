package com.jule.domino.game.network.protocol.acks;

import com.google.protobuf.MessageLite;
import com.jule.domino.game.network.ServerOps;
import com.jule.domino.game.network.protocol.ClientAck;
import com.jule.domino.game.network.protocol.ClientHeader;

/**
 * 心跳消息网关->游戏服务器回复
 *
 * 申请入座
 */
public class JoloGame_ApplySitDownAck_50001 extends ClientAck {

    /**
     * @param messageLite
     */
    public JoloGame_ApplySitDownAck_50001(MessageLite messageLite, ClientHeader header) {
        super(messageLite, header);
        setFunctionId(ServerOps.getOpCode(this.getClass()));
    }
}
