package com.jule.domino.game.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.core.common.log.LoggerUtils;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.play.TableUtil;
import com.jule.domino.game.service.TableService;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RoleType;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.acks.JoloGame_ApplyStandUpAck_50002;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

/**
 * 申请站起
 */
@Slf4j
public class JoloGame_ApplyStandUpReq_50002 extends ClientReq {
    private long time;
    public JoloGame_ApplyStandUpReq_50002(int functionId) {
        super(functionId);
    }

    private JoloGame.JoloGame_ApplyStandUpReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        time = System.currentTimeMillis();
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloGame.JoloGame_ApplyStandUpReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId,req.toString());
        this.setTable(TableService.getInstance().getTable(header.gameId + "", req.getRoomId(), req.getTableId()));
    }

    @Override
    public void processImpl() throws Exception {
        log.debug("收到消息-> " + functionId + ", reqNum-> " + header.reqNum + ", req->" + req.toString());
        String userId = this.userId;
        String roomId = req.getRoomId();
        String tableId = req.getTableId();
        AbstractTable table = getTable();
        PlayerInfo player = table.getPlayer(userId);
        JoloGame.JoloGame_ApplyStandUpAck.Builder ack = JoloGame.JoloGame_ApplyStandUpAck.newBuilder();

        try {
            ack.setUserId(userId);
            ack.setRoomId(roomId);
            ack.setTableId(tableId);
            ack.setResult(1);

            if (table.getTableStateEnum().equals(TableStateEnum.IDEL) ||
                    table.getTableStateEnum().equals(TableStateEnum.GAME_READY) ||
                    table.getTableStateEnum().equals(TableStateEnum.SETTLE_ANIMATION)) {

            } else {
                log.error("请在本局结束后操作 state:" + table.getTableStateEnum());
                ack.setResult(-1).setResultMsg("请在本局结束后操作");
                ctx.writeAndFlush(new JoloGame_ApplyStandUpAck_50002(ack.build(), header));
                return;
            }
            if (null == player) {
                log.error("can't found player info, playerId->" + userId);
                ack.setResult(-1).setResultMsg(ErrorCodeEnum.GAME_50050_2.getCode());
                ctx.writeAndFlush(new JoloGame_ApplyStandUpAck_50002(ack.build(), header));
                return;
            }

            if (player.getState().getValue() == PlayerStateEnum.spectator.getValue()) {
                log.error("player is a spectator, playerId->" + userId);
                ack.setResult(-2).setResultMsg(ErrorCodeEnum.GAME_50014_1.getCode());
                ctx.writeAndFlush(new JoloGame_ApplyStandUpAck_50002(ack.build(), header));
                return;
            }

            if (player != null && player.getState().getValue() >= PlayerStateEnum.siteDown.getValue()) {
                if (table.standUp(player.getSeatNum(), player.getPlayerId(), "standUpReq")) {
                    if (player.getRoleType() != null && player.getRoleType().equals(RoleType.ROBOT)) {
                        LoggerUtils.robot.info("Robot standUp reason apply_stand_up id:" + player.getPlayerId() + ",gameId:" + table.getPlayType()
                                + ",roomId:" + table.getRoomId() + ",tableId:" + table.getTableId());
                    }
                    double currentMoney = table.playerDataSettlement(player);

                    ack.setCurrStoreScore(currentMoney);
                    ctx.writeAndFlush(new JoloGame_ApplyStandUpAck_50002(ack.build(), header));
                } else {//最好不要走到这里来
                    ack.setResult(-2).setResultMsg(ErrorCodeEnum.GAME_50002_1.getCode());
                    ctx.writeAndFlush(new JoloGame_ApplyStandUpAck_50002(ack.build(), header));
                    return;
                }
            }
            long timeMillis = System.currentTimeMillis() - time;
            if (timeMillis > GameConst.COST_TIME) {
                LoggerUtils.performance.info("ApplyStandUpReq_50002,cost time:{},req:{}", timeMillis, req.toString());
            }
        } catch (Exception ex) {
            log.error("系统异常");
            ack.setResult(-10).setResultMsg(ErrorCodeEnum.GAME_50002_1.getCode());
            ctx.writeAndFlush(new JoloGame_ApplyStandUpAck_50002(ack.build(), header));
            log.error("", ex);
        } finally {
            log.debug("StandUp ACK info: " + ack.toString());
            log.debug("StandUp ACK bytes length: " + ack.build().toByteArray().length);
            if (null != table) {
                log.debug("All Player info: " + System.getProperty("line.separator") + TableUtil.toStringAllPlayers(table));

                log.debug("InGame Player info: " + System.getProperty("line.separator") + TableUtil.toStringInGamePlayers(table));
            }
            log.debug("StandUp over. Table state: " + table.getTableStateEnum());
        }
    }
}
