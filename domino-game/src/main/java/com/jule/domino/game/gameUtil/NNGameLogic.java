package com.jule.domino.game.gameUtil;

import JoloProtobuf.GameSvr.JoloGame;
import com.google.common.primitives.Ints;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.GameRecordModel;
import com.jule.domino.game.model.NiuNiuPoker;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.network.protocol.TableInnerReq;
import com.jule.domino.game.network.protocol.logic.LeaveTableLogic;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.play.TableUtil;
import com.jule.domino.game.play.impl.NiuNiuTable;
import com.jule.domino.game.service.TableService;
import com.jule.domino.game.service.TimerService;
import com.jule.domino.game.service.holder.CardOfTableHolder;
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class NNGameLogic {
    /**
     * 游戏开始计时器
     *
     * @param table
     */
    public static void gameReady(final AbstractTable table) {
        log.info(table.getCurrGameNum() + 1 + "-----------------------------游戏准备cd--------------------------" + table.getTableId());
        table.setTableStateEnum(TableStateEnum.GAME_READY);
        table.setTableStatus();
        //停止牌桌内正在进行的倒计时
        TimerService.getInstance().delTimerTask(table.getRoomTableRelation());
        TimerService.getInstance().addTimerTask(
                table.getReadyCd(),
                table.getRoomTableRelation(),
                new TableInnerReq(table.getPlayTypeStr(), table.getRoomId(), table.getTableId()) {
                    @Override
                    public void processImpl() throws Exception {
                        //倒计时结束将所有在桌子上的人放入游戏集合里
                        table.getInGamePlayersBySeatNum().forEach((k, v) -> {
                            if (!table.getInGamePlayers().containsKey(k)) {
                                v.setState(PlayerStateEnum.game_ready);
                                table.getInGamePlayers().put(k, v);
                            }
                        });
                        gameStart(table);
                    }
                });
    }

    //开始游戏并发牌
    public static void gameStart(final AbstractTable table) {
        log.info(table.getCurrGameNum() + 1 + "-----------------------------游戏开始-----------------------------" + table.getTableId());
        //停止牌桌内正在进行的倒计时
        try {
            TimerService.getInstance().delTimerTask(table.getRoomTableRelation());
            table.setCurrGameOrderId(GameOrderIdGenerator.generate());//游戏此回合唯一订单号
            log.debug("此局游戏的orderId:" + table.getCurrGameOrderId());
            //获取本桌使用的手牌
            CardOfTableHolder.PutCardOperationObj(table.getCurrGameOrderId(), new DealCardForTable(table.getGameType()));
            table.setCurrGameNum(table.getCurrGameNum() + 1);//局数加1
            //修改桌面状态为下注中
            table.setTableStateEnum(TableStateEnum.BET);
            table.setTableStatus();
            table.setStartTime(new Date());//记录游戏开始时间
            table.setPlayerHandCards();//设置玩家手牌(找出最大的牌发给vip)
//            int firstGiveCard = table.getFirstGiveCardSeatNum();
//            table.lookForFirstBetPlayer();//寻找第一个下注玩家
//            table.setFirstGiveCardSeatNum(table.getCurrActionSeatNum());
//            table.getInGamePlayers().forEach((k,v)->v.se);
            log.info("当前庄家模式：{}", table.getBankerType());
            if (table.getBankerType() == 1) {   //明牌抢庄
                table.pressCard(true, false);//第一次发牌发四张
                NoticeBroadcastMessages.giveCardBoardcast(table, true, -1);//广播发牌51005
//                commonTimer(table, 1, table.getInGamePlayers().size());//留给前段发牌动画的倒计时
                NoticeBroadcastMessages.robDealerCd(table);
                robDealerBefor(table);//抢庄倒计时
            } else if (table.getBankerType() == 2) { //自由抢庄
                NoticeBroadcastMessages.robDealerCd(table);
                robDealerBefor(table);//抢庄倒计时
            } else if (table.getBankerType() == 3) { //轮流庄
                PlayerInfo player = table.getNextBetPlayer(table.getPlayer(table.getCurrDealerPlayerId()).getSeatNum());
                player.setState(PlayerStateEnum.already_rob);
                player.setMultiple(1);
                table.setCurrDealerPlayerId(player.getPlayerId());
                RobDealerAnimation(table);
            } else if (table.getBankerType() == 4) { //固定庄
                if (table.getCurrDealerPlayerId() == null) {
                    PlayerInfo player = table.getPlayer(table.getFirstReadyPlayer());
                    table.setCurrDealerPlayerId(player.getPlayerId());
                    player.setState(PlayerStateEnum.already_rob);
                    player.setMultiple(1);
                } else {
                    PlayerInfo player = table.getPlayer(table.getCurrDealerPlayerId());
                    table.setCurrDealerPlayerId(player.getPlayerId());
                    player.setState(PlayerStateEnum.already_rob);
                    player.setMultiple(1);
                }
                RobDealerAnimation(table);
            } else if (table.getBankerType() == 5) { //牛牛上庄（第一局为点开始的人，如果没人牌型大于牛牛，则上局当庄的人继续）
                //此处每局找出最大牌型的人之后 都赋值给了firstReadyPlayer字段
                PlayerInfo player = table.getPlayer(table.getFirstReadyPlayer());
                table.setCurrDealerPlayerId(player.getPlayerId());
                player.setState(PlayerStateEnum.already_rob);
                player.setMultiple(1);
                RobDealerAnimation(table);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("游戏开始错误：" + e.getMessage());
        }
    }

    /**
     * 抢庄倒计时
     *
     * @param table
     */
    public static void robDealerBefor(final AbstractTable table) {
        log.info(table.getCurrGameNum() + "-----------------------------抢庄cd-----------------------------" + table.getTableId());
        table.setTableStateEnum(TableStateEnum.PLAYER_ROB);//设置桌子状态为抢庄中
        //停止牌桌内正在进行的倒计时
        TimerService.getInstance().delTimerTask(table.getRoomTableRelation());
        TimerService.getInstance().addTimerTask(
                table.getBankerCd(),
                table.getRoomTableRelation(), new TableInnerReq(table.getPlayType() + "", table.getRoomId(), table.getTableId()) {
                    @Override
                    public void processImpl() {
                        RobDealerAnimation(table);
                    }
                });
    }

    public static void RobDealerAnimation(final AbstractTable table) {
        TimerService.getInstance().delTimerTask(table.getRoomTableRelation());
        //把没有抢庄的设置为0
        for (PlayerInfo playerInfo : table.getInGamePlayersBySeatNum().values()) {
            if (!playerInfo.getState().equals(PlayerStateEnum.already_rob)) {
                //设置自己的状态
                playerInfo.setState(PlayerStateEnum.already_rob);
                playerInfo.setMultiple(0);
            }
        }
        if (table.getCurrDealerPlayerId() == null) {
            List<Integer> list = new ArrayList<>();
            table.getInGamePlayers().values().forEach(e -> list.add(e.getMultiple()));
            List<String> playerIds = new ArrayList<>();
            int maxMultiple = Collections.max(list);
            table.getInGamePlayers().values().forEach(e -> {
                if (e.getMultiple() == maxMultiple) {
                    playerIds.add(e.getPlayerId());
                }
            });
            int in = (int) (Math.random() * (playerIds.size()));
            table.setCurrDealerPlayerId(playerIds.get(in));
        }
        NoticeBroadcastMessages.dealerFinish(table);//广播抢庄的结果
        if (table.getBankerType() != 1) { //不是明牌抢庄时 定庄后才发牌
            table.pressCard(true, false);//第一次发牌发四张
            NoticeBroadcastMessages.giveCardBoardcast(table, true, -1);//广播发牌51005
        }
        timer(table);//此定时器是为了消息顺序
        betTimer(table);//下注倒计时
    }

    public static void timer(AbstractTable table) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                NoticeBroadcastMessages.countdownBroadcast(table, table.getBetCd());//广播下注时间 51032
            }
        }, 10);// 设定指定的时间time
    }

    /**
     * 下注计时器
     *
     * @param table
     */
    public static void betTimer(final AbstractTable table) {
        log.info(table.getCurrGameNum() + "-----------------------------下注cd-----------------------------" + table.getTableId());
        table.setTableStateEnum(TableStateEnum.BET); //修改桌面状态为下注中
        //停止牌桌内正在进行的倒计时
        TimerService.getInstance().delTimerTask(table.getRoomTableRelation());
        table.getInGamePlayers().values().forEach(e -> e.setState(PlayerStateEnum.beting));//设置所有玩家状态为下注中
        //下注前前端需要的信息
//        TableUtil.calculateBetMultiple(table);
//        NoticeBroadcastMessages.betMultiple(table);
        log.debug("下注计时器开始计时, 倒计时秒数->" + table.getCommonConfig().getBetCountDownSec());
        TimerService.getInstance().addTimerTask(
                table.getBetCd(),
                table.getRoomTableRelation(), new TableInnerReq(table.getPlayType() + "", table.getRoomId(), table.getTableId()) {
                    @Override
                    public void processImpl() throws Exception {
                        table.setLastActionTime(System.currentTimeMillis());
                        //当没有其它程序触发提前终止计时器时，那么计时器倒计时完成后，将执行下面的自动下注
                        Iterator<PlayerInfo> iter = table.getInGamePlayers().values().iterator();
                        while (iter.hasNext()) {
                            PlayerInfo playerInfo = iter.next();
                            if (!playerInfo.getState().equals(PlayerStateEnum.already_bet)) {
                                playerInfo.setState(PlayerStateEnum.already_bet);
                                playerInfo.setBetMultiple(1);
                            }
                        }
//                table.pressCard(true, true);//发出所有牌
//                NoticeBroadcastMessages.giveCardBoardcast(table, false, -1);//广播发牌51005
                        openCards(table);
                    }
                });
    }


    /**
     * 开牌倒计时
     *
     * @param table
     */
    public static synchronized void openCards(AbstractTable table) {
        log.info(table.getCurrGameNum() + "-----------------------------亮牌cd-----------------------------" + table.getTableId());
        table.pressCard(false, true);//发出所有牌
        NoticeBroadcastMessages.openCardCd(table);
        NoticeBroadcastMessages.giveCardBoardcast(table, false, -1);//广播发牌51005
        //停止牌桌内正在进行的倒计时
        TimerService.getInstance().delTimerTask(table.getRoomTableRelation());
//        //广播所有人的下注信息
//        NoticeBroadcastMessages.allPlayerBetResultBroadcast(table);
        table.setTableStateEnum(TableStateEnum.OPEN_CARD);
//        //通知客户端（发出所有人的牌）发出看牌协议及倒计时时间
//        NoticeBroadcastMessages.pressCardBroadcast(table);
        TimerService.getInstance().addTimerTask(
                table.getOpenCardCd(),
                table.getRoomTableRelation(), new TableInnerReq(table.getPlayType() + "", table.getRoomId(), table.getTableId()) {
                    @Override
                    public void processImpl() {
                        log.debug("//调用计时器，倒计时结算动画" + table.getTableStateEnum() + ", tableInfo->" + TableUtil.toStringNormal(table));
                        table.setLastActionTime(System.currentTimeMillis());
                        settleAnimation(table); //调用计时器，倒计时结算动画
                    }
                }
        );
    }

    /**
     * 结算
     *
     * @param table
     */
    public static void settleAnimation(final AbstractTable table) {
        log.info(table.getCurrGameNum() + "-----------------------------结算cd-----------------------------" + table.getTableId());
        //停止牌桌内正在进行的倒计时
        try {
            TimerService.getInstance().delTimerTask(table.getRoomTableRelation());
            table.setTableStateEnum(TableStateEnum.SETTLE_ANIMATION);
            table.setTableStatus();
            //玩家积分计算
            List<JoloGame.JoloGame_TablePlay_PlayerSettleInfo> settleInfoList = scoreCalculation(table);
            //广播结算51013
            NoticeBroadcastMessages.settleAnimationBroadcast(table, settleInfoList);
            //重置玩家的部分信息
            Iterator<PlayerInfo> iter = table.getInGamePlayersBySeatNum().values().iterator();
            while (iter.hasNext()) {
                PlayerInfo player = iter.next();
                //插入玩家牌局数据
                if (player.getState().getValue() >= PlayerStateEnum.beting.getValue()) {
                    log.info("玩家游戏记录：{}", player.toSitDownString());
                    insertGameRecord(table, player);
                }
                player.resetGameingInfo();
            }
            //修改桌子的状态
            table.setTableStateEnum(TableStateEnum.IDEL);
            table.setTableStatus();
            table.initTableStateAttribute();
            log.error("走没有这里：" + table.getClass().getCanonicalName());
            //所有局数结束进入总结算并解散桌子
            log.info("当前局数：{}， 游戏最高局数：{}", table.getCurrGameNum(), table.getGameNum());
            if (table.getCurrGameNum() >= table.getGameNum()) {
                table.getInGamePlayersBySeatNum().forEach((k, v) -> {
                    LeaveTableLogic.getInstance().logic(v, table);//处理玩家离桌的redis信息
                });
                log.info("删除的桌子信息：{}", table.toString());
                TableService.getInstance().directDestroyTable(String.valueOf(table.getPlayType()), table.getRoomId(), table.getTableId());
            } else {
                NoticeBroadcastMessages.gameStart(table);//游戏准备cd广播
                gameReady(table);//游戏准备cd
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("结算错误：{}", e.getMessage());
        } finally {

        }
    }

    /**
     * 分数计算
     *
     * @param table
     */
    public static List<JoloGame.JoloGame_TablePlay_PlayerSettleInfo> scoreCalculation(final AbstractTable table) {
        //比较玩家牌型大小
        PlayerInfo banker = table.getPlayer(table.getCurrDealerPlayerId()); //获胜的用户
        PlayerInfo maxPlayer = null;//牛牛上庄需要找出最大牌型的人
        NiuNiuPoker texasA = new NiuNiuPoker(typeConversion(banker.getHandCards()), table.getSpecialCardType());
        int texasAMultiple = NiuNiuTable.cardTypeMultiple(table.getDoubleRule(), texasA.getTypeCompareValue());
        banker.setCardType(texasA.getTypeCompareValue());
        banker.setCardTypeName(texasA.getTypeName());
        int bankerMultiple = banker.getMultiple();//庄家的抢庄倍数
        int bankerScore = 0;//庄家的输赢积分
        for (PlayerInfo player : table.getInGamePlayers().values()) {
            if (banker.getSeatNum() == player.getSeatNum()) continue;
            NiuNiuPoker texasB = new NiuNiuPoker(typeConversion(player.getHandCards()), table.getSpecialCardType());
            if (table.getBankerType() == 5 && texasB.getTypeCompareValue() >= 10) {//牛牛上庄需要找出最大牌型的人
                if (maxPlayer == null) {
                    maxPlayer = player;
                } else {
                    NiuNiuPoker maxCard = new NiuNiuPoker(typeConversion(maxPlayer.getHandCards()), table.getSpecialCardType());
                    if (maxCard.compare(texasB) == -1) {
                        maxPlayer = player;
                    }
                }
            }
            if (texasA.compare(texasB) == -1) {
                int texasBMultiple = NiuNiuTable.cardTypeMultiple(table.getDoubleRule(), texasB.getTypeCompareValue());
                player.setCurScore(bankerMultiple * player.getBetMultiple() * texasBMultiple);
            } else {
                player.setCurScore(bankerMultiple * player.getBetMultiple() * texasAMultiple * -1);//此处为负数
            }
            bankerScore -= player.getCurScore();
            player.setCardType(texasB.getTypeCompareValue());
            player.setCardTypeName(texasB.getTypeName());
            player.setPlayScoreStore(player.getPlayScoreStore() + player.getCurScore());
        }
        if (table.getBankerType() == 5 && maxPlayer != null) {//牛牛上庄
            table.setFirstReadyPlayer(maxPlayer.getPlayerId());
        }
        banker.setCurScore(bankerScore);//设置庄家输赢积分
        banker.setPlayScoreStore(banker.getPlayScoreStore() + banker.getCurScore());
        for (PlayerInfo player : table.getInGamePlayers().values()) {
            log.info("本局游戏玩家的信息：id -> {}, 牌型 -> {} 庄家倍数 -> {}, 下注倍数 -> {}, 输赢积分 -> {}",
                    player.getPlayerId(), player.getCardTypeName(), bankerMultiple, player.getBetMultiple(), player.getCurScore());
        }
        List<JoloGame.JoloGame_TablePlay_PlayerSettleInfo> settleInfoList = new ArrayList<>();
        for (PlayerInfo player : table.getInGamePlayersBySeatNum().values()) {
            JoloGame.JoloGame_TablePlay_PlayerSettleInfo.Builder playerSettleInfo = JoloGame.JoloGame_TablePlay_PlayerSettleInfo.newBuilder();
//            int winScore = table.getTableAlreadyBetScore() - (int) player.getWinLoseScore4Hand();
//            if (player.isWinner()) {
//                player.setPlayScoreStore(player.getPlayScoreStore() + winScore);
//            } else {
//                player.setPlayScoreStore(player.getPlayScoreStore() - player.getWinLoseScore4Hand());
//            }
            playerSettleInfo.setUserId(player.getPlayerId())
                    .setSeatNum(player.getSeatNum())
                    .setWinLose(-1)
                    .setWinLoseScore(player.getCurScore())
                    .setPlayScoreStore(player.getPlayScoreStore())
                    .addAllHandCards(player.getHandCards() == null ? new ArrayList<>() : Ints.asList(player.getHandCards()))
                    .setCardType(player.getCardType());
            settleInfoList.add(playerSettleInfo.build());
        }
        return settleInfoList;
    }
//    /**
//     * 结算动画倒计时
//     *
//     * @param table
//     */
//    public static void settleAnimation(final AbstractTable table) {
//        table.setTableStateEnum(TableStateEnum.SETTLE_ANIMATION);
//        //停止牌桌内正在进行的倒计时
//        TimerService.getInstance().delTimerTask(table.getRoomTableRelation());
//        //广播所有玩家的牌型
//        NoticeBroadcastMessages.showCardTypeBroadcast(table);
//        //广播结算动画
//        NoticeBroadcastMessages.settleAnimationBroadcast(table);
//
//
//        Iterator<PlayerInfo> iter = table.getAllPlayers().values().iterator();
//        while (iter.hasNext()) {
//            PlayerInfo playerInfo = iter.next();
//            if (playerInfo != null) {
//                playerInfo.resetGameingInfo();
//            }
//        }
//        RoomOprService.OBJ.standupHandler("" + table.getPlayType(), table.getRoomId(), table.getTableId());
//
//        for (PlayerInfo player : table.getInGamePlayersBySeatNum().values()) {
//            LeaveTableLogic.getInstance().logic(player, table, null,false);
//        }
//
//        //修改桌子的状态
//        table.setTableStateEnum(TableStateEnum.IDEL);
//        table.setTableStatus();
//        table.initTableStateAttribute();
//        table.setChannelId("");
//    }


    /**
     * 共享计时器
     *
     * @param table
     * @param taskType
     * @param time
     */
    public static void commonTimer(final AbstractTable table, int taskType, int time) {
        //停止牌桌内正在进行的倒计时
        TimerService.getInstance().delTimerTask(table.getRoomTableRelation());
        TimerService.getInstance().addTimerTask(
                time,
                table.getRoomTableRelation(),
                new TableInnerReq(table.getPlayTypeStr(), table.getRoomId(), table.getTableId()) {
                    @Override
                    public void processImpl() {
                        switch (taskType) {
                            case 1:
                                NoticeBroadcastMessages.countdownBroadcast(table, table.getBetCd());//广播倒计时
                                betTimer(table);//启动下注cd
                                break;
                            case 2:
                                NoticeBroadcastMessages.openCardCd(table);//广播开牌倒计时
                                GameLogic.openCards(table);//启动开牌倒计时
                                break;
                        }
                    }
                }
        );
    }

    public static List<Byte> typeConversion(int[] arr) {
        List<Byte> brr = new ArrayList<>();
        for (int i = 0; i < arr.length; i++) {
            brr.add((byte) arr[i]);
        }
        return brr;
    }

    private static void insertGameRecord(AbstractTable table, PlayerInfo player) {
        DBUtil.accumulationNumberOfGames(player.getPlayerId());
        GameRecordModel gameRecord = new GameRecordModel();
        gameRecord.setGameId(table.getPlayType());
        gameRecord.setTableId(table.getTableId());
        gameRecord.setCurrGameNum(table.getCurrGameNum());
        gameRecord.setUserId(player.getPlayerId());
        gameRecord.setNickName(player.getNickName());
        gameRecord.setCardType(player.getState() == PlayerStateEnum.fold ? -1 : player.getCardType());
        gameRecord.setHandCards(Ints.asList(player.getHandCards()).toString());
        gameRecord.setIcoUrl(player.getIcon());
//        gameRecord.setTotalTableScore(table.getTableAlreadyBetScore());
        gameRecord.setTotalTableScore((int) player.getWinLoseScore4Hand());
        gameRecord.setWinLoseScore((int) player.getCurScore());
//        if (player.isWinner()) {
//            int winScore = table.getTableAlreadyBetScore() - (int) player.getWinLoseScore4Hand();
//            gameRecord.setWinLoseScore(winScore);
//        } else {
//            gameRecord.setWinLoseScore((int) -player.getWinLoseScore4Hand());
//        }
        gameRecord.setStartTime(table.getStartTime());
        gameRecord.setEndTime(new Date());
        gameRecord.setPlayerCurrScore((int) player.getPlayScoreStore());
        DBUtil.insertGameRecord(gameRecord);//插入牌局记录
    }
}