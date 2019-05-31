package com.jule.domino.game.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.core.common.log.LoggerUtils;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.game.gameUtil.GameLogic;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.acks.JoloGame_OtherPlayerInfoAck_50014;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.TableService;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

/**
 * 玩家亮牌请求
 */
@Slf4j
public class JoloGame_OtherPlayerInfoReq_50014 extends ClientReq {
    public JoloGame_OtherPlayerInfoReq_50014(int functionId) {
        super(functionId);
    }

    private JoloGame.JoloGame_OtherPlayerInfoReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloGame.JoloGame_OtherPlayerInfoReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId,req.toString());
        this.setTable(TableService.getInstance().getTable(header.gameId + "", req.getRoomId(), req.getTableId()));
    }

    @Override
    public void processImpl() throws Exception {
//        log.debug("收到消息-> " + functionId + ", reqNum-> " + header.reqNum + ", req->" + req.toString());
        log.info("收到消息-> " + functionId + ", req->" + req.toString());
        String userId = req.getUserId();
        JoloGame.JoloGame_OtherPlayerInfoAck.Builder ack = JoloGame.JoloGame_OtherPlayerInfoAck.newBuilder();
        try {
            AbstractTable table = getTable();
            //记录桌子最后操作时间
            table.setLastActionTime(System.currentTimeMillis());
            PlayerInfo player = table.getPlayer(userId);
            log.info("桌子目前的信息：{}",table.toString());
            log.info("玩家的信息：{}",player.toSitDownString());
            if(!table.getInGamePlayers().containsKey(player.getSeatNum())){
                ctx.writeAndFlush(new JoloGame_OtherPlayerInfoAck_50014(ack.setResult(-1).setResultMsg("不可进行该操作。").build(), header));
                return;
            }
            player.setState(PlayerStateEnum.open_card);
            ctx.writeAndFlush(new JoloGame_OtherPlayerInfoAck_50014(ack.setResult(1).build(), header));
            //玩家亮牌通知
            NoticeBroadcastMessages.playerOpenCard(table,player);
            //桌子上的所有人都已亮牌进入开牌阶段
            boolean isSettle = true;
            for(PlayerInfo playerInfo : table.getInGamePlayers().values()){
                if(playerInfo.getState() != PlayerStateEnum.open_card){
                    isSettle = false;
                }
            }
            if (isSettle && table.getTableStateEnum() == TableStateEnum.OPEN_CARD) {
                log.info("所有人都已经亮牌,准备进入结算{}",table.toString());
                GameLogic.settleAnimation(table); //调用开牌计时器
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            log.info("50014 ack 玩家亮牌：{}", ack.toString());
        }
    }
}
