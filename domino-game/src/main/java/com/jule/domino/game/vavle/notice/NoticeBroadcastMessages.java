package com.jule.domino.game.vavle.notice;

import JoloProtobuf.GameSvr.JoloGame;
import com.google.common.primitives.Ints;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.enums.RoleType;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.play.TableUtil;
import com.jule.domino.game.service.LogService;
import com.jule.domino.game.service.NoticePlatformSerivce;
import com.jule.domino.game.service.holder.FunctionIdHolder;
import com.jule.domino.game.utils.CardComparator;
import com.jule.domino.game.utils.NumUtils;
import com.jule.domino.game.utils.log.TableLogUtil;
import com.jule.domino.log.service.LogReasons;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.*;

/**
 * 广播通知处理类
 */
@Slf4j
public class NoticeBroadcastMessages {

    private static void winLoseList(AbstractTable table,
                                    List<JoloGame.JoloGame_TablePlay_PlayerSettleInfo> winList,
                                    List<JoloGame.JoloGame_TablePlay_PlayerSettleInfo> loseList,
                                    StringBuilder strLog) {
        try {
            Map<String, Integer> winners = CardComparator.OBJ.getWinner(table.getInGamePlayers());
            List<String> winnerList = new ArrayList<>();

            double canLoseScore = 0;
            double virtaulCanLoseScore = 0;
            //总倍数
            int winDoubleMul = 0;
            int loseDoubleMul = 0;
            for (String k : winners.keySet()) {
                winDoubleMul += winners.get(k);
                winnerList.add(k);
            }

            //计算输家筹码
            Map<Integer, Double> canLoseMap = new HashMap<>();
            Map<String, Double> loserMap = new HashMap<>();
            for (PlayerInfo player : table.getInGamePlayers().values()) {
                strLog.append("{seat:" + player.getSeatNum() + ",");
                strLog.append("userId:" + player.getPlayerId() + ",");
                strLog.append("nickName:" + player.getNickName() + ",");
                strLog.append("winLoseScore:" + player.getCurScore() + ",");
                strLog.append("robMultiple:" + player.getMultiple() + ",");
                strLog.append("betMultiple:" + player.getBetMultiple() + ",");
                strLog.append("ant:" + table.getRoomConfig().getAnte() + ",");
                log.debug(strLog.toString());
                if (winners.containsKey(player.getPlayerId())) {
                    //我是赢家
                    continue;
                }

                //玩家最多能输的钱
                double canLose = winDoubleMul * player.getBetMultiple() * table.getRoomConfig().getAnte();
                double myCanLose = canLose > player.getPlayScoreStore() ? player.getPlayScoreStore() : canLose;

                canLoseMap.put(player.getSeatNum(), myCanLose);
                loserMap.put(player.getPlayerId(), 0d);

                //牌桌上最多能输的钱
                canLoseScore += myCanLose;
                loseDoubleMul += player.getBetMultiple();
                virtaulCanLoseScore += canLose;
                log.debug("玩家{}可以输{},理论输{}", player.getPlayerId(), myCanLose, canLose);
            }

            double servicefee = 0;
            //赢家id与赢取金额
            String winner = null;
            double winScore = 0d;
            for (PlayerInfo player : table.getInGamePlayers().values()) {
                if (!winners.containsKey(player.getPlayerId())) {
                    //我是输家
                    continue;
                }
                winner = player.getPlayerId();
                //玩家理论能赢的筹码
                double canWin = NumUtils.double2Decimal(virtaulCanLoseScore * player.getBetMultiple() / winDoubleMul);
                //玩家有限责任能获得
                double dutyWin = canWin > player.getPlayScoreStore() ? player.getPlayScoreStore() : canWin;

                if (player.getPlayScoreStore() > canWin) {
                    //玩家真实获得
                    double realWin = 0d;
                    for (Integer seatNum : canLoseMap.keySet()) {
                        PlayerInfo loser = table.getInGamePlayers().get(seatNum);
                        //能输给此赢家的钱
                        double maxLose = canLoseMap.get(seatNum);
                        if (loser.getPlayScoreStore() < maxLose) {
                            maxLose = loser.getPlayScoreStore();
                        }

                        log.debug("maxLose={},BetMultiple={},winDoubleMul={}", maxLose, player.getBetMultiple(), winDoubleMul);
                        double dutyLose = NumUtils.double2Decimal(maxLose * player.getBetMultiple() / winDoubleMul);
                        realWin += dutyLose;
                        loserMap.put(loser.getPlayerId(), loserMap.get(loser.getPlayerId()) + dutyLose);

                        log.debug("GameOrderId={},play={},dutylose={},canlose={}", String.valueOf(table.getCurrGameOrderId()),
                                loser.getPlayerId(), dutyLose, canLoseMap.get(seatNum));
                    }

                    servicefee = NumUtils.double2Decimal(realWin * table.getRoomConfig().getServiceChargeRate());
                    double realWinScore = NumUtils.double2Decimal(realWin - servicefee);

                    log.debug("GameOrderId={},玩家{} realwin={}, 赢了{}, 产生服务费{}",
                            String.valueOf(table.getCurrGameOrderId()), realWin,
                            player.getPlayerId(), realWinScore, servicefee);
                    JoloGame.JoloGame_TablePlay_PlayerSettleInfo.Builder loseBuilder = JoloGame.JoloGame_TablePlay_PlayerSettleInfo.newBuilder();
                    //加钱
                    player.addPlayScoreStore(realWinScore, LogReasons.CommonLogReason.GAME_SETTLE);
                    NoticePlatformSerivce.OBJ.updateMoney(table, player, NoticePlatformSerivce.SETTLEMENT, realWinScore, true);

                    table.getAlreadyBet().put(player.getPlayerId(), realWinScore);

                    //构建返回消息
                    loseBuilder.setUserId(player.getPlayerId())
                            .setSeatNum(player.getSeatNum())
                            .setWinLose(1)
                            .setWinLoseScore(realWinScore)
                            .setPlayScoreStore(player.getPlayScoreStore())
                            .addAllHandCards(NumUtils.ConvertByte2IntArr(player.getCards()))
                            .setCardType(CardComparator.OBJ.isSpecialCard(player.getCards()));
                    winList.add(loseBuilder.build());
                    oprRobotPool(player, realWinScore, true);
                    winScore = realWinScore;
                    //发送结算日志
                    LogService.OBJ.sendGameSettleLog(table, servicefee, player.getPlayerId());
                } else {
                    //玩家真实获得
                    double realWin = 0d;
                    for (Integer seatNum : canLoseMap.keySet()) {
                        PlayerInfo loser = table.getInGamePlayers().get(seatNum);
                        double needlose = NumUtils.double2Decimal(dutyWin * loser.getBetMultiple() / loseDoubleMul);
                        //能输给此赢家的钱
                        double maxLose = canLoseMap.get(seatNum);
                        if (loser.getPlayScoreStore() < maxLose) {
                            maxLose = loser.getPlayScoreStore();
                        }

                        log.debug("maxLose={},BetMultiple={},winDoubleMul={}", maxLose, player.getBetMultiple(), winDoubleMul);
                        double dutyLose = NumUtils.double2Decimal(maxLose * player.getBetMultiple() / winDoubleMul);
                        if (needlose > dutyLose) {
                            realWin += dutyLose;
                            loserMap.put(loser.getPlayerId(), loserMap.get(loser.getPlayerId()) + dutyLose);
                        } else {
                            realWin += needlose;
                            loserMap.put(loser.getPlayerId(), loserMap.get(loser.getPlayerId()) + needlose);
                        }
                        log.debug("GameOrderId={},play={},needlose={},dutylose={},canlose={}", String.valueOf(table.getCurrGameOrderId()),
                                loser.getPlayerId(), needlose, dutyLose, canLoseMap.get(seatNum));
                    }

                    servicefee = NumUtils.double2Decimal(realWin * table.getRoomConfig().getServiceChargeRate());
                    double realWinScore = NumUtils.double2Decimal(realWin - servicefee);

                    log.debug("GameOrderId={},玩家{} realwin={}, 赢了{}, 产生服务费{}",
                            String.valueOf(table.getCurrGameOrderId()), realWin,
                            player.getPlayerId(), realWinScore, servicefee);
                    JoloGame.JoloGame_TablePlay_PlayerSettleInfo.Builder loseBuilder = JoloGame.JoloGame_TablePlay_PlayerSettleInfo.newBuilder();
                    //加钱
                    player.addPlayScoreStore(realWinScore, LogReasons.CommonLogReason.GAME_SETTLE);
                    NoticePlatformSerivce.OBJ.updateMoney(table, player, NoticePlatformSerivce.SETTLEMENT, realWinScore, true);

                    table.getAlreadyBet().put(player.getPlayerId(), realWinScore);

                    //构建返回消息
                    loseBuilder.setUserId(player.getPlayerId())
                            .setSeatNum(player.getSeatNum())
                            .setWinLose(1)
                            .setWinLoseScore(realWinScore)
                            .setPlayScoreStore(player.getPlayScoreStore())
                            .addAllHandCards(NumUtils.ConvertByte2IntArr(player.getCards()))
                            .setCardType(CardComparator.OBJ.isSpecialCard(player.getCards()));
                    winList.add(loseBuilder.build());
                    oprRobotPool(player, realWinScore, true);
                    winScore = realWinScore;
                    //发送结算日志
                    LogService.OBJ.sendGameSettleLog(table, servicefee, player.getPlayerId());
                }
            }

            int pct = table.getCommonConfig().getTotalLosePct();
            double totalPlayerLoseScore = 0;
            for (PlayerInfo player : table.getInGamePlayers().values()) {
                if (!loserMap.containsKey(player.getPlayerId())) {
                    //我是赢家
                    continue;
                }

                //能输掉的筹码
                double scoreTmp = loserMap.get(player.getPlayerId());
                double loseScore = scoreTmp > player.getPlayScoreStore() ? player.getPlayScoreStore() : scoreTmp;
                double playerLoseScore = NumUtils.double2Decimal(loseScore);
                totalPlayerLoseScore += playerLoseScore;

                StoredObjManager.incrByFloat(RedisConst.GAME_KILL_AMOUNT_POOL.getProfix(), Math.abs(loseScore) * pct / 100.0);
                //扣钱
                player.minusPlayScoreStore(playerLoseScore, LogReasons.CommonLogReason.GAME_SETTLE);
                NoticePlatformSerivce.OBJ.updateMoney(table, player, NoticePlatformSerivce.SETTLEMENT, playerLoseScore, false);

                log.debug("玩家{},输了{}筹码~", player.getPlayerId(), playerLoseScore);
                table.getAlreadyBet().put(player.getPlayerId(), playerLoseScore);

                //构建返回消息
                JoloGame.JoloGame_TablePlay_PlayerSettleInfo.Builder winnerBuilder = JoloGame.JoloGame_TablePlay_PlayerSettleInfo.newBuilder();
                winnerBuilder.setUserId(player.getPlayerId())
                        .setSeatNum(player.getSeatNum())
                        .setWinLose(0)
                        .setWinLoseScore(playerLoseScore)
                        .setPlayScoreStore(player.getPlayScoreStore())
                        .addAllHandCards(NumUtils.ConvertByte2IntArr(player.getCards()))
                        .setCardType(CardComparator.OBJ.isSpecialCard(player.getCards()));
                winList.add(winnerBuilder.build());

                oprRobotPool(player, playerLoseScore, false);
            }

            NoticePlatformSerivce.OBJ.records(table, servicefee, winner, winScore);

            //发送输赢日志
            LogService.OBJ.sendLoseWinLog(table, winnerList, 0, 0);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    //TODO:新玩家入桌 41001
    public static void playerJoinTable(final AbstractTable table,PlayerInfo player) {
        try {
            table.boardcastMessage(table.getTableId(),
                    JoloGame.JoloGame_Notice2Client_SitDownReq.newBuilder()
                            .setUserId(player.getPlayerId())
                            .setRoomId(table.getRoomId())
                            .setTableId(table.getTableId())
                            .setNickName(player.getNickName())
                            .setIcon(player.getIcon())
                            .setPlayScore(player.getPlayScoreStore())
                            .setSeatNum(player.getSeatNum())
                            .setSpecialFunction(player.getSpecialFunction())
                            .build(),
                    FunctionIdHolder.Game_Notice_JoinTable);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    //TODO:新玩家入座 51001
    public static void playerSitDown(final AbstractTable table, String userId) {
        try {
            PlayerInfo player = table.getPlayer(userId);
            table.boardcastMessage(table.getTableId(),
                    JoloGame.JoloGame_Notice2Client_SitDownReq.newBuilder()
                            .setUserId(player.getPlayerId())
                            .setRoomId(table.getRoomId())
                            .setTableId(table.getTableId())
                            .setNickName(player.getNickName())
                            .setPlayScore(player.getPlayScoreStore())
                            .setSeatNum(player.getSeatNum())
                            .setIcon(player.getIcon())
                            .setSpecialFunction(player.getSpecialFunction())
                            .build(),
                    FunctionIdHolder.Game_Notice_SiteDown);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    //TODO:玩家站起 51002
    public static void playerStandUp(final AbstractTable table, String userId, int seatNum) {
        PlayerInfo player = null;
        try {
            player = table.getPlayer(userId);
            table.boardcastMessage(table.getTableId(),
                    JoloGame.JoloGame_Notice2Client_StandUpReq.newBuilder()
                            .setUserId(userId)
                            .setRoomId(table.getRoomId())
                            .setTableId(table.getTableId())
                            .setNickName(player != null ? player.getNickName() : "")
                            .setSeatNum(seatNum)
                            .setInGamePlayers(table.getInGamePlayers().size())
                            .build(),
                    FunctionIdHolder.Game_Notice_StandUp);
        } catch (Exception ex) {
            log.error("发送玩家站起Notice失败，userId->{}, roomId->{}, tableId->{}, ex->{}",
                    userId, table.getRoomId(), table.getTableId(), ex.getMessage(), ex);
        }
        if (null != player) {
            player.removePlayerInfo();//清空玩家数据
        }
    }

    //准备状态1：OK 0：等待 51029

    /**
     * @param table
     * @param playerInfo 改变状态的对象
     */
    public static void readyStatus(final AbstractTable table, PlayerInfo playerInfo) {
        try {
            if (playerInfo == null) {
                return;
            }
            table.boardcastMessage(table.getTableId(),
                    JoloGame.JoloGame_Notice2Client_ReadyReq.newBuilder()
                            .setUserId(playerInfo.getPlayerId())
                            .setRoomId(table.getRoomId())
                            .setTableId(table.getTableId())
                            .setGameOrderId(table.getCurrGameOrderId())
                            .setReadyStatus(playerInfo.getState().equals(PlayerStateEnum.game_ready) ? 1 : 0)
                            .build(), FunctionIdHolder.Game_Notice2Client_ReadyType);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    //TODO：牌局开始 51004
    public static void gameStart(AbstractTable tableInfo) {
        try {
            JoloGame.JoloGame_Notice2Client_GameStartReq.Builder gameStart = JoloGame.JoloGame_Notice2Client_GameStartReq.newBuilder();
            gameStart.setRoomId(tableInfo.getRoomId())
                    .setTableId(tableInfo.getTableId())
                    .setGameOrderId(tableInfo.getCurrGameOrderId())
                    .setCountDownSec(tableInfo.getReadyCd())
                    .setCurrGameNum(tableInfo.getCurrGameNum());
            tableInfo.boardcastMessage(tableInfo.getTableId(), gameStart.build(),FunctionIdHolder.Game_Notice_GameStart);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    //TODO：通知全部客户端->发牌轮->开始 51005 牛牛
    public static void giveCardBoardcast(AbstractTable table) {
        try {
            JoloGame.JoloGame_Notice2Client_GiveCardRound_StartReq.Builder reqBuilder = JoloGame.JoloGame_Notice2Client_GiveCardRound_StartReq.newBuilder();
            reqBuilder.setRoomId(table.getRoomId())
                    .setTableId(table.getTableId())
                    .setGameOrderId(table.getCurrGameOrderId())
                    .addAllPlayerInfoList(TableUtil.getPlayers(table));
            table.boardcastMessage(table.getTableId(), reqBuilder.build(), FunctionIdHolder.Game_Notice_GiveCardRound_Start);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    //TODO：通知全部客户端->发牌轮->开始 51005 港式五张
    public static void giveCardBoardcast(AbstractTable table, boolean isGameStart, int firstGiveCard) {
        try {
            for (PlayerInfo player : table.getAllPlayers().values()) {
                //每轮发牌重置玩家本轮的下注方式以及下注积分
                player.setRoundScore(0);
                player.setBetMode(0);
                player.setBetScore(0);
                String playerId = player.getPlayerId();
                JoloGame.JoloGame_Notice2Client_GiveCardRound_StartReq.Builder reqBuilder = JoloGame.JoloGame_Notice2Client_GiveCardRound_StartReq.newBuilder();
                reqBuilder.setRoomId(table.getRoomId())
                        .setTableId(table.getTableId())
                        .setGameOrderId(table.getCurrGameOrderId())
                        .setCurrGameNum(table.getCurrGameNum())
                        .setIsGameStart(isGameStart ? 1 : 2)
                        .setTotalTableScore(table.getTableAlreadyBetScore())
                        .setFirstGiveCardSeatNum(firstGiveCard);
                reqBuilder.addAllPlayerInfoList(TableUtil.getPlayersInTable(table, playerId));
                //推给单个人的广播
                table.boardcastMessageSingle(playerId, reqBuilder.build(), FunctionIdHolder.Game_Notice_GiveCardRound_Start);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    //TODO:通知全部客户端->下注轮次->玩家实施下注 51007
    public static void betRoundDoBet(AbstractTable table, PlayerInfo doBetPlayer, PlayerInfo nextActionPlayerInfo, int betScore, int betMode) {
        try {
            JoloGame.JoloGame_Notice2Client_BetRound_DoBetReq.Builder reqBuilder = JoloGame.JoloGame_Notice2Client_BetRound_DoBetReq.newBuilder();
            reqBuilder.setRoomId(table.getRoomId())
                    .setTableId(table.getTableId())
                    .setGameOrderId(table.getCurrGameOrderId())
                    .setBetUserId(doBetPlayer.getPlayerId())
                    .setBetUserSeatNum(doBetPlayer.getSeatNum())
                    .setBetScore(betScore)
                    .setBetMode(betMode)
                    .setTableTotalBetScore(table.getTableAlreadyBetScore());
            if (nextActionPlayerInfo != null) {
                log.info("下一个操作玩家：{}",nextActionPlayerInfo.toSitDownString());
                reqBuilder.setNextActionPlayerInfo(nextActionPlayerInfo(nextActionPlayerInfo, table, false));
            }
            table.boardcastMessage(table.getTableId(), reqBuilder.build(), FunctionIdHolder.Game_Notice_BetRound_DoBet);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    //TODO:通知全部客户端->玩家已亮牌 51008
    public static void playerOpenCard(AbstractTable table, PlayerInfo player) {
        JoloGame.JoloGame_Notice2Client_PlayerSeeCardReq.Builder builder = JoloGame.JoloGame_Notice2Client_PlayerSeeCardReq.newBuilder();
        builder.setRoomId(table.getRoomId())
                .setTableId(table.getTableId())
                .setGameOrderId(table.getCurrGameOrderId())
                .setUserId(player.getPlayerId())
                .setSeatNum(player.getSeatNum())
                .setPlayerInfo(nextActionPlayerInfo(player, table, true));
        table.boardcastMessage(table.getTableId(), builder.build(), FunctionIdHolder.Game_Notice_OpenCard);
    }

    //通知全部客户端->广播亮牌倒计时 51032
    public static void openCardCd(AbstractTable table) {
        table.boardcastMessage(table.getTableId(),
                JoloGame.JoloGame_Notice2Client_BetInfoReq.newBuilder()
                        .setRoomId(table.getRoomId())
                        .setTableId(table.getTableId())
                        .setGameOrderId(table.getCurrGameOrderId())
                        .setCountDownSec(table.getOpenCardCd()).build(),
                FunctionIdHolder.Game_Notice_OpenCardCd);
    }

    //倒计时广播 51034
    public static void  countdownBroadcast(AbstractTable table,int time) {
        table.boardcastMessage(table.getTableId(),
                JoloGame.JoloGame_Notice2Client_BetInfoReq.newBuilder()
                        .setRoomId(table.getRoomId())
                        .setTableId(table.getTableId())
                        .setGameOrderId(table.getCurrGameOrderId())
                        .setCountDownSec(time).build(),
                FunctionIdHolder.Game_Notice_BetCd);
    }

    //倒计时广播 51036
    public static void  robDealerCd(AbstractTable table) {
        table.boardcastMessage(table.getTableId(),
                JoloGame.JoloGame_Notice2Client_BetInfoReq.newBuilder()
                        .setRoomId(table.getRoomId())
                        .setTableId(table.getTableId())
                        .setGameOrderId(table.getCurrGameOrderId())
                        .setCountDownSec(table.getBankerCd()).build(),
                FunctionIdHolder.Game_Notice_RobDealerCd);
    }
    /**
     * 广播所有玩家下注结果
     *
     * @param table
     */
    public static void allPlayerBetResultBroadcast(AbstractTable table) {
        try {
            JoloGame.JoloGame_Notice2Client_BetResultReq.Builder notice
                    = JoloGame.JoloGame_Notice2Client_BetResultReq.newBuilder();

            List<JoloGame.JoloGame_BetResultInfo> list = new ArrayList<>();

            Iterator<PlayerInfo> iter = table.getInGamePlayers().values().iterator();
            while (iter.hasNext()) {
                PlayerInfo playerInfo = iter.next();
                if (playerInfo.getIsDealer() == 1) {
                    continue;
                }
                playerInfo.setIsBlind(1);
                JoloGame.JoloGame_BetResultInfo.Builder betResultInfo = JoloGame.JoloGame_BetResultInfo.newBuilder();
                betResultInfo.setBetScore(playerInfo.getBetMultiple())//table.getRoomConfig().getAnte() * playerInfo.getBetMultiple())
                        .setSeatNum(playerInfo.getSeatNum())
                        .setUserId(playerInfo.getPlayerId());
                list.add(betResultInfo.build());
            }
            notice.setGameOrderId(table.getCurrGameOrderId())
                    .setRoomId(table.getRoomId())
                    .setTableId(table.getTableId())
                    .addAllBetResultList(list);
            table.boardcastMessage(table.getTableId(),
                    notice.build(), FunctionIdHolder.Game_Notice2Client_BetResultType);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }


    /**
     * 下注前广播的信息
     *
     * @param table
     */
    public static void betBeforeBroadcast(final AbstractTable table) {
        try {
            JoloGame.JoloGame_Notice2Client_BetInfoReq.Builder
                    notice = JoloGame.JoloGame_Notice2Client_BetInfoReq.newBuilder();
            notice.setRoomId(table.getRoomId())
                    .setTableId(table.getTableId())
                    .setGameOrderId(table.getCurrGameOrderId())
                    .setCountDownSec(table.getBetCd());
            table.boardcastMessage(table.getTableId(), notice.build(), FunctionIdHolder.Game_Notice2Client_BetInfoType);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

//    /**
//     * 广播下注倍数
//     *
//     * @param table
//     */
//    public static void betMultiple(AbstractTable table) {
//        JoloGame.JoloGame_Notice2Client_BetMultipleInfoReq.Builder betMultipleInfo =
//                JoloGame.JoloGame_Notice2Client_BetMultipleInfoReq.newBuilder();
//        betMultipleInfo.setGameOrderId(table.getCurrGameOrderId())
//                .setRoomId(table.getRoomId())
//                .setTableId(table.getTableId())
//                .addAllBtnsMultiple();
//        Iterator<PlayerInfo> iter = table.getInGamePlayers().values().iterator();
//        while (iter.hasNext()) {
//            PlayerInfo playerInfo = iter.next();
//            table.boardcastMessageSingle(playerInfo.getPlayerId(), betMultipleInfo.build(),
//                    FunctionIdHolder.Game_Notice2Client_BetMultipleInfoType);
//        }
//
//    }
    /**
     * 发牌
     *
     * @param table
     */
    public static void pressCardBroadcast(AbstractTable table) {
        try {
            JoloGame.JoloGame_Notice2Client_HandCardsListReq.Builder
                    notice = JoloGame.JoloGame_Notice2Client_HandCardsListReq.newBuilder();
            notice.setCountDownSec(table.getCommonConfig().getOpenCardsCD())
                    .setGameOrderId(table.getCurrGameOrderId())
                    .setRoomId(table.getRoomId())
                    .setTableId(table.getTableId());
            List<JoloGame.JoloGame_HandCardsInfo> handCardsInfos = new ArrayList<>();
            notice.addAllHandCardsList(handCardsInfos);
            table.boardcastMessage(table.getTableId(),notice.build(), FunctionIdHolder.Game_Notice2Client_HandCardListType);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }


    //TODO:通知全部客户端->结算动画 51013
    public static void settleAnimationBroadcast(AbstractTable table,List<JoloGame.JoloGame_TablePlay_PlayerSettleInfo> settleInfoList) {
        try {
            JoloGame.JoloGame_Notice2Client_SettleRound_SettleReq.Builder reqBuilder = JoloGame.JoloGame_Notice2Client_SettleRound_SettleReq.newBuilder();
            reqBuilder.setRoomId(table.getRoomId())
                    .setTableId(table.getTableId())
                    .setGameOrderId(table.getCurrGameOrderId())
                    .addAllSettleInfoList(settleInfoList);
            table.boardcastMessage(table.getTableId(), reqBuilder.build(), FunctionIdHolder.Game_Notice_SettleRound_SettleResult);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    //单独通知：通知玩家更新货币 51055
    public static void useItem(AbstractTable table,String userId,int headSculpture,int cardSkin) {
        table.boardcastMessage(table.getTableId(),
                JoloGame.JoloGame_Notice2Client_SkinReq.newBuilder()
                        .setUserId(userId)
                        .setHeadSculpture(headSculpture)
                        .setCardSkin(cardSkin).build(),
                FunctionIdHolder.Game_Notice2Client_Item);
    }

    //通知全部客户端->betRound->用户弃牌 51017
    public static void applyFold(AbstractTable table, PlayerInfo applyPlayer, PlayerInfo nextBetPlayer) {
        JoloGame.JoloGame_Notice2Client_BetRound_FoldReq.Builder reqBuilder = JoloGame.JoloGame_Notice2Client_BetRound_FoldReq.newBuilder();
        reqBuilder.setRoomId(table.getRoomId())
                .setTableId(table.getTableId())
                .setApplyUserId(applyPlayer == null ? table.getOfflinePlayerId() : applyPlayer.getPlayerId())
                .setApplyUserSeatNum(applyPlayer == null ? table.getOfflineSeatNum() : applyPlayer.getSeatNum())
                .setInGamePlayers(table.getInGamePlayers().size())
                .setGameOrderId(table.getCurrGameOrderId());
        if (nextBetPlayer != null) {
            reqBuilder.setNextActionPlayerInfo(nextActionPlayerInfo(nextBetPlayer, table, false));
        }
        table.boardcastMessage(table.getTableId(), reqBuilder.build(), FunctionIdHolder.Game_Notice_BetRound_Fold);
    }

    /**
     * 发送玩家进入消息 51039
     *
     * @param table
     */
    public static void sendPlayerInNotice(AbstractTable table) {
        try {
            JoloGame.JoloGame_Notice2Client_PlayersIn.Builder
                    notice = JoloGame.JoloGame_Notice2Client_PlayersIn.newBuilder();

            notice.setGameOrderId(table.getCurrGameOrderId())
                    .setGameId(String.valueOf(table.getPlayType()))
                    .setRoomId(table.getRoomId())
                    .setTableId(table.getTableId());

            table.boardcastMessage(table.getTableId(),
                    notice.build(), FunctionIdHolder.Game_Notice2Client_PlayersIn);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * 玩家离桌广播 51023
     *
     * @param table
     * @param player
     */
    public static void sendPlayerLeaveNotice(AbstractTable table, PlayerInfo player) {
        try {
            table.boardcastMessage(table.getTableId(),
                    JoloGame.JoloGame_Notice2Client_leaveReq.newBuilder()
                            .setTableId(table.getTableId())
                            .setRoomId(table.getRoomId())
                            .setUserId(player.getPlayerId())
                            .setIsLeaveTable(player.getTotalAlreadyBetScore4Hand() == 0 ? 1 : 2).build(),
                    FunctionIdHolder.Game_Notice2Client_leavel);
        } catch (Exception ex) {
            log.error("发送消息失败 exception={}", ex);
        }
    }

    /**
     * 玩家聊天广播  51052
     */
    public static void chatMesgSend(AbstractTable table,String userId,int chatType,int chatId,String targeId) {
        try {
            table.boardcastMessage(table.getTableId(),
                    JoloGame.JoloGame_ChatMesgSend.newBuilder()
                            .setRoomId(table.getRoomId())
                            .setTableId(table.getTableId())
                            .setUserId(userId)
                            .setChatType(chatType)
                            .setChatId(chatId)
                            .setTargetId(targeId).build(),
                    FunctionIdHolder.Game_Notice_ChatMesgSend);
        } catch (Exception ex) {
            log.error("发送消息失败 exception={}", ex);
        }
    }

    /**
     * 机器人资金池 X 操作
     *
     * @param player
     * @param score
     */
    private static void oprRobotPool(PlayerInfo player, double score, boolean win) {
        if (player == null || player.getRoleType() != RoleType.ROBOT) {
            return;
        }

        double incr = score;
        if (!win) {
            incr = -score;
        }

        StoredObjManager.incrByFloat(RedisConst.ROBOT_POOL_CURRENT_MONEY.getProfix(), incr);
    }

    private static JoloGame.JoloGame_TablePlay_PlayerInfo nextActionPlayerInfo(PlayerInfo player, AbstractTable table, Boolean isOpenCard) {
        //玩家该跟注多少
        int maxBetScore = table.getEqualScore().size() == 0 ? 0 : Collections.max(table.getEqualScore()) - (int) player.getWinLoseScore4Hand();
        return JoloGame.JoloGame_TablePlay_PlayerInfo.newBuilder()
                .setUserId(player.getPlayerId())
                .setNickName(player.getNickName())
                .setPlayScoreStore(player.getPlayScoreStore())
                .setSeatNum(player.getSeatNum())
                .setIsCurrAction(player.getIsCurrActive())
                .setCurrActionSurplusTime(table.getBetCd())
                .addAllHandCards(isOpenCard ? Ints.asList(player.getHandCards()) : new ArrayList<>())
                .setIsDealer(0)
                .setState(player.getState().getValue())
                .setIsBlind(0)
                .setPreviousBetScore(maxBetScore).build();
    }

    //推给vip单人的特殊功能提示(开启或关闭)
    public static void sendSpecialFunctionMsg(String playerId,AbstractTable table,String msg){
        table.boardcastMessageSingle(playerId,
                JoloGame.JoloGame_Notice2Client_RobMultipleInfoReq.newBuilder()
                        .setRoomId(table.getRoomId())
                        .setTableId(table.getTableId())
                        .setGameOrderId(msg).build(),
                FunctionIdHolder.Game_SpecialFunction_Msg);
    }

    //广播抢庄倍数 51027
    public static void robMultiple(AbstractTable table,PlayerInfo player) {
        table.boardcastMessage(table.getTableId(),
                JoloGame.JoloGame_Notice2Client_RobDealerReq.newBuilder()
                        .setUserId(player.getPlayerId())
                        .setRoomId(table.getRoomId())
                        .setTableId(table.getTableId())
                        .setGameOrderId(table.getCurrGameOrderId())
                        .setMultiple(player.getMultiple()).build(),
                FunctionIdHolder.Game_Notice2Client_RobDealerType);
    }

    //广播庄家是谁 51082
    public static void dealerFinish(AbstractTable table) {
        try {
            PlayerInfo player = table.getPlayer(table.getCurrDealerPlayerId());
            table.boardcastMessage(table.getTableId(),
                    JoloGame.JoloGame_Fix_Dealer_FinishReq.newBuilder()
                            .setUserId(player.getPlayerId())
                            .setRoomId(table.getRoomId())
                            .setTableId(table.getTableId())
                            .setSeatNum(player.getSeatNum()).build(),
                    FunctionIdHolder.Game_Notice2Client_FixDealerType);
        } catch (Exception ex) {
            log.error("发送消息失败 exception={}", ex);
        }
    }
}
