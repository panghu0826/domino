package com.jule.domino.game.room.network.protocol.reqs;

import JoloProtobuf.RoomSvr.JoloRoom;
import com.google.common.base.Strings;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.platform.HallAPIService;
import com.jule.domino.base.platform.bean.PlatUserBean;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.gate.service.UserService;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.room.network.protocol.acks.JoloRoom_RoomListAck_40005;
import com.jule.domino.game.room.service.RoomConfigSerivce;
import com.jule.domino.game.service.NoticePlatformSerivce;
import com.jule.domino.game.utils.NumUtils;
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
public class JoloRoom_RoomListReq_40005 extends ClientReq {

    private JoloRoom.JoloRoom_GetRoomListReq req;

    public JoloRoom_RoomListReq_40005(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req =  JoloRoom.JoloRoom_GetRoomListReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId,req.toString());
    }

    @Override
    public void processImpl() throws Exception {
        log.info("房间列表请求={}",req.toString());
        JoloRoom.JoloRoom_GetRoomListAck.Builder ack = JoloRoom.JoloRoom_GetRoomListAck.newBuilder();

        ack.setResult(1)
                .setUserId(req.getUserId())
                .setGameId(req.getGameId())
                .setIsGetTableList(1)
                .addAllRoomList(RoomConfigSerivce.OBJ.getRoomConfigs());

        ctx.writeAndFlush(new JoloRoom_RoomListAck_40005(ack.build(), header));

        log.debug("method=updateMoney, user= {}",ack.getUserId());
        User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(),RedisConst.USER_INFO.getField()+this.userId,User.class);
        if (user == null || user.getChannel_id().equals("robot")){
            log.debug("method=updateMoney, user= {},channel={}",this.userId,user.getChannel_id());
            return;
        }
//        PlatUserBean userBean = HallAPIService.OBJ.getById(user.getAndroid_id());
//        if (userBean == null){
//            log.debug("method=updateMoney, user= {},userBean=null",ack.getUserId());
//            return;
//        }

        double curMoney = NumUtils.double2Decimal(user.getMoney());
        if (user != null) {
            user.setMoney(curMoney);
            StoredObjManager.hset(RedisConst.USER_INFO.getProfix(),RedisConst.USER_INFO.getField()+user.getId(), user);
        }
        UserService.getInstance().sendPayNoticeMsg(user.getId() ,curMoney);
        log.debug("method=updateMoney, user= {}, money = {}",user.getId(), curMoney);
    }
}
