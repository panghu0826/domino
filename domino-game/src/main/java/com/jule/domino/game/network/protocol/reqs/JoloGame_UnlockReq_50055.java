package com.jule.domino.game.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.game.gameUtil.GameLogic;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.acks.JoloGame_ReadyAck_50018;
import com.jule.domino.game.network.protocol.acks.JoloGame_UnlockAck_50055;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.TableService;
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class JoloGame_UnlockReq_50055 extends ClientReq {

    public JoloGame_UnlockReq_50055(int functionId) {
        super(functionId);
    }

    private JoloGame.JoloGame_UnlockReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloGame.JoloGame_UnlockReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId, req.toString());
        this.setTable(TableService.getInstance().getTable(header.gameId + "", req.getRoomId(), req.getTableId()));
    }

    @Override
    public void processImpl() throws Exception {
//        log.debug("收到消息, functionId->" + functionId + ", reqNum->" + header.reqNum + ", req->" + req.toString());
        log.info("收到消息, functionId->" + functionId + ", req->" + req.toString());
        JoloGame.JoloGame_UnlockAck.Builder ack = JoloGame.JoloGame_UnlockAck.newBuilder();
        try {
            AbstractTable table = getTable();
            ctx.writeAndFlush(new JoloGame_UnlockAck_50055(
                    ack.setUserId(req.getUserId())
                            .setRoomId(req.getRoomId())
                            .setTableId(req.getTableId())
                            .setResult(1).build(), header));
            if(table != null) {
                PlayerInfo player = table.getPlayer(req.getUserId());
                player.setHeadSculpture(req.getHeadSculpture());
                player.setCardSkin(req.getCardSkin());
                NoticeBroadcastMessages.useItem(table,req.getUserId(),req.getHeadSculpture(),req.getCardSkin());
            }else {
                log.error("找不到该玩家：{}",req.getUserId());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            log.info("50055 ack 玩家使用道具->: {}", ack.toString());
        }
    }
}
