package com.jule.domino.game.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.core.common.log.LoggerUtils;
import com.jule.domino.game.gameUtil.GameLogic;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.acks.JoloGame_ApplyBetAck_50005;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.play.TableUtil;
import com.jule.domino.game.service.TableService;
import com.jule.domino.game.utils.NumUtils;
import com.jule.domino.game.utils.log.TableLogUtil;
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.enums.GameConst;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

/**
 * 申请下注
 */
@Slf4j
public class JoloGame_ApplyBetReq_50005 extends ClientReq {
    private long time;

    public JoloGame_ApplyBetReq_50005(int functionId) {
        super(functionId);
    }

    private JoloGame.JoloGame_ApplyBetReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        time = System.currentTimeMillis();
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloGame.JoloGame_ApplyBetReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId,req.toString());
        this.setTable(TableService.getInstance().getTable(header.gameId + "", req.getRoomId(), req.getTableId()));
    }

    @Override
    public void processImpl() throws Exception {
        log.debug("收到消息-> " + functionId + ", reqNum-> " + header.reqNum + ", req->" + req.toString());
        String userId = this.userId;
        String roomId = req.getRoomId();
        String tableId = req.getTableId();
        String gameOrderId = req.getGameOrderId();
        int betScore = req.getBetScore();
        AbstractTable table = getTable();
        JoloGame.JoloGame_ApplyBetAck.Builder ack = JoloGame.JoloGame_ApplyBetAck.newBuilder();
        ack.setUserId(userId);
        ack.setRoomId(roomId);
        ack.setTableId(tableId);
        ack.setGameOrderId(gameOrderId);
        ack.setBetScore(betScore);

        try {
            byte betBtn = NumUtils.intToByte((int)betScore);

            if (table == null) {
                ack.setResult(-1).setResultMsg("找不到当前桌子");
                ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.build(), header));
                return;
            }
            if (table.getTableStateEnum().getValue() > TableStateEnum.BET.getValue()) {
                log.error("当前牌桌状态={}",table.getTableStateEnum().name());
                ack.setResult(-1).setResultMsg("桌内状态不符，不能操作");
                ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.build(), header));
                return;
            }

            PlayerInfo player = table.getPlayer(userId);
            if (null == player) {
                log.error("can't found player info, playerId->" + userId + ",tableInfo:" + TableUtil.toStringNormal(table));
                ack.setResult(-1).setResultMsg(ErrorCodeEnum.GAME_50050_2.getCode());
                ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.build(), header));
                return;
            }
            if(!player.getBetMultipleAry().contains(betBtn)){
                log.error(" playerId->" + userId + ",tableInfo:" + TableUtil.toStringNormal(table)
                +"betMultipleAry:"+player.getBetMultipleAry()+",betBtn:"+betBtn);
                //默认1倍
                betScore = 1;
                ack.setBetScore(betScore);
            }

            if (player.getSeatNum() <= 0) {
                log.error("player have no seat,can't bet. playerId->" + userId + ",tableInfo:" + TableUtil.toStringNormal(table));
                ack.setResult(-2).setResultMsg(ErrorCodeEnum.GAME_50014_1.getCode());
                ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.build(), header));
                return;
            }

            if (player.getState() == PlayerStateEnum.already_bet){
                log.error("Player= {} have beted ,Do not bet again ",userId);
                return;
            }

            String currGameOrderId = table.getCurrGameOrderId();
            long baseBetScore = table.getRoomConfig().getAnte();

            //如果gameOrderId不一致，那么返回错误
            if (!gameOrderId.equals(currGameOrderId)) {
                ack.setResult(-13);
                log.error("GameOrderId不一致, req gameOrderId->" + gameOrderId + ", currGameOrderId->" + currGameOrderId + ",tableInfo:" + TableUtil.toStringNormal(table));
                //ack.setResultMsg(ErrorCodeEnum.GAME_50013_2.getCode());
                ack.setResultMsg("gameOrderId错误");
                ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.build(), header));
                return;
            }

            long total = betScore * baseBetScore;

            if (total > player.getPlayScoreStore()) {
                ack.setResult(-5);
                log.error("Player desktop score is not enough" + " -> " +
                        "desktopScore:" + player.getPlayScoreStore() + " -> betScore:" + betScore + ",tableInfo:" + TableUtil.toStringNormal(table));
                ack.setResultMsg(ErrorCodeEnum.GAME_50001_4.getCode());
                ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.build(), header));
                return;
            }

            player.setState(PlayerStateEnum.already_bet);
            player.setBetMultiple((int) betScore);
            ack.setUserId(userId);
            ack.setGameOrderId(gameOrderId);
            ack.setResult(1);

            //判断是否都下完注
            if (table.isActionOver(PlayerStateEnum.already_bet)) {
                //启动开牌倒计时
                log.debug("给牌操作即将开始tmp");
                GameLogic.giveCards(table);
            }

            ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.build(), header));

            table.setLastActionTime(System.currentTimeMillis());

            TableLogUtil.bet(functionId, "bet", userId, player.getNickName(),
                    table.getPlayType() + "", table.getRoomId(), table.getTableId(), table.getCurrGameOrderId(),
                    0, 0, betScore, player.getPlayScoreStore());

            //广播玩家下注
            try {
                NoticeBroadcastMessages.betRoundDoBet(table, player);
            } catch (Exception ex) {
                log.error("SendNotice ERROR：", ex);
            }
            long timeMillis = System.currentTimeMillis() - time;
            if (timeMillis > GameConst.COST_TIME) {
                LoggerUtils.performance.info("ApplyBetReq_50005,cost time:{},req:{}", timeMillis, req.toString());
            }
        } catch (Exception ex) {
            ack.setResult(-10).setResultMsg(ErrorCodeEnum.GAME_50002_2.getCode());
            ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.build(), header));
            log.error(ex.getMessage(), ex);
        }
    }
}
