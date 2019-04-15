package com.jule.domino.game.gameUtil;

import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.game.network.protocol.TableInnerReq;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.play.TableUtil;
import com.jule.domino.game.service.LogService;
import com.jule.domino.game.service.TimerService;
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
import lombok.extern.slf4j.Slf4j;
import java.util.Iterator;

@Slf4j
public class GameLogic {

    /**
     * 游戏开始计时器
     *
     * @param table
     */
    public static void gameReady(final AbstractTable table) {
        table.setTableStateEnum(TableStateEnum.GAME_READY);
        table.setTableStatus();
        //停止牌桌内正在进行的倒计时
        TimerService.getInstance().delTimerTask(table.getRoomTableRelation());

        //NoticeBroadcastMessages.sendPlayerInNotice(table);

        table.setLoseUserIds(table.getInGamePlayerIds(""));

        TimerService.getInstance().addTimerTask(
                table.getCommonConfig().getGameStartCountDownSec(),
                table.getRoomTableRelation(),
                new TableInnerReq(table.getPlayTypeStr(), table.getRoomId(), table.getTableId()) {
                    @Override
                    public void processImpl() throws Exception {
                        NoticeBroadcastMessages.sendPlayerInNotice(table);

                        //修改桌面状态为下注中
                        table.setTableStateEnum(TableStateEnum.BET);
                        table.setTableStatus();

                        startAni(table);
                    }
                });
    }

    public static void startAni(final AbstractTable table) {
        TimerService.getInstance().addTimerTask(
                table.getCommonConfig().getGameStartCountDownSec(),
                table.getRoomTableRelation(),
                new TableInnerReq(table.getPlayTypeStr(), table.getRoomId(), table.getTableId()) {
                    @Override
                    public void processImpl() throws Exception {
                        betTimer(table);
                    }
                });
        //发送开局日志
        LogService.OBJ.sendGamestartLog(table);
        LogService.OBJ.sendGameStartPlayerLog(table);
    }


    /**
     * 下注计时器
     *
     * @param table
     */
    public static void betTimer(final AbstractTable table) {
        log.debug("下注操作");
        //停止牌桌内正在进行的倒计时
        TimerService.getInstance().delTimerTask(table.getRoomTableRelation());

        //开局前信息
        NoticeBroadcastMessages.gameStart(table);

        table.getInGamePlayers().putAll(table.getInGamePlayersBySeatNum());

        //下注前前端需要的信息
        TableUtil.calculateBetMultiple(table);
        NoticeBroadcastMessages.betBeforeBroadcast(table);
        NoticeBroadcastMessages.betMultiple(table);
        TimerService.getInstance().addTimerTask(
                table.getCommonConfig().getBetCountDownSec(),
                table.getRoomTableRelation(),
                new TableInnerReq(table.getPlayTypeStr(), table.getRoomId(), table.getTableId()) {
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
                log.debug("给牌操作即将开始");
                giveCards(table);
            }
        });
    }

    /**
     * 发牌动画
     * @param table
     */
    public static synchronized void giveCards(AbstractTable table) {
        log.debug("给牌操作");
        //停止牌桌内正在进行的倒计时
        TimerService.getInstance().delTimerTask(table.getRoomTableRelation());
        //广播所有人的下注信息51035
        NoticeBroadcastMessages.allPlayerBetResultBroadcast(table);
        //广播发牌51005
        NoticeBroadcastMessages.giveCardBoardcast(table);
        TimerService.getInstance().addTimerTask(
                table.getCommonConfig().getOpenCardsCD(),
                table.getRoomTableRelation(),
                new TableInnerReq(table.getPlayTypeStr(), table.getRoomId(), table.getTableId()) {
                    @Override
                    public void processImpl() {
                        log.debug("调用计时器，发牌动画" + table.getTableStateEnum() + ", tableInfo->" + TableUtil.toStringNormal(table));
                        table.setLastActionTime(System.currentTimeMillis());
                        openCards(table);
                    }
                }
        );
    }

    /**
     * @param table
     */
    public static synchronized void openCards(AbstractTable table) {
        //停止牌桌内正在进行的倒计时
        TimerService.getInstance().delTimerTask(table.getRoomTableRelation());
        //若都操作了下注 则发牌
        table.pressCard();
        table.setTableStateEnum(TableStateEnum.OPEN_CARD);
        table.setTableStatus();
        //通知客户端（发出所有人的牌）发出看牌协议及倒计时时间51033
        NoticeBroadcastMessages.pressCardBroadcast(table);

        TimerService.getInstance().addTimerTask(
                table.getCommonConfig().getSettleCD(),
                table.getRoomTableRelation(),
                new TableInnerReq(table.getPlayTypeStr(), table.getRoomId(), table.getTableId()) {
                    @Override
                    public void processImpl() {
                        log.debug("调用结算动画" + table.getTableStateEnum() + ", tableInfo->" + TableUtil.toStringNormal(table));
                        table.setLastActionTime(System.currentTimeMillis());
                        settleAnimation(table); //调用计时器，倒计时结算动画
                    }
                }
        );
    }

    /**
     * 结算动画倒计时
     *
     * @param table
     */
    public static void settleAnimation(final AbstractTable table) {
        table.setTableStateEnum(TableStateEnum.SETTLE_ANIMATION);
        table.setTableStatus();
        //停止牌桌内正在进行的倒计时
        TimerService.getInstance().delTimerTask(table.getRoomTableRelation());

        //广播结算动画 && 结算51013
        NoticeBroadcastMessages.settleAnimationBroadcast(table);

        Iterator<PlayerInfo> iter = table.getAllPlayers().values().iterator();
        while (iter.hasNext()) {
            PlayerInfo playerInfo = iter.next();
            if (playerInfo != null) {
                playerInfo.resetGameingInfo();
            }
        }

        for (PlayerInfo player : table.getInGamePlayersBySeatNum().values()) {
            //强制玩家离桌，清除玩家数据
            NoticeBroadcastMessages.sendPlayerLeaveNotice(table, player);
            table.returnLobby(player.getPlayerId(), false);
        }
        //修改桌子的状态
        table.setTableStateEnum(TableStateEnum.IDEL);
        table.setTableStatus();
        table.initTableStateAttribute();
        table.clearAllSeats();
    }


}