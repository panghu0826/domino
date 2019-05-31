package com.jule.domino.game.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.game.gameUtil.GameLogic;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.acks.JoloGame_ApplyFoldAck_50012;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.TableService;
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Slf4j
public class JoloGame_ApplyFoldReq_50012 extends ClientReq {

    public JoloGame_ApplyFoldReq_50012(int functionId) {
        super(functionId);
    }

    private JoloGame.JoloGame_ApplyFoldReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloGame.JoloGame_ApplyFoldReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId, req.toString());
        this.setTable(TableService.getInstance().getTable(header.gameId + "", req.getRoomId(), req.getTableId()));
    }

    @Override
    public void processImpl() throws Exception {
//        log.debug("收到消息, functionId->" + functionId + ", reqNum->" + header.reqNum + ", req->" + req.toString());
        log.info("收到消息, functionId->" + functionId +", req->" + req.toString());
        JoloGame.JoloGame_ApplyFoldAck.Builder ack = JoloGame.JoloGame_ApplyFoldAck.newBuilder();
        try {
            AbstractTable table = getTable();
            //记录桌子最后操作时间
            table.setLastActionTime(System.currentTimeMillis());
            log.info("当前桌子的状态：{}",table.toString());
            if(table.getTableStateEnum() != TableStateEnum.BET){
                ctx.writeAndFlush(new JoloGame_ApplyFoldAck_50012(
                        ack.setUserId(req.getUserId())
                                .setRoomId(req.getRoomId())
                                .setTableId(req.getTableId())
                                .setGameOrderId(req.getGameOrderId())
                                .setBetRoundId(req.getBetRoundId())
                                .setResult(-1)
                                .setResultMsg("游戏此阶段不可弃牌。").build(), header));
                return;
            }

            PlayerInfo playerInfo = table.getPlayer(req.getUserId());
            playerInfo.setState(PlayerStateEnum.fold);
            //将玩家从游戏map中踢出
            table.getInGamePlayers().remove(playerInfo.getSeatNum());
            ctx.writeAndFlush(new JoloGame_ApplyFoldAck_50012(
                    ack.setUserId(req.getUserId())
                            .setRoomId(req.getRoomId())
                            .setTableId(req.getTableId())
                            .setGameOrderId(req.getGameOrderId())
                            .setBetRoundId(req.getBetRoundId())
                            .setResult(1).build(), header));
            log.info("当前玩家主动弃牌的数据：{}",playerInfo.toSitDownString());
            GameLogic.playerFold(table,playerInfo);
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            log.info("50012 ack 玩家弃牌->: {}", ack.toString());
        }
    }
}
