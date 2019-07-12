package com.jule.domino.game.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.acks.JoloGame_Anti_FakeAck_50066;
import com.jule.domino.game.network.protocol.acks.JoloGame_UnlockAck_50055;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.TableService;
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JoloGame_Anti_FakeReq_50066 extends ClientReq {

    public JoloGame_Anti_FakeReq_50066(int functionId) {
        super(functionId);
    }

    private JoloGame.JoloGame_Anti_FakeReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloGame.JoloGame_Anti_FakeReq.parseFrom(blob);
//        RabbitMqSender.me.producer(functionId, req.toString());
    }

    @Override
    public void processImpl() throws Exception {
//        log.debug("收到消息, functionId->" + functionId + ", reqNum->" + header.reqNum + ", req->" + req.toString());
        log.info("收到消息, functionId->" + functionId + ", req->" + req.toString());
        JoloGame.JoloGame_Anti_FakeAck.Builder ack = JoloGame.JoloGame_Anti_FakeAck.newBuilder();
        String userId = req.getUserId();
        try {
            //用户信息：从缓存获取
            User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId, User.class);
            user.setDown_platform(req.getAntiFake());
            int in = DBUtil.updateByPrimaryKey(user);
            if(in == 1){
                ack.setResult(1);
            }else {
                ack.setResult(-1).setResultMsg("设置防卫字符出错！");
            }
            ctx.writeAndFlush(new JoloGame_Anti_FakeAck_50066(ack.build(), header));
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            log.info("50066 ack 玩家设置防伪字符->: {}", ack.toString());
        }
    }
}
