package com.jule.domino.game.room.network.protocol.acks;


import com.google.protobuf.MessageLite;
import com.jule.domino.game.network.protocol.ClientAck;
import com.jule.domino.game.network.protocol.ClientHeader;
import com.jule.domino.game.room.network.ServerOps;

/**
 * @author
 * @since 2018/9/11 17:03
 */
public class JoloRoom_RoomListAck_40005 extends ClientAck {

    public JoloRoom_RoomListAck_40005(MessageLite messageLite, ClientHeader header) {
        super(messageLite, header);
        setFunctionId(ServerOps.getOpCode(this.getClass()));
    }
}
