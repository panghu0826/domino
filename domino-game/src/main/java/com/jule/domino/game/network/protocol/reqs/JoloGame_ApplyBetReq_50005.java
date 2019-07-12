package com.jule.domino.game.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.core.common.log.LoggerUtils;
import com.jule.domino.game.gameUtil.GameLogic;
import com.jule.domino.game.gameUtil.NNGameLogic;
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
        RabbitMqSender.me.producer(functionId, req.toString());
        this.setTable(TableService.getInstance().getTable(header.gameId + "", req.getRoomId(), req.getTableId()));
    }

    @Override
    public void processImpl() throws Exception {
//        log.debug("收到消息-> " + functionId + ", reqNum-> " + header.reqNum + ", req->" + req.toString());
        log.info("收到消息-> " + functionId + ", req->" + req.toString());
        String userId = req.getUserId();
        String roomId = req.getRoomId();
        String tableId = req.getTableId();
        String gameOrderId = req.getGameOrderId();
        int betScore = req.getBetScore();
        int betMode = req.getBetMode();

        AbstractTable table = getTable();
        JoloGame.JoloGame_ApplyBetAck.Builder ack = JoloGame.JoloGame_ApplyBetAck.newBuilder();
        ack.setUserId(userId);
        ack.setRoomId(roomId);
        ack.setTableId(tableId);
        ack.setGameOrderId(gameOrderId);
        ack.setBetScore(0);
        ack.setTotalBetScore(0);
        try {
            //记录桌子最后操作时间
            table.setLastActionTime(System.currentTimeMillis());

            if (table.getTableStateEnum().getValue() > TableStateEnum.BET.getValue()) {
                log.error("当前牌桌状态={}", table.getTableStateEnum().name());
                ack.setResult(-1).setResultMsg("桌内状态不符，不能操作");
                ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.build(), header));
                return;
            }

            PlayerInfo player = table.getPlayer(userId);
            if (null == player) {
                log.error("桌内找不到该玩家, playerId->" + userId + ",tableInfo:" + TableUtil.toStringNormal(table));
                ack.setResult(-2).setResultMsg("桌内找不到该玩家");
                ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.build(), header));
                return;
            }

            if (player.getSeatNum() <= 0) {
                log.error("玩家不再座位上，不可进行该操作：{}",player.toSitDownString());
                ack.setResult(-3).setResultMsg("玩家不再座位上，不可进行该操作");
                ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.build(), header));
                return;
            }

            if(player.getState() == PlayerStateEnum.fold){
                log.info("玩家已弃牌，不可再下注：{}",player.toSitDownString());
                ack.setResult(-4).setResultMsg("玩家已弃牌，不可再下注");
                ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.build(), header));
                return;
            }

            if(player.getState() != PlayerStateEnum.beting){
                log.info("你不是桌子当前行动者：{}",player.toSitDownString());
                ack.setResult(-5).setResultMsg("你不是桌子当前行动者");
                ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.build(), header));
                return;
            }
//            if (player.getState() != PlayerStateEnum.beting) {
//                log.error("Player= {} have beted ,Do not bet again ", userId);
//                return;
//            }

//            String currGameOrderId = table.getCurrGameOrderId();
//            long baseBetScore = table.getRoomConfig().getAnte();
//
//            //如果gameOrderId不一致，那么返回错误
//            if (!gameOrderId.equals(currGameOrderId)) {
//                ack.setResult(-13);
//                log.error("GameOrderId不一致, req gameOrderId->" + gameOrderId + ", currGameOrderId->" + currGameOrderId + ",tableInfo:" + TableUtil.toStringNormal(table));
//                //ack.setResultMsg(ErrorCodeEnum.GAME_50013_2.getCode());
//                ack.setResultMsg("gameOrderId错误");
//                ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.build(), header));
//                return;
//            }
//
//            long total = betScore * baseBetScore;

//            if (total > player.getPlayScoreStore()) {
//                ack.setResult(-5);
//                log.error("Player desktop score is not enough" + " -> " +
//                        "desktopScore:" + player.getPlayScoreStore() + " -> betScore:" + betScore + ",tableInfo:" + TableUtil.toStringNormal(table));
//                ack.setResultMsg(ErrorCodeEnum.GAME_50001_4.getCode());
//                ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.build(), header));
//                return;
//            }
            if(betMode == 2 && table.getRoundTableScore() == 0){ //如果当前桌子没人下注，但是有人点跟注(正常情况是不能点)
                return;//不让跟(容错)
            }

            if(table.getGameType() == 1) {
                if (betMode == 1) {//加注
                    int roundBasisBet = table.getRoundTableScore();
                    betScore += roundBasisBet - player.getRoundScore();
                    if (player.getWinLoseScore4Hand() + betScore > table.getBetMaxScore()) {
                        ack.setBetScore(0);
                        ack.setTotalBetScore((int) player.getWinLoseScore4Hand());
                        ack.setResult(-4).setResultMsg("玩家下注额已超过桌子上限");
                        ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.build(), header));
                        return;
                    }
                    player.setRoundScore(player.getRoundScore() + betScore);
                    table.setRoundTableScore(player.getRoundScore());
                } else if (betMode == 2) {//跟注
                    int maxBetScore = Collections.max(table.getEqualScore());
                    betScore = maxBetScore - (int) player.getWinLoseScore4Hand();
                } else {//梭哈
                    betScore = table.getBetMaxScore() - (int) player.getWinLoseScore4Hand();
                    table.setRoundTableScore(betScore);
                }
                log.info("玩家下注的方式（1/加注，2/跟注，3/梭哈）：{}， --下注的积分：{}", betMode, betScore);

                player.setState(PlayerStateEnum.already_bet);
                player.setIsCurrActive(0);
                player.setBetMode(betMode);
                player.setBetScore(betScore);
                //当前一局玩家的总下注额
                player.setWinLoseScore4Hand(player.getWinLoseScore4Hand() + betScore);
                //当前桌子玩家的总下注额
                player.setTotalAlreadyBetScore4Hand(player.getTotalAlreadyBetScore4Hand() + betScore);
                //当前一局所有玩家的总下注额
                table.setTableAlreadyBetScore(table.getTableAlreadyBetScore() + betScore);
                //当前桌子所有玩家的总下注额
                table.setTableTotalAlreadyBetScore(table.getTableTotalAlreadyBetScore() + betScore);
                //下注积分放入集合里
                table.getEqualScore().clear();
                table.getInGamePlayers().values().forEach(e -> table.getEqualScore().add((int) e.getWinLoseScore4Hand()));
                log.info("桌子目前的信息：{}", table.toString());
                log.info("玩家的信息：{}", player.toSitDownString());
                //回复ack消息
                ack.setBetScore(betScore);
                ack.setTotalBetScore((int) player.getWinLoseScore4Hand());
                ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.setResult(1).build(), header));
                //寻找下一个操作者
                PlayerInfo nextActionPlayer = null;
                if (table.getEqualScore().size() != 1) {
                    nextActionPlayer = table.getNextBetPlayer(player.getSeatNum());
                }
                //广播玩家下注
                try {
                    NoticeBroadcastMessages.betRoundDoBet(table, player, nextActionPlayer, betScore, betMode);
                } catch (Exception ex) {
                    log.error("SendNotice ERROR：", ex);
                }

                log.info("当前桌子下注积分是否相等：{},  equalScore数据: {}", (table.getEqualScore().size() == 1), table.getEqualScore().toString());
                log.info("当前桌子上的手牌数：{}", player.getHandCards().length);
                if (table.getEqualScore().size() == 1) {
                    if (player.getWinLoseScore4Hand() == table.getBetMaxScore()) {
                        log.info("发完剩余 {} 张牌，并进入开牌阶段：{}", (5 - player.getHandCards().length), table.toString());
                        //每个人每张牌0.4秒，四舍五入
                        double cards = (5 - player.getHandCards().length) * 0.4;
                        int time = (int) Math.round(cards * table.getInGamePlayers().size());
                        table.pressCard(false, true);//发出所有牌
                        int firstGiveCard = table.getFirstGiveCardSeatNum();
                        //广播发牌51005
                        NoticeBroadcastMessages.giveCardBoardcast(table, false, firstGiveCard);
                        GameLogic.commonTimer(table, 2, time);
                    } else if (player.getHandCards().length == 5) {
                        log.info("桌子进入开牌阶段：{}", table.toString());
                        NoticeBroadcastMessages.openCardCd(table);//广播开牌倒计时
                        GameLogic.openCards(table);//启动开牌倒计时
                    } else {
                        log.info("桌子进入开牌阶段：{}", table.toString());
                        //设置玩家手牌
                        table.pressCard(false, false);
                        int firstGiveCard = table.getFirstGiveCardSeatNum();
                        //寻找第一个下注玩家
                        table.lookForFirstBetPlayer();
                        table.setFirstGiveCardSeatNum(table.getCurrActionSeatNum());
                        //广播发牌51005
                        NoticeBroadcastMessages.giveCardBoardcast(table, false, firstGiveCard);
                        table.setRoundTableScore(0);//重置本轮次玩家下注额
                        int time = (int) Math.round(table.getInGamePlayers().size() * 0.4);
                        GameLogic.commonTimer(table, 1, time);
                    }
                } else {
                    log.info("普通下注或跟注：{}", table.toString());
                    //启动下一个行动玩家的bet倒计时
                    NoticeBroadcastMessages.countdownBroadcast(table, table.getBetCd());//广播倒计时
                    GameLogic.betTimer(table);
                }
            }else if(table.getGameType() == 2){
                betScore = req.getBetScore();
                player.setState(PlayerStateEnum.already_bet);
                player.setBetMultiple(betScore);
                ctx.writeAndFlush(new JoloGame_ApplyBetAck_50005(ack.setResult(1).build(), header));
                NoticeBroadcastMessages.betRoundDoBet(table, player, null, betScore, betMode);
                boolean flags = true;
                for (PlayerInfo playerInfo : table.getInGamePlayers().values()) {
                    if(playerInfo.getPlayerId().equals(table.getCurrDealerPlayerId()))continue;
                    if (!playerInfo.getState().equals(PlayerStateEnum.already_bet)) {
                        flags = false;
                    }
                }
                if(flags){
                    NNGameLogic.openCards(table);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            log.info("50005 ack 玩家下注：{}", ack.toString());
        }
    }
}
