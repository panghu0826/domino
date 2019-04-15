package com.jule.domino.game.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.core.common.log.LoggerUtils;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.acks.JoloGame_OtherPlayerInfoAck_50014;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.TableService;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.enums.GameConst;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

/**
 * 记录本桌内信息 入桌清空
 */
@Slf4j
public class JoloGame_OtherPlayerInfoReq_50014 extends ClientReq {
    private long time;

    public JoloGame_OtherPlayerInfoReq_50014(int functionId) {
        super(functionId);
    }

    private JoloGame.JoloGame_OtherPlayerInfoReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        time = System.currentTimeMillis();
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloGame.JoloGame_OtherPlayerInfoReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId,req.toString());
        this.setTable(TableService.getInstance().getTable(header.gameId + "", req.getRoomId(), req.getTableId()));
    }

    @Override
    public void processImpl() throws Exception {
        log.debug("收到消息-> " + functionId + ", reqNum-> " + header.reqNum + ", req->" + req.toString());
        String userId = this.userId;
        int seatNum = req.getSeatNum();
        AbstractTable table = getTable();
        PlayerInfo player = table.getPlayer(userId);
        JoloGame.JoloGame_OtherPlayerInfoAck.Builder ack = JoloGame.JoloGame_OtherPlayerInfoAck.newBuilder();
        try {
            ack.setResult(1);

            if (null == player) {
                ack.setResult(-1).setResultMsg(ErrorCodeEnum.GAME_50050_2.getCode());
                ctx.writeAndFlush(new JoloGame_OtherPlayerInfoAck_50014(ack.build(), header));
                return;
            }

            if (player.getState().getValue() == PlayerStateEnum.spectator.getValue()) {
                ack.setResult(-2).setResultMsg(ErrorCodeEnum.GAME_50014_1.getCode());
                ctx.writeAndFlush(new JoloGame_OtherPlayerInfoAck_50014(ack.build(), header));
                return;
            }

            if (player != null && player.getState().getValue() >= PlayerStateEnum.siteDown.getValue()) {
                PlayerInfo pi = table.getInGamePlayersBySeatNum().get(seatNum);
                if (pi == null) {
                    ack.setResult(-3).setResultMsg(ErrorCodeEnum.GAME_50014_2.getCode());
                    ctx.writeAndFlush(new JoloGame_OtherPlayerInfoAck_50014(ack.build(), header));
                    return;
                }

                JoloGame.JoloGame_TablePlay_OtherPlayerInfo.Builder playerInfo = JoloGame.JoloGame_TablePlay_OtherPlayerInfo.newBuilder();
                playerInfo.setUserId(pi.getPlayerId());
                playerInfo.setHandsTimes(pi.getHandsWon().getLinkedDeque().size());
                playerInfo.setNickName(pi.getNickName());
                playerInfo.setHandsWon(pi.getHandsWon().won());
                playerInfo.setPlayScoreStore(pi.getPlayScoreStore());
                playerInfo.setIcon(pi.getIcon());
                ack.setOtherPlayerInfo(playerInfo);
                ctx.writeAndFlush(new JoloGame_OtherPlayerInfoAck_50014(ack.build(), header));
            }
            long timeMillis = System.currentTimeMillis() - time;
            if (timeMillis > GameConst.COST_TIME) {
                LoggerUtils.performance.info("OtherPlayerInfoReq_50014,cost time:{},req:{}", timeMillis, req.toString());
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
        }
    }
}
