package com.jule.domino.game.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.game.gameUtil.NNGameLogic;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.acks.JoloGame_Rob_DealerAck_50019;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.TableService;
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 抢庄
 */
@Slf4j
public class JoloGame_Rob_DealerReq_50019 extends ClientReq {
    private long time;
    private JoloGame.JoloGame_Rob_DealerReq req;

    public JoloGame_Rob_DealerReq_50019(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf byteBuf) throws Exception {
        time = System.currentTimeMillis();
        byte[] blob = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(blob);
        req = JoloGame.JoloGame_Rob_DealerReq.parseFrom(blob);
//        RabbitMqSender.me.producer(functionId, req.toString());
        AbstractTable table = TableService.getInstance().getTable(header.gameId + "", req.getRoomId(), req.getTableId());
        if (null != table) {
            this.setTable(table);
        } else {
            log.error("桌子不存在,roomId->" + req.getRoomId() + ", tableId->" + req.getTableId());
        }
    }

    @Override
    public void processImpl() throws Exception {
        log.debug("收到消息-> " + functionId + " reqNum-> " + header.reqNum + "msg ->" + req.toString());

        String userId = this.userId;
        //String gameOrderId = req.getGameOrderId();
        String roomId = req.getRoomId();
        int seatNum = req.getSeatNum();
        String tableId = req.getTableId();
        int multiple = req.getMultiple();

        JoloGame.JoloGame_Rob_DealerAck.Builder ack = JoloGame.JoloGame_Rob_DealerAck.newBuilder();
        ack.setUserId(userId);
        ack.setRoomId(roomId);
        ack.setTableId(tableId);
        ack.setSeatNum(seatNum);
        ack.setResult(1);

        try {
            AbstractTable table = getTable();
            if (table == null) {
                log.info("table is null , userId = " + userId);
                return;
            }
            if (table.getTableStateEnum().getValue() > TableStateEnum.PLAYER_ROB.getValue()) {
                ack.setResult(-1).setResultMsg("桌内状态不符，不能操作");
                ctx.writeAndFlush(new JoloGame_Rob_DealerAck_50019(ack.build(), header));
                return;
            }
            //查找玩家信息
            PlayerInfo player = table.getPlayer(userId);
            if (null == player) {
                ack.setResult(-2).setResultMsg("找不到该玩家");
                ctx.writeAndFlush(new JoloGame_Rob_DealerAck_50019(ack.build(), header));
                return;
            }
            //设置自己的状态
            player.setState(PlayerStateEnum.already_rob);
            player.setMultiple(multiple);

            ctx.writeAndFlush(new JoloGame_Rob_DealerAck_50019(ack.build(), header));
            //广播我抢庄的结果
            NoticeBroadcastMessages.robMultiple(table, player);
            if (table.isActionOver(PlayerStateEnum.already_rob)) {
                NNGameLogic.RobDealerAnimation(table);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
//            ack.setResult(10);
//            ctx.writeAndFlush(new JoloGame_Rob_DealerAck_50019(ack.build(), header));
//            log.error(ex.getMessage(), ex);
        }finally {
            log.info("50019 ack 玩家抢庄->: {}", ack.toString());
        }
    }
}
