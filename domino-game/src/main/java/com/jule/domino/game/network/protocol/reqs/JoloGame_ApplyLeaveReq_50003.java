package com.jule.domino.game.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.core.common.log.LoggerUtils;
import com.jule.domino.base.enums.PlayerStateEnum;
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
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
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
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloGame.JoloGame_ApplyLeaveReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId,req.toString());
        this.setTable(TableService.getInstance().getTable(header.gameId + "", req.getRoomId(), req.getTableId()));
    }

    @Override
    public void processImpl() throws Exception {
        log.debug("收到消息-> " + functionId + " reqNum-> " + header.reqNum + "req->" + req.toString());
        String userId = req.getUserId();
        PlayerService.getInstance().onPlayerLoutOut("" + userId);
        AbstractTable table = getTable();
        PlayerInfo player = table.getPlayer(userId);
        JoloGame.JoloGame_ApplyLeaveAck.Builder ack = JoloGame.JoloGame_ApplyLeaveAck.newBuilder();
        try {
            //记录桌子最后操作时间
            table.setLastActionTime(System.currentTimeMillis());
            ack.setUserId(userId);
            ack.setRoomId(req.getRoomId());
            ack.setTableId(req.getTableId());
            ack.setResult(1);
            if(player.getTotalAlreadyBetScore4Hand() == 0) {
                //处理玩家离桌的redis信息
                LeaveTableLogic.getInstance().logic(player, table);
            }
            //输出结果给客户端
            ctx.writeAndFlush(new JoloGame_ApplyLeaveAck_50003(ack.build(), header));
            //玩家离桌广播
            NoticeBroadcastMessages.sendPlayerLeaveNotice(table,player);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            log.info("50003 ack 玩家离桌->: {}", ack.toString());
        }
    }
}
