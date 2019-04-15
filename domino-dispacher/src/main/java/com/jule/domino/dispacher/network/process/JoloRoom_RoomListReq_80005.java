package com.jule.domino.dispacher.network.process;

import JoloProtobuf.RoomSvr.JoloRoom;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.service.RoomConfigSerivce;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

/**
 * 请求展示房间列表
 *
 * @author
 *
 * @since 2018/9/11 17:03
 */
@Slf4j
public class JoloRoom_RoomListReq_80005 extends Req {

    private byte[] blob;

    private JoloRoom.JoloRoom_GetRoomListReq req;

    public JoloRoom_RoomListReq_80005(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloRoom.JoloRoom_GetRoomListReq.parseFrom(blob);
    }

    @Override
    public void processImpl() throws Exception {
        log.info("房间列表请求={}",req.toString());
        sendAcqMsg(JoloRoom.JoloRoom_GetRoomListAck.newBuilder()
                .setResult(1)
                .setUserId(req.getUserId())
                .setGameId(req.getGameId())
                .setIsGetTableList(1)
                .addAllRoomList(RoomConfigSerivce.OBJ.getRoomConfigs()));
    }
}
