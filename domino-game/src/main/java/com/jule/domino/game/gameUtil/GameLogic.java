package com.jule.domino.game.gameUtil;

import com.google.common.primitives.Ints;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.enums.RoleType;
import com.jule.domino.base.model.RoomTableRelationModel;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.GameRecordModel;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.game.model.TexasPoker;
import com.jule.domino.game.network.protocol.TableInnerReq;
import com.jule.domino.game.network.protocol.logic.LeaveTableLogic;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.TableService;
import com.jule.domino.game.service.TaskService;
import com.jule.domino.game.service.TimerService;
import com.jule.domino.game.service.holder.CardOfTableHolder;
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GameLogic {

    /**
     * 游戏开始计时器
     *
     * @param table
     */
    public static void gameReady(final AbstractTable table) {
        log.info(table.getCurrGameNum()+1+"-----------------------------游戏准备cd--------------------------" + table.getTableId());
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
                        if (table.getInGamePlayers().size() < 2) {
                            //修改桌子的状态
                            table.setTableStateEnum(TableStateEnum.IDEL);
                            table.setTableStatus();
                        } else {
                            gameStart(table);
                        }
                    }
                });
    }

    //开始游戏并发牌
    public static void gameStart(final AbstractTable table) {
        log.info(table.getCurrGameNum()+1+"-----------------------------游戏开始-----------------------------" + table.getTableId());
        //停止牌桌内正在进行的倒计时
        try {
            TimerService.getInstance().delTimerTask(table.getRoomTableRelation());
            table.setCurrGameOrderId(GameOrderIdGenerator.generate());//游戏此回合唯一订单号
            log.debug("此局游戏的orderId:" + table.getCurrGameOrderId());
            //获取本桌使用的手牌
//            CardOfTableHolder.PutCardOperationObj(table.getCurrGameOrderId(),
//                    new DealCardForTable(new RoomTableRelationModel(table.getPlayType() + "", table.getRoomId(), table.getTableId(), table.getTableStateEnum().getValue()),
//                            table.getCurrGameOrderId()));
            CardOfTableHolder.PutCardOperationObj(table.getCurrGameOrderId(),new DealCardForTable(table.getPlayType()));
            table.getInGamePlayers().forEach((k, player) -> {//所有人下底注
                int betScore = (int) table.getCurrBaseBetScore();//桌子目前的底注
                player.setState(PlayerStateEnum.beting);//设置桌子玩家的状态都为下注中
                player.setWinLoseScore4Hand(player.getWinLoseScore4Hand() + betScore);//当前一局玩家的总下注额
                player.setTotalAlreadyBetScore4Hand(player.getTotalAlreadyBetScore4Hand() + betScore);//当前桌子玩家的总下注额
                table.setTableAlreadyBetScore(table.getTableAlreadyBetScore() + betScore);//当前一局所有玩家的总下注额
                table.setTableTotalAlreadyBetScore(table.getTableTotalAlreadyBetScore() + betScore);//当前桌子所有玩家的总下注额
            });
            table.setCurrGameNum(table.getCurrGameNum() + 1);//局数加1
            //修改桌面状态为下注中
            table.setTableStateEnum(TableStateEnum.BET);
            table.setTableStatus();
            table.setStartTime(new Date());//记录游戏开始时间
            table.setPlayerHandCards();//设置玩家手牌
            table.pressCard(true, false);//第一次发牌发三张
            int firstGiveCard = table.getFirstGiveCardSeatNum();
            table.lookForFirstBetPlayer();//寻找第一个下注玩家
            table.setFirstGiveCardSeatNum(table.getCurrActionSeatNum());
            NoticeBroadcastMessages.giveCardBoardcast(table, true, firstGiveCard);//广播发牌51005
            commonTimer(table, 1, table.getInGamePlayers().size());
        }catch (Exception e){
            e.printStackTrace();
            log.error("游戏开始错误："+e.getMessage());
        }
    }

    /**
     * 下注计时器
     *
     * @param table
     */
    public static void betTimer(final AbstractTable table) {
        log.info(table.getCurrGameNum()+"-----------------------------下注cd-----------------------------" + table.getTableId());
        //停止牌桌内正在进行的倒计时
        TimerService.getInstance().delTimerTask(table.getRoomTableRelation());
        //修改桌面状态为下注中
        table.setTableStateEnum(TableStateEnum.BET);
        table.setTableStatus();
//        //广播cd时间
//        NoticeBroadcastMessages.betBeforeBroadcast(table);
        TimerService.getInstance().addTimerTask(
                table.getBetCd(),
                table.getRoomTableRelation(),
                new TableInnerReq(table.getPlayTypeStr(), table.getRoomId(), table.getTableId()) {
                    @Override
                    public void processImpl() throws Exception {
                        //记录桌子最后操作时间
                        table.setLastActionTime(System.currentTimeMillis());
                        //玩家超时弃牌
                        PlayerInfo currBetPlayer = table.getInGamePlayers().get(table.getCurrActionSeatNum());
                        if (currBetPlayer != null) {
                            currBetPlayer.setIsCurrActive(0);//玩家已不是当前行动者
                            currBetPlayer.setState(PlayerStateEnum.fold); //玩家已弃牌
                            table.getInGamePlayers().remove(currBetPlayer.getSeatNum());//将玩家从游戏map中踢出
                        }
                        log.info("当前玩家超时弃牌的数据：{}",currBetPlayer.toSitDownString());
                        playerFold(table, currBetPlayer);
                    }
                });
    }

    /**
     * 亮牌倒计时
     *
     * @param table
     */
    public static synchronized void openCards(final AbstractTable table) {
        log.info(table.getCurrGameNum()+"-----------------------------亮牌cd-----------------------------" + table.getTableId());
        //停止牌桌内正在进行的倒计时
        TimerService.getInstance().delTimerTask(table.getRoomTableRelation());
        table.setTableStateEnum(TableStateEnum.OPEN_CARD);
        table.setTableStatus();
        TimerService.getInstance().addTimerTask(
                table.getOpenCardCd(),
                table.getRoomTableRelation(),
                new TableInnerReq(table.getPlayTypeStr(), table.getRoomId(), table.getTableId()) {
                    @Override
                    public void processImpl() {
                        //记录桌子最后操作时间
                        table.setLastActionTime(System.currentTimeMillis());
                        //广播游戏中所有人的手牌
                        for (PlayerInfo player : table.getInGamePlayers().values()) {
                            if (player.getState() != PlayerStateEnum.open_card) {
                                NoticeBroadcastMessages.playerOpenCard(table, player);
                            }
                        }
                        log.info("开牌倒计时结束，进入结算：{}",table.toString());
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
        log.info(table.getCurrGameNum()+"-----------------------------结算cd-----------------------------" + table.getTableId());
        //停止牌桌内正在进行的倒计时
        try {
            TimerService.getInstance().delTimerTask(table.getRoomTableRelation());
            table.setTableStateEnum(TableStateEnum.SETTLE_ANIMATION);
            table.setTableStatus();
            //比较玩家牌型大小
            PlayerInfo winner = null; //获胜的用户
            for (PlayerInfo player : table.getInGamePlayers().values()) {
                if (winner == null) {
                    winner = player;
                } else {//此处有多余操作new和set
                    TexasPoker texasA = new TexasPoker(winner.getHandCards());
                    TexasPoker texasB = new TexasPoker(player.getHandCards());
                    winner.setCardType(texasA.getTypeCompareValue());
                    if (texasA.compareTo(texasB) == -1) {
                        winner = player;
                    }
                    player.setCardType(texasB.getTypeCompareValue());
                }
            }
            winner.setWinner(true);
            log.info("本局游戏赢家的信息：{}", winner.toSitDownString());
            //广播结算动画 && 结算51013
            NoticeBroadcastMessages.settleAnimationBroadcast(table);
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
            log.error("走没有这里："+table.getClass().getCanonicalName());
            //所有局数结束进入总结算并解散桌子
            log.info("当前局数：{}， 游戏最高局数：{}", table.getCurrGameNum(), table.getGameNum());
            if (table.getCurrGameNum() >= table.getGameNum()) {
                table.getInGamePlayersBySeatNum().forEach((k, v) -> {
                    //处理玩家离桌的redis信息
                    LeaveTableLogic.getInstance().logic(v, table);
                });
                log.info("删除的桌子信息：{}", table.toString());
                TableService.getInstance().directDestroyTable(String.valueOf(table.getPlayType()), table.getRoomId(), table.getTableId());
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("结算错误：{}",e.getMessage());
        }finally {

        }
    }

    public static void playerFold(AbstractTable table, PlayerInfo playerInfo) {
        //如果游戏中只有两个人以下，那么进入结算逻辑
        if (table.getInGamePlayers().size() < 2 && table.getTableStateEnum() != TableStateEnum.SETTLE_ANIMATION) {
            log.info("弃牌后游戏人数不足两人进入结算：{}",table.toString());
            NoticeBroadcastMessages.applyFold(table, playerInfo, null);
            GameLogic.settleAnimation(table); //调用计时器，倒计时结算动画
            return;
        }
        table.getEqualScore().clear();
        table.getInGamePlayers().values().forEach(e -> table.getEqualScore().add((int) e.getWinLoseScore4Hand()));
        playerInfo.setIsCurrActive(0);//弃牌后取消玩家的操作状态
        PlayerInfo nextActionPlayer = null;
        log.info("玩家弃牌桌子下注积分是否相等：{},  equalScore数据: {}", (table.getEqualScore().size() == 1), table.getEqualScore().toString());
        if (table.getEqualScore().size() != 1 || table.getRoundTableScore() == 0) {
            nextActionPlayer = table.getNextBetPlayer(playerInfo.getSeatNum());
        }
        NoticeBroadcastMessages.applyFold(table, playerInfo, nextActionPlayer);
        if (table.getEqualScore().size() == 1 && table.getRoundTableScore() != 0) {
            if (Collections.max(table.getEqualScore()) == table.getBetMaxScore()) {
                log.info("发完剩余 {} 张牌，并进入开牌阶段：{}",(5 - playerInfo.getHandCards().length),table.toString());
                //每个人每张牌0.4秒，四舍五入
                double cards = (5 - playerInfo.getHandCards().length) * 0.4;
                int time = (int)Math.round(cards * table.getInGamePlayers().size());
                table.pressCard(false, true);//发出所有牌
                //广播发牌51005
                int firstGiveCard = table.getFirstGiveCardSeatNum();
                NoticeBroadcastMessages.giveCardBoardcast(table, false, firstGiveCard);
                commonTimer(table,2,time);
            } else if (playerInfo.getHandCards().length == 5) {
                log.info("桌子进入开牌阶段：{}",table.toString());
                NoticeBroadcastMessages.openCardCd(table);//广播开牌倒计时
                GameLogic.openCards(table);//启动开牌倒计时
            } else {
                log.info("桌子进入开牌阶段：{}",table.toString());
                //设置玩家手牌
                table.pressCard(false, false);
                int firstGiveCard = table.getFirstGiveCardSeatNum();
                //寻找第一个下注玩家
                table.lookForFirstBetPlayer();
                table.setFirstGiveCardSeatNum(table.getCurrActionSeatNum());
                //广播发牌51005
                NoticeBroadcastMessages.giveCardBoardcast(table, false, firstGiveCard);
                table.setRoundTableScore(0);//重置本轮次玩家下注额
                int time = (int)Math.round(table.getInGamePlayers().size() * 0.4);
                commonTimer(table,1,time);
            }
        } else {
            log.info("普通下注或跟注：{}",table.toString());
            //启动下一个行动玩家的bet倒计时
            NoticeBroadcastMessages.countdownBroadcast(table,table.getBetCd());//广播倒计时
            GameLogic.betTimer(table);
        }

    }

    /**
     * 共享计时器
     * @param table
     * @param taskType
     * @param time
     */
    public static void commonTimer(final AbstractTable table,int taskType,int time){
        //停止牌桌内正在进行的倒计时
        TimerService.getInstance().delTimerTask(table.getRoomTableRelation());
        TimerService.getInstance().addTimerTask(
                time,
                table.getRoomTableRelation(),
                new TableInnerReq(table.getPlayTypeStr(), table.getRoomId(), table.getTableId()) {
                    @Override
                    public void processImpl() {
                        switch (taskType){
                            case 1:
                                NoticeBroadcastMessages.countdownBroadcast(table,table.getBetCd());//广播倒计时
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

    public static void timeOutRemoveTable(final AbstractTable table) {
        TimerService.getInstance().addTimerTask(
                600,
                table.getRoomTableRelation(),
                new TableInnerReq(table.getPlayTypeStr(), table.getRoomId(), table.getTableId()) {
                    @Override
                    public void processImpl() {
                        log.info("超时删除桌子：{}", table.toString());
                        //将创建房间的房卡发回玩家账号
                        User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + table.getCreateTableUserId(), User.class);
                        if (user == null) {
                            user = DBUtil.selectByPrimaryKey(table.getCreateTableUserId());
                        }
                        user.setMoney(user.getMoney() + table.getGameNum() / 5);
                        DBUtil.updateByPrimaryKey(user);
                        table.getAllPlayers().forEach((k, v) -> {
                            //玩家离桌广播
                            NoticeBroadcastMessages.sendPlayerLeaveNotice(table, v);
                        });
                        table.getAllPlayers().forEach((k, v) -> {
                            //处理玩家离桌的redis信息
                            LeaveTableLogic.getInstance().logic(v, table);
                        });
                        TableService.getInstance().directDestroyTable(String.valueOf(table.getPlayType()), table.getRoomId(), table.getTableId());
                    }
                }
        );
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
        if (player.isWinner()) {
            int winScore = table.getTableAlreadyBetScore() - (int) player.getWinLoseScore4Hand();
            gameRecord.setWinLoseScore(winScore);
        } else {
            gameRecord.setWinLoseScore((int) -player.getWinLoseScore4Hand());
        }
        gameRecord.setStartTime(table.getStartTime());
        gameRecord.setEndTime(new Date());
        gameRecord.setPlayerCurrScore((int) player.getPlayScoreStore());
        DBUtil.insertGameRecord(gameRecord);//插入牌局记录
    }
}