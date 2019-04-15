package com.jule.domino.game.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.core.common.log.LoggerUtils;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.game.network.protocol.logic.LeaveTableLogic;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.play.TableUtil;
import com.jule.domino.game.service.PlayerService;
import com.jule.domino.game.service.TableService;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.acks.JoloGame_ApplyLeaveAck_50003;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

/**
 * 申请离桌
 */
@Slf4j
public class JoloGame_ApplyLeaveReq_50003 extends ClientReq {

    private long time;

    public JoloGame_ApplyLeaveReq_50003(int functionId) {
        super(functionId);
    }

    private JoloGame.JoloGame_ApplyLeaveReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        time = System.currentTimeMillis();
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloGame.JoloGame_ApplyLeaveReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId,req.toString());
        this.setTable(TableService.getInstance().getTable(header.gameId + "", req.getRoomId(), req.getTableId()));
    }

    @Override
    public void processImpl() throws Exception {
        log.debug("收到消息-> " + functionId + " reqNum-> " + header.reqNum + "req->" + req.toString());
        String userId = this.userId;
        PlayerService.getInstance().onPlayerLoutOut("" + userId);
        AbstractTable table = getTable();
        if (table == null) {
            log.error("table is null ");
            return;
        }
        PlayerInfo player = table.getPlayer(userId);
        JoloGame.JoloGame_ApplyLeaveAck.Builder ack = JoloGame.JoloGame_ApplyLeaveAck.newBuilder();
        try {

            ack.setUserId(userId);
            ack.setRoomId(req.getRoomId());
            ack.setTableId(req.getTableId());
            ack.setResult(1);

            if (table.getTableStateEnum().equals(TableStateEnum.IDEL) ||
                    table.getTableStateEnum().equals(TableStateEnum.GAME_READY) ||
                    table.getTableStateEnum().equals(TableStateEnum.SETTLE_ANIMATION)) {

            } else {
                log.error("请在本局结束后操作 state:" + table.getTableStateEnum());
                ack.setResult(-1).setResultMsg("请在本局结束后操作");
                ctx.writeAndFlush(new JoloGame_ApplyLeaveAck_50003(ack.build(), header));
                return;
            }
            if (null == player) {
                log.error("can't found player info, playerId->" + userId);
                ack.setResult(1).setResultMsg(ErrorCodeEnum.GAME_50050_2.getCode());//modify by gx 20181010 resut=1,因为找不到玩家信息，可以返回大厅
                ctx.writeAndFlush(new JoloGame_ApplyLeaveAck_50003(ack.build(), header));
                return;
            }

            LeaveTableLogic.getInstance().logic(player, table, ack);

            //输出结果给客户端
            ctx.writeAndFlush(new JoloGame_ApplyLeaveAck_50003(ack.build(), header));
            long timeMillis = System.currentTimeMillis() - time;
            if (timeMillis > GameConst.COST_TIME) {
                LoggerUtils.performance.info("ApplyLeaveReq_50003,cost time:{},req:{}", timeMillis, req.toString());
            }
        } catch (Exception ex) {
            //TODO 是否必须字段都加上了
            ack.setResult(-10).setResultMsg(ErrorCodeEnum.GAME_50002_2.getCode());
            ack.setCurrStoreScore(0);
            ctx.writeAndFlush(new JoloGame_ApplyLeaveAck_50003(ack.build(), header));
            log.error("", ex);
        } finally {
            log.debug("Leave ACK info: " + ack.toString());
            log.debug("Leave ACK bytes length: " + ack.build().toByteArray().length);
            if (null != table) {
                log.debug("All Player info: " + System.getProperty("line.separator") + TableUtil.toStringAllPlayers(table));

                log.debug("InGame Player info: " + System.getProperty("line.separator") + TableUtil.toStringInGamePlayers(table));
            }
            log.debug("Leave over. Table state: " + table.getTableStateEnum());
        }
    }
}
