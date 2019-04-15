package com.jule.domino.game.vavle.notice;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.enums.RoleType;
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
            for (String k: winners.keySet()){
                winDoubleMul += winners.get(k);
                winnerList.add(k);
            }

            //计算输家筹码
            Map<Integer,Double> canLoseMap = new HashMap<>();
            Map<String,Double> loserMap = new HashMap<>();
            for (PlayerInfo player : table.getInGamePlayers().values()) {
                strLog.append("{seat:" + player.getSeatNum() + ",");
                strLog.append("userId:" + player.getPlayerId() + ",");
                strLog.append("nickName:" + player.getNickName() + ",");
                strLog.append("winLoseScore:" + player.getCurScore() + ",");
                strLog.append("robMultiple:" + player.getMultiple() + ",");
                strLog.append("betMultiple:" + player.getBetMultiple() + ",");
                strLog.append("ant:" + table.getRoomConfig().getAnte() + ",");
                log.debug(strLog.toString());
                if (winners.containsKey(player.getPlayerId())){
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
                log.debug("玩家{}可以输{},理论输{}",player.getPlayerId(),myCanLose,canLose);
            }

            double servicefee = 0;
            //赢家id与赢取金额
            String winner = null;
            double winScore = 0d;
            for (PlayerInfo player : table.getInGamePlayers().values()) {
                if (!winners.containsKey(player.getPlayerId())){
                    //我是输家
                    continue;
                }
                winner = player.getPlayerId();
                //玩家理论能赢的筹码
                double canWin = NumUtils.double2Decimal(virtaulCanLoseScore * player.getBetMultiple() /winDoubleMul);
                //玩家有限责任能获得
                double dutyWin = canWin > player.getPlayScoreStore() ? player.getPlayScoreStore() : canWin;

                if (player.getPlayScoreStore() > canWin){
                    //玩家真实获得
                    double realWin = 0d;
                    for (Integer seatNum : canLoseMap.keySet()){
                        PlayerInfo loser = table.getInGamePlayers().get(seatNum);
                        //能输给此赢家的钱
                        double maxLose = canLoseMap.get(seatNum);
                        if (loser.getPlayScoreStore() < maxLose){
                            maxLose = loser.getPlayScoreStore();
                        }

                        log.debug("maxLose={},BetMultiple={},winDoubleMul={}",maxLose,player.getBetMultiple(),winDoubleMul);
                        double dutyLose = NumUtils.double2Decimal(maxLose * player.getBetMultiple()/winDoubleMul);
                        realWin += dutyLose;
                        loserMap.put(loser.getPlayerId(), loserMap.get(loser.getPlayerId())+dutyLose);

                        log.debug("GameOrderId={},play={},dutylose={},canlose={}",String.valueOf(table.getCurrGameOrderId()),
                                loser.getPlayerId(),dutyLose,canLoseMap.get(seatNum));
                    }

                    servicefee = NumUtils.double2Decimal(realWin * table.getRoomConfig().getServiceChargeRate());
                    double realWinScore = NumUtils.double2Decimal(realWin - servicefee);

                    log.debug("GameOrderId={},玩家{} realwin={}, 赢了{}, 产生服务费{}",
                            String.valueOf(table.getCurrGameOrderId()),realWin,
                            player.getPlayerId(), realWinScore, servicefee);
                    JoloGame.JoloGame_TablePlay_PlayerSettleInfo.Builder loseBuilder = JoloGame.JoloGame_TablePlay_PlayerSettleInfo.newBuilder();
                    //加钱
                    player.addPlayScoreStore(realWinScore, LogReasons.CommonLogReason.GAME_SETTLE);
                    NoticePlatformSerivce.OBJ.updateMoney(table, player, NoticePlatformSerivce.SETTLEMENT, realWinScore,true);

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
                }else {
                    //玩家真实获得
                    double realWin = 0d;
                    for (Integer seatNum : canLoseMap.keySet()){
                        PlayerInfo loser = table.getInGamePlayers().get(seatNum);
                        double needlose = NumUtils.double2Decimal(dutyWin * loser.getBetMultiple()/loseDoubleMul);
                        //能输给此赢家的钱
                        double maxLose = canLoseMap.get(seatNum);
                        if (loser.getPlayScoreStore() < maxLose){
                            maxLose = loser.getPlayScoreStore();
                        }

                        log.debug("maxLose={},BetMultiple={},winDoubleMul={}",maxLose,player.getBetMultiple(),winDoubleMul);
                        double dutyLose = NumUtils.double2Decimal(maxLose * player.getBetMultiple()/winDoubleMul);
                        if (needlose > dutyLose){
                            realWin += dutyLose;
                            loserMap.put(loser.getPlayerId(), loserMap.get(loser.getPlayerId())+dutyLose);
                        }else {
                            realWin += needlose;
                            loserMap.put(loser.getPlayerId(), loserMap.get(loser.getPlayerId())+needlose);
                        }
                        log.debug("GameOrderId={},play={},needlose={},dutylose={},canlose={}",String.valueOf(table.getCurrGameOrderId()),
                                loser.getPlayerId(),needlose,dutyLose,canLoseMap.get(seatNum));
                    }

                    servicefee = NumUtils.double2Decimal(realWin * table.getRoomConfig().getServiceChargeRate());
                    double realWinScore = NumUtils.double2Decimal(realWin - servicefee);

                    log.debug("GameOrderId={},玩家{} realwin={}, 赢了{}, 产生服务费{}",
                            String.valueOf(table.getCurrGameOrderId()),realWin,
                            player.getPlayerId(), realWinScore, servicefee);
                    JoloGame.JoloGame_TablePlay_PlayerSettleInfo.Builder loseBuilder = JoloGame.JoloGame_TablePlay_PlayerSettleInfo.newBuilder();
                    //加钱
                    player.addPlayScoreStore(realWinScore, LogReasons.CommonLogReason.GAME_SETTLE);
                    NoticePlatformSerivce.OBJ.updateMoney(table, player, NoticePlatformSerivce.SETTLEMENT, realWinScore,true);

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
                if (!loserMap.containsKey(player.getPlayerId())){
                    //我是赢家
                    continue;
                }

                //能输掉的筹码
                double scoreTmp = loserMap.get(player.getPlayerId());
                double loseScore = scoreTmp > player.getPlayScoreStore() ? player.getPlayScoreStore(): scoreTmp;
                double playerLoseScore = NumUtils.double2Decimal(loseScore);
                totalPlayerLoseScore += playerLoseScore;

                StoredObjManager.incrByFloat(RedisConst.GAME_KILL_AMOUNT_POOL.getProfix(), Math.abs(loseScore) * pct/100.0);
                //扣钱
                player.minusPlayScoreStore(playerLoseScore , LogReasons.CommonLogReason.GAME_SETTLE);
                NoticePlatformSerivce.OBJ.updateMoney(table, player, NoticePlatformSerivce.SETTLEMENT, playerLoseScore,false);

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

            NoticePlatformSerivce.OBJ.records(table,servicefee,winner,winScore);

            //发送输赢日志
            LogService.OBJ.sendLoseWinLog(table, winnerList,0, 0);
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

    //TODO:准备状态1：OK 0：等待

    /**
     * @param table
     * @param playerInfo 改变状态的对象
     */
    public static void readyStatus(final AbstractTable table, PlayerInfo playerInfo) {
        try {
            if (playerInfo == null) {
                return;
            }
            table.boardcastMessage(table.getTableId(), JoloGame.JoloGame_Notice2Client_ReadyReq.newBuilder()
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
            JoloGame.JoloGame_Notice2Client_GameStartReq.Builder gameStart =
                    JoloGame.JoloGame_Notice2Client_GameStartReq.newBuilder();
            gameStart.setRoomId(tableInfo.getRoomId())
                    .setTableId(tableInfo.getTableId())
                    .setGameOrderId(tableInfo.getCurrGameOrderId())
                    .setCountDownSec(tableInfo.getCommonConfig().getGameStartCountDownSec());

            tableInfo.boardcastMessage(tableInfo.getTableId(), gameStart.build(),
                    FunctionIdHolder.Game_Notice_GameStart);
            TableLogUtil.gameStart(FunctionIdHolder.Game_Notice_GameStart, "gameStart",
                    "" + tableInfo.getPlayType(), tableInfo.getRoomId(), tableInfo.getTableId(),
                    tableInfo.getCurrGameOrderId(), TableUtil.inGamePlayersBySeatNumToString(tableInfo), 0,
                    TableUtil.tableInfoToString(tableInfo));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    //TODO：通知全部客户端->发牌轮->开始 51005
    public static void giveCardBoardcast(AbstractTable table) {
        try {
            JoloGame.JoloGame_Notice2Client_GiveCardRound_StartReq.Builder reqBuilder = JoloGame.JoloGame_Notice2Client_GiveCardRound_StartReq.newBuilder();
            reqBuilder.setRoomId(table.getRoomId())
                    .setTableId(table.getTableId())
                    .setGameOrderId(table.getCurrGameOrderId())
                    .addAllPlayerInfoList(TableUtil.getPlayersInTable(table));
            table.boardcastMessage(table.getTableId(), reqBuilder.build(), FunctionIdHolder.Game_Notice_GiveCardRound_Start);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }


    //TODO:通知全部客户端->下注轮次->玩家实施下注 51007
    public static void betRoundDoBet(AbstractTable table, PlayerInfo doBetPlayer) {
        try {
            JoloGame.JoloGame_Notice2Client_BetRound_DoBetReq.Builder reqBuilder = JoloGame.JoloGame_Notice2Client_BetRound_DoBetReq.newBuilder();
            reqBuilder.setRoomId(table.getRoomId())
                    .setTableId(table.getTableId())
                    .setGameOrderId(table.getCurrGameOrderId())
                    .setBetUserId(doBetPlayer.getPlayerId())
                    .setBetUserSeatNum(doBetPlayer.getSeatNum())
                    .setBetScore(doBetPlayer.getBetMultiple());

            table.boardcastMessage(table.getTableId(),
                    reqBuilder.build(),
                    FunctionIdHolder.Game_Notice_BetRound_DoBet);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
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
                if(playerInfo.getIsDealer()==1){
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
                    .setCountDownSec(table.getCommonConfig().getBetCountDownSec());

            //table.boardcastMessage(table.getTableId(), notice.build(), FunctionIdHolder.Game_Notice2Client_BetInfoType);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * 广播下注倍数
     * @param table
     */
    public static void betMultiple(AbstractTable table) {
        JoloGame.JoloGame_Notice2Client_BetMultipleInfoReq.Builder betMultipleInfo =
                JoloGame.JoloGame_Notice2Client_BetMultipleInfoReq.newBuilder();
        betMultipleInfo.setGameOrderId(table.getCurrGameOrderId())
                .setRoomId(table.getRoomId())
                .setTableId(table.getTableId())
                .addAllBtnsMultiple(table.getRoomConfig().getDoubleList());

        Iterator<PlayerInfo> iter = table.getInGamePlayers().values().iterator();
        while (iter.hasNext()) {
            PlayerInfo playerInfo = iter.next();

            table.boardcastMessageSingle(playerInfo.getPlayerId(), betMultipleInfo.build(),
                    FunctionIdHolder.Game_Notice2Client_BetMultipleInfoType);
        }

    }


    private static void fillHandCardsInfo(AbstractTable table, List<JoloGame.JoloGame_HandCardsInfo> handCardsInfos) {
        try {
            //判断策略值
            int initKillPool = Integer.parseInt(StoredObjManager.get(RedisConst.ROBOT_INIT_KILL_POOL.getProfix()));
            int initRobotPool = Integer.parseInt(StoredObjManager.get(RedisConst.ROBOT_INIT_ROBOT_POOL.getProfix()));;

            double x = Double.parseDouble(StoredObjManager.get(RedisConst.ROBOT_POOL_CURRENT_MONEY.getProfix())) + initRobotPool;

            double z = Double.parseDouble(StoredObjManager.get(RedisConst.GAME_KILL_AMOUNT_POOL.getProfix())) + initKillPool;

            List<PlayerInfo> robots = new ArrayList<>();

            double max = table.getCommonConfig().getStrategyMaxPct();
            double min = table.getCommonConfig().getStrategyMinPct();

            log.debug("AI配置initKillPool={}，initRobotPool={}",initKillPool,initRobotPool);
            log.debug("AI机器人策略比值x={},z={}", String.valueOf(x), String.valueOf(z));
            /*if (x > max * z ){
                //机器人输
                log.debug("AI机器人策略 x > {}*z ,机器人应该输", max);
                doExchangeCard(false,robots,table);
            }else */if (x < min * z){
                //机器人赢
                log.debug("AI机器人策略 x < {}*z ,机器人应该赢", min);
                doExchangeCard(true,robots,table);
            }else {
                log.debug("AI机器人策略正常发牌、不做操作");
            }
        }catch (Exception ex ){
            log.error("机器人验证异常 ex = {}",ex);
        }


        Iterator<PlayerInfo> iter = table.getInGamePlayers().values().iterator();
        while (iter.hasNext()) {
            PlayerInfo playerInfo = iter.next();
            List<Integer> cards = NumUtils.ConvertByte2IntArr(playerInfo.getCards());
            JoloGame.JoloGame_HandCardsInfo.Builder handCards = JoloGame.JoloGame_HandCardsInfo.newBuilder();
            handCards.setUserId(playerInfo.getPlayerId())
                    .addAllHandCards(cards)
                    .setCardType(playerInfo.getType());
            handCardsInfos.add(handCards.build());
            playerInfo.setIsBlind(2);
        }
    }

    private static void doExchangeCard(boolean robotWin, List<PlayerInfo> robots, AbstractTable table){
        Map<String,Integer> winners = CardComparator.OBJ.getWinner(table.getInGamePlayers());

        //牌型列表
        List<int[]> winnerCards = new ArrayList<>();
        List<int[]> loserCards = new ArrayList<>();

        //玩家列表
        List<PlayerInfo> players = new ArrayList<>();

        boolean needChange = false;
        for (Integer seat : table.getInGamePlayers().keySet()){
            PlayerInfo player = table.getInGamePlayers().get(seat);

            //玩家分类
            if (player.getRoleType() == RoleType.ROBOT){
                robots.add(player);
            }else {
                players.add(player);
            }

            //牌型分类
            if (winners.containsKey(player.getPlayerId())){
                //赢家牌型
                winnerCards.add(player.getHandCards());
                if (player.getRoleType() == RoleType.ROBOT ){
                    if (!robotWin){
                        needChange = true;
                    }
                }else {
                    if (robotWin){
                        needChange = true;
                    }
                }
            }else {
                //输家牌型
                loserCards.add(player.getHandCards());
            }
        }

        //不需要换牌
        if (!needChange){
            return;
        }

        //执行换牌
        List<int[]> sortedCard = new ArrayList<>();
        sortedCard.addAll(winnerCards);
        sortedCard.addAll(loserCards);

        for (Integer seat : table.getInGamePlayers().keySet()) {
            PlayerInfo player = table.getInGamePlayers().get(seat);
            int[] arrHandCards = null;

            int index = 0;
            if (player.getRoleType() == RoleType.ROBOT ){
                if (!robotWin){
                    index = sortedCard.size()-1;
                }
            }else {
                if (robotWin){
                    index = sortedCard.size()-1;
                }
            }

            arrHandCards = sortedCard.get(index);
            sortedCard.remove(index);

            player.setHandCards(arrHandCards); //玩家看牌后则为手牌赋值
            player.setCards(NumUtils.ConvertInt2ByteArr(arrHandCards));
            player.setType(CardComparator.OBJ.isSpecialCard(arrHandCards));
        }

    }

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
            fillHandCardsInfo(table, handCardsInfos);
            notice.addAllHandCardsList(handCardsInfos);

            table.boardcastMessage(table.getTableId(),
                    notice.build(), FunctionIdHolder.Game_Notice2Client_HandCardListType);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }


    //TODO:通知全部客户端->结算动画 51013
    public static void settleAnimationBroadcast(AbstractTable table) {
        Long time = System.currentTimeMillis();
        try {
            StringBuilder strLog = new StringBuilder();
            strLog.append("[");

            List<JoloGame.JoloGame_TablePlay_PlayerSettleInfo> winList = new ArrayList<>();
            List<JoloGame.JoloGame_TablePlay_PlayerSettleInfo> loseList = new ArrayList<>();
            winLoseList(table, winList, loseList, strLog);
            JoloGame.JoloGame_Notice2Client_SettleRound_SettleReq.Builder reqBuilder = JoloGame.JoloGame_Notice2Client_SettleRound_SettleReq.newBuilder();
            reqBuilder.setRoomId(table.getRoomId())
                    .setTableId(table.getTableId())
                    .setGameOrderId(table.getCurrGameOrderId())
                    .addAllSettleWinList(winList)
                    .addAllSettleLoseList(loseList);

            table.boardcastMessage(table.getTableId(),
                    reqBuilder.build(), FunctionIdHolder.Game_Notice_SettleRound_SettleResult);


            if (strLog.indexOf(",") > 0) {
                strLog.replace(strLog.length() - 1, strLog.length(), "");
            }
            strLog.append("]");
            TableLogUtil.settle(FunctionIdHolder.Game_Notice_SettleRound_SettleResult, "settleAnimationBroadcast", table.getPlayType() + "",
                    table.getRoomId(), table.getTableId(), strLog.toString());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        long cost = System.currentTimeMillis() - time;
        if (cost > 1000) {
            log.error("settleAnimationBroadcast() 超时" + cost);
        }
    }

    /**
     * 发送玩家进入消息 51039
     * @param table
     */
    public static void sendPlayerInNotice(AbstractTable table){
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
     * 通知机器人离座、回收金币
     * @param table
     * @param player
     */
    public static void sendPlayerLeaveNotice(AbstractTable table, PlayerInfo player){
        try {
            if (player.getRoleType() != RoleType.ROBOT){
                return;
            }

            JoloGame.JoloGame_Notice2Client_leaveReq.Builder notice = JoloGame.JoloGame_Notice2Client_leaveReq.newBuilder();
            notice.setTableId(table.getTableId()).setRoomId(table.getRoomId()).setUserId(player.getPlayerId());

            //table.boardcastMessage(table.getTableId(), notice.build(), FunctionIdHolder.Game_Notice2Client_leavel);
            table.boardcastMessageSingle(player.getPlayerId(), notice.build(), FunctionIdHolder.Game_Notice2Client_leavel);
        }catch (Exception ex){
            log.error("发送消息失败 exception={}",ex);
        }
    }

    /**
     * 机器人资金池 X 操作
     * @param player
     * @param score
     */
    private static void oprRobotPool(PlayerInfo player, double score, boolean win){
        if (player == null || player.getRoleType() != RoleType.ROBOT){
            return;
        }

        double incr = score;
        if (!win){
            incr = -score;
        }

        StoredObjManager.incrByFloat(RedisConst.ROBOT_POOL_CURRENT_MONEY.getProfix(), incr);
    }
}
