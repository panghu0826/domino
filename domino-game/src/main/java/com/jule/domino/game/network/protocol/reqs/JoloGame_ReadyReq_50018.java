package com.jule.domino.game.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import JoloProtobuf.RoomSvr.JoloRoom;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.base.model.RoomTableRelationModel;
import com.jule.domino.game.gameUtil.DealCardForTable;
import com.jule.domino.game.gameUtil.GameLogic;
import com.jule.domino.game.gameUtil.GameOrderIdGenerator;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.TableInnerReq;
import com.jule.domino.game.network.protocol.acks.JoloGame_ApplySitDownAck_50001;
import com.jule.domino.game.network.protocol.acks.JoloGame_ReadyAck_50018;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.TableService;
import com.jule.domino.game.service.TimerService;
import com.jule.domino.game.service.holder.CardOfTableHolder;
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class JoloGame_ReadyReq_50018 extends ClientReq {

    public JoloGame_ReadyReq_50018(int functionId) {
        super(functionId);
    }

    private JoloGame.JoloGame_ReadyReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloGame.JoloGame_ReadyReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId, req.toString());
        this.setTable(TableService.getInstance().getTable(header.gameId + "", req.getRoomId(), req.getTableId()));
    }

    @Override
    public void processImpl() throws Exception {
//        log.debug("收到消息, functionId->" + functionId + ", reqNum->" + header.reqNum + ", req->" + req.toString());
        log.info("收到消息, functionId->" + functionId + ", req->" + req.toString());
        JoloGame.JoloGame_ReadyAck.Builder ack = JoloGame.JoloGame_ReadyAck.newBuilder();
        try {
            int seatNum = req.getSeatNum();
            AbstractTable table = getTable();
            //记录桌子最后操作时间
            table.setLastActionTime(System.currentTimeMillis());
            PlayerInfo playerInfo = table.getPlayer(req.getUserId());
            ack.setUserId(req.getUserId());
            ack.setRoomId(req.getRoomId());
            ack.setTableId(req.getTableId());
            ack.setSeatNum(seatNum);
            ack.addAllReadyList(new ArrayList<>());
            if(playerInfo.getSeatNum() <= 0){
                ctx.writeAndFlush(new JoloGame_ReadyAck_50018(ack.setResult(-2).setResultMsg("未坐下不可准备。").build(), header));
                return;
            }
            log.info("桌子目前的信息：{}",table.toString());
            log.info("玩家的信息：{}",playerInfo.toSitDownString());
            if (table.getTableStateEnum() == TableStateEnum.IDEL
                    || table.getTableStateEnum() == TableStateEnum.GAME_READY) {
                ctx.writeAndFlush(new JoloGame_ReadyAck_50018(ack.setResult(1).build(), header));
                //玩家准备，并放进游戏中集合里
                playerInfo.setState(PlayerStateEnum.game_ready);
                table.getInGamePlayers().put(seatNum, playerInfo);
                boolean flags = table.getInGamePlayers().size() >= 2 && table.getInGamePlayers().size() >= table.getInGamePlayersBySeatNum().size();
                if (!flags) {
                    NoticeBroadcastMessages.readyStatus(table, playerInfo);
                }
                log.info("桌子目前得状态：{}，是否全部准备{}", table.getTableStateEnum(), (table.getInGamePlayers().size() >= table.getInGamePlayersBySeatNum().size()));
                if(flags){
                    GameLogic.gameStart(table);
                }else if(table.getTableStateEnum() == TableStateEnum.IDEL && table.getInGamePlayers().size() >= 2){
                    //游戏准备cd广播
                    NoticeBroadcastMessages.gameStart(table);
                    //游戏准备cd
                    GameLogic.gameReady(table);
                }
                //判断游戏是否能开始
//                TableService.getInstance().playGame(table);
            } else {
                ctx.writeAndFlush(new JoloGame_ReadyAck_50018(ack.setResult(-1).setResultMsg("游戏中不可进行该操作。").build(), header));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            log.info("50018 ack 玩家准备->: {}", ack.toString());
        }
    }
}
