package com.jule.robot.service.websocket;

import JoloProtobuf.AuthSvr.JoloAuth;
import JoloProtobuf.GameSvr.JoloGame;
import JoloProtobuf.RoomSvr.JoloRoom;
import com.jule.db.proxy.EntityProxy;
import com.jule.robot.config.Config;
import com.jule.robot.model.PlayerInfo;
import com.jule.robot.model.eenum.PlayerStateEnum;
import com.jule.robot.service.holder.*;
import com.jule.robot.strategy.BaseRobotStrategry;
import com.jule.robot.strategy.StrategryReflectMap;
import com.jule.robot.util.RandomTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class RobotGameWebSocketClient extends BaseWebSocketClient {
    private final static Logger logger = LoggerFactory.getLogger(RobotGameWebSocketClient.class);
    private int alreadyPlayRound = 0;
    private int playRoundCount = 50;
    private String verify = ""; //登录证书
    private long _baseBetScore = 0;
    private String currPlayType = ""; //Dealer玩法时的当前选择的玩法
    private boolean isNeedFoldBy91001005 = false; //该机器人是否需要在Blind玩法时弃牌

    private boolean beted = false;


    public RobotGameWebSocketClient(int gameId, String userId, String roomId, String tableId ) {
        this.gameId = gameId;
        this.roomId = roomId;
        this.tableId = tableId;
        this.userId = userId;
        super.connectWebSocket(FunctionIdHolder.GATE_SVR_URI);
    }

    @Override
    public void routeFunctionId(int functionId, byte[] bytes) {
        BaseRobotStrategry strategy = StrategryReflectMap.getStrategry(functionId);
        switch (functionId) {
            //Room
            case FunctionIdHolder.Room_ACK_ApplyJoinTable:
                strategy.doAction(this, functionId, bytes);
                return;
            case FunctionIdHolder.GATE_ACK_loginUser:
                AckLoginUser(functionId, bytes);
                return;

            //Game
            case FunctionIdHolder.Game_ACK_ApplySitDown:
                strategy.doAction(this, functionId, bytes);
                return;
            case FunctionIdHolder.Game_ACK_ApplyLeave:
                strategy.doAction(this, functionId, bytes);
                return;
            case FunctionIdHolder.Game_ACK_ApplyBet:
                strategy.doAction(this, functionId, bytes);
                return;


            //NOTICE
            case FunctionIdHolder.Game_Notice_SiteDown:
                NoticeSitDown(functionId, bytes);
                return;
            case FunctionIdHolder.Game_Notice_StandUp:
                NoticeStandUp(functionId, bytes);
                return;
            case FunctionIdHolder.Game_Notice_leaveReq:
                //Noticeleave(functionId, bytes);
                strategy.doAction(this, functionId, bytes);
                return;
            case FunctionIdHolder.Game_Notice_BuyIn:
                NoticeBuyIn(functionId, bytes);
                return;
            case FunctionIdHolder.Game_Notice_GameStart:
                NoticeGameStart(functionId, bytes);
                return;
            case FunctionIdHolder.Game_Notice_GiveCardRound_Start:
                NoticeGiveCardRoundStart(functionId, bytes);
                return;

            case FunctionIdHolder.Game_Notice_BetMultipleInfoReq:
                NoticeBetMultipleInfo(functionId, bytes);
                return;

            case FunctionIdHolder.Game_Notice_BetRound_DoBet:
                NoticeBetRoundDoBet(functionId, bytes);
                return;
            case FunctionIdHolder.Game_Notice_SettleRound_SettleResult:
                NoticeSettleResult(functionId, bytes);
                return;
            case FunctionIdHolder.Game_Notice_SettleRound_History:
                NoticeSettleHistory(functionId, bytes);
                return;
            default:
                logger.debug("未知消息 functionID ->" + functionId + ", functionName ->" + FunctionIdHolder.GetFunctionName(functionId));
        }
    }

    @Override
    public void webSocketOpen() {
        ExecuteLoginUser();
    }


    //region 下注
    public void ExecuteBet(String gameOrderId, int baseBetScore, int betScore, int reqNum, int limitBetScore) {
        try {
            logger.debug("执行 下注 动作。, {}, baseBetScore->{}, betScore->{}", toStringUserInfo(), baseBetScore, betScore);
            if (betScore < baseBetScore) {
                betScore = baseBetScore;
            }

            if (betScore > limitBetScore) {
                betScore = limitBetScore;
            }

            PlayerInfo playerInfo = onTablePlayers.get(userId);
            if (null == playerInfo) {
                logger.error("ExecuteBet(). 用户已站起，playerInfo为空, {}", toStringUserInfo());
                return;
            }
            int functionId = FunctionIdHolder.Game_REQ_ApplyBet;

            JoloGame.JoloGame_ApplyBetReq.Builder req = JoloGame.JoloGame_ApplyBetReq.newBuilder();
            req.setUserId(userId);
            req.setRoomId(roomId);
            req.setTableId(tableId);
            req.setGameOrderId(gameOrderId);
            req.setBetScore(2);

            logger.debug("GAME_REQ_ApplyBet Body, reqNum->" + reqNum + ", functionName->" + FunctionIdHolder.GetFunctionName(functionId) + ", " + toStringUserInfo() + ", tableId->" + req.getTableId()
                    + ", gameOrderId->" + req.getGameOrderId() + ", isBlind->" + playerInfo.getIsBlind() + ", betScore->" + betScore);

            sendData(functionId, gameId, reqNum, req.build().toByteArray(), gameSvrId);
        } catch (Exception ex) {
            logger.error("ExecuteBet ERROR," + toStringUserInfo() + ", ex = " + ex.getMessage(), ex);
        }
    }
    //endregion

    //region 登录 & 加入牌桌
    public void ExecuteLoginUser() {
        JoloAuth.JoloCommon_LoginReq.Builder req = JoloAuth.JoloCommon_LoginReq.newBuilder();
        req.setUserId(userId);
        req.setToken(userId);
        req.setClientVersion("robot");
        req.setChannelId("robot");
        req.setUserIp("192.168.0.14");
        req.setPlatform(0);
        req.setPlatformVersion("1.0.0");
        req.setDeviceNum("abcdefg-gfedcba");
        req.setVerify(verify);

        int reqNum = (int) (System.currentTimeMillis() / 1000);
        logger.debug("REQ ExecuteLoginUser, gameId->" + gameId + ", " + toStringUserInfo() + ", reqNum->" + reqNum);
        sendData(FunctionIdHolder.GATE_REQ_loginUser, gameId, reqNum, req.build().toByteArray());
    }

    private void AckLoginUser(int functionId, byte[] bytes) {
        try {
            JoloAuth.JoloCommon_LoginAck ack = JoloAuth.JoloCommon_LoginAck.parseFrom(bytes);
            logger.debug("ACK Header, functionId->" + functionId + ", functionName->" + FunctionIdHolder.GetFunctionName(functionId) + ", ACK Body, result->" + ack.getResult() + ", ResultMsg->" + ack.getResultMsg() + ", userId->" + ack.getUserId()
                    + ", nickName->" + ack.getNickName() + ", money->" + ack.getMoney());

            if (ack.getResult() == 1) {
                userId = ack.getUserId();
                verify = ack.getVerify();
                nickName = ack.getNickName();
                RobotClientHolder.addClient(userId, this);
                if (Config.TEST_TYPE_IS_STRESS == 1) {
                    ExecuteJoinTableForStressTest();
                } else {
                    ExecuteJoinTable();
                }
            } else {
                RobotClientHolder.closeClient(userId, this, "登录Game失败,result=" + ack.getResult() + ",money=" + ack.getMoney());
                logger.error("login failed. userId->" + ack.getUserId() + ", money->" + ack.getMoney() + ", result->" + ack.getResult() + ", resultMsg->" + ack.getResultMsg());
            }
        } catch (Exception ex) {
            logger.error("AckLoginUser error, msg = " + ex.getMessage(), ex);
        }
    }

    public void ExecuteJoinTable() {
        JoloRoom.JoloRoom_ApplyJoinTableRobotReq.Builder req = JoloRoom.JoloRoom_ApplyJoinTableRobotReq.newBuilder();
        req.setGameId(gameId + "");
        req.setUserId(userId);
        req.setRoomId(roomId);
        req.setTableId(tableId);

        int reqNum = (int) (System.currentTimeMillis() / 1000);
        sendData(FunctionIdHolder.Room_REQ_ApplyJoinTable, gameId, reqNum, req.build().toByteArray());
    }

    public void ExecuteJoinTableForStressTest() {
        JoloRoom.JoloRoom_ApplyJoinTableReq.Builder req = JoloRoom.JoloRoom_ApplyJoinTableReq.newBuilder();
        req.setGameId(gameId + "");
        req.setUserId(userId);
        req.setRoomId("10");
        int functionId = FunctionIdHolder.Room_REQ_ApplyJoinTable_NoRoomId;
        int reqNum = (int) (System.currentTimeMillis() / 1000);
        sendData(functionId, gameId, reqNum, req.build().toByteArray());
    }

    //打赏荷官
    private void ExecuteRewardCroupier() {
        logger.debug("执行 打赏荷官, {}", toStringUserInfo());
    }

    //确认准备
    private void ExecuteReady() {
        JoloGame.JoloGame_ReadyReq.Builder req = JoloGame.JoloGame_ReadyReq.newBuilder();
        req.setUserId(userId);
        req.setRoomId(roomId);
        req.setTableId(tableId);
        req.setSeatNum(seatNum);

        int reqNum = (int) (System.currentTimeMillis() / 1000);
        int functionId = FunctionIdHolder.Game_REQ_ReadyReq;

        logger.info("ExecuteReady Body, reqNum->" + reqNum + ", functionName->" + FunctionIdHolder.GetFunctionName(functionId) + ", " + toStringUserInfo() + ", roomId->" + req.getRoomId() + ", tableId->" + req.getTableId()
                + ", seatNum->" + req.getSeatNum());

        sendData(functionId, gameId, reqNum, req.build().toByteArray(), gameSvrId);
    }


    //执行申请下注倍数
    private void ExecuteBetMultiple(List<Integer> multipleList) {
        long delayMillSec = (long) (1000 + 1000 * RandomTools.getRandomNum(2));
        Timer timer = new Timer();//实例化Timer类
        timer.schedule(new TimerTask() {
            public void run() {
                JoloGame.JoloGame_ApplyBetReq.Builder req = JoloGame.JoloGame_ApplyBetReq.newBuilder();
                req.setUserId(userId);
                req.setRoomId(roomId);
                req.setTableId(tableId);
                req.setGameOrderId(gameOrderId);
                //机器人牌九下注倍数随机
                req.setBetScore(multipleList.get(RandomTools.getRandomNum(multipleList.size() - 1)));

                int reqNum = (int) (System.currentTimeMillis() / 1000);
                int functionId = FunctionIdHolder.Game_REQ_ApplyBet;

                logger.info("执行下注倍数 Body, reqNum->" + reqNum + ", functionName->" + FunctionIdHolder.GetFunctionName(functionId) + ", " + toStringUserInfo() + ", roomId->" + req.getRoomId() + ", tableId->" + req.getTableId());
                sendData(functionId, gameId, reqNum, req.build().toByteArray(), gameSvrId);
            }
        }, delayMillSec);
    }

    //赠送礼物
    private void ExecuteGiveGifts() {
        try {
            //送礼干掉
        } catch (Exception ex) {
            logger.error("ExecuteGiveGifts error, msg->" + ex.getMessage(), ex);
        }
    }


    /**
     * Notice_坐下
     */
    private void NoticeSitDown(int functionId, byte[] bytes) {
        try {
            JoloGame.JoloGame_Notice2Client_SitDownReq ack = JoloGame.JoloGame_Notice2Client_SitDownReq.parseFrom(bytes);
            logger.info("Notice Header, functionId->" + functionId + ", functionName->" + FunctionIdHolder.GetFunctionName(functionId) + toStringReplaceLine()
                    + ", Body, roomId->" + ack.getRoomId() + ", tableId->" + ack.getTableId() + toStringReplaceLine()
                    + ", " + toStringUserInfo() + ", seatNum->" + ack.getSeatNum() + ", playScore->" + ack.getPlayScore());

            if (roomId.equals(ack.getRoomId())
                    && tableId.equals(ack.getTableId())) {
                PlayerInfo playerInfo = new PlayerInfo();
                playerInfo.setUserId(ack.getUserId());
                playerInfo.setNickName(ack.getNickName());
                playerInfo.setSeatNum(ack.getSeatNum());
                playerInfo.setPlayScoreStore(ack.getPlayScore());
                playerInfo.setState(PlayerStateEnum.siteDown.getValue());

                onTablePlayers.put(ack.getUserId(), playerInfo);
                getInGamePlayersBySeatNum().put(ack.getSeatNum(), playerInfo);
            }
        } catch (Exception ex) {
            logger.error("NoticeSitDown error, " + toStringUserInfo() + ", msg = " + ex.getMessage(), ex);
        }
    }

    /**
     * Notice_站起
     */
    private void NoticeStandUp(int functionId, byte[] bytes) {
        try {
            JoloGame.JoloGame_Notice2Client_StandUpReq ack = JoloGame.JoloGame_Notice2Client_StandUpReq.parseFrom(bytes);
            logger.debug("Notice Header, functionId->" + functionId + ", functionName->" + FunctionIdHolder.GetFunctionName(functionId) + toStringReplaceLine()
                    + toStringUserInfo() + ", Body, roomId->" + ack.getRoomId() + ", tableId->" + ack.getTableId() + ", seatNum->" + ack.getSeatNum());

            if (roomId.equals(ack.getRoomId()) && tableId.equals(ack.getTableId())) {
                PlayerInfo playerInfo = onTablePlayers.remove(ack.getUserId());
                if (playerInfo != null) {
                    standupPlayers.put(ack.getUserId(), playerInfo);
                }


                PlayerInfo thisClientRobot = onTablePlayers.get(userId);
                if (null == thisClientRobot) {
                    thisClientRobot = standupPlayers.get(userId);
                }
                if (null != thisClientRobot &&
                        (thisClientRobot.getState() == PlayerStateEnum.spectator.getValue()
                                || thisClientRobot.getState() == PlayerStateEnum.siteDown.getValue())
                        ) {
                    //如果当前连接的机器人，正处于旁观或刚坐下的状态，那么判断机器人是否需要离桌
                    CheckForQuit(); //机器人判断自己是否应该站起
                }
            }
        } catch (Exception ex) {
            logger.error("NoticeStandUp error," + toStringUserInfo() + ", msg = " + ex.getMessage(), ex);
        }
    }

    //离桌通知
    private void Noticeleave(int functionId, byte[] bytes) {
        try {
            JoloGame.JoloGame_Notice2Client_leaveReq ack = JoloGame.JoloGame_Notice2Client_leaveReq.parseFrom(bytes);
            logger.debug("Notice Header, functionId->" + functionId + ", functionName->" + FunctionIdHolder.GetFunctionName(functionId) + toStringReplaceLine()
                    + toStringUserInfo() + ", Body, roomId->" + ack.getRoomId() + ", tableId->" + ack.getTableId());

            if (roomId.equals(ack.getRoomId()) && tableId.equals(ack.getTableId())) {
                PlayerInfo playerInfo = onTablePlayers.remove(ack.getUserId());
                if (playerInfo != null) {
                    standupPlayers.put(ack.getUserId(), playerInfo);
                }

                PlayerInfo thisClientRobot = onTablePlayers.get(userId);
                if (null == thisClientRobot) {
                    thisClientRobot = standupPlayers.get(userId);
                }
                if (null != thisClientRobot &&
                        (thisClientRobot.getState() == PlayerStateEnum.spectator.getValue()
                                || thisClientRobot.getState() == PlayerStateEnum.siteDown.getValue())
                        ) {
                    //如果当前连接的机器人，正处于旁观或刚坐下的状态，那么判断机器人是否需要离桌
                    CheckForQuit(); //机器人判断自己是否应该站起
                }
            }
        } catch (Exception ex) {
            logger.error("Noticeleave error," + toStringUserInfo() + ", msg = " + ex.getMessage(), ex);
        }
    }

    /**
     * Notice_买入
     */
    private void NoticeBuyIn(int functionId, byte[] bytes) {
        try {
            JoloGame.JoloGame_Notice2Client_BuyInReq ack = JoloGame.JoloGame_Notice2Client_BuyInReq.parseFrom(bytes);
            logger.debug("Notice Header, functionId->" + functionId + ", functionName->" + FunctionIdHolder.GetFunctionName(functionId) + toStringReplaceLine()
                    + ", Body, roomId->" + ack.getRoomId() + ", tableId->" + ack.getTableId() + toStringReplaceLine()
                    + ", " + toStringUserInfo() + ", seatNum->" + ack.getSeatNum() + toStringReplaceLine()
                    + ", buyInScore->" + ack.getBuyInScore() + ", playScoreStore->" + ack.getPlayScoreStore());

            if (roomId.equals(ack.getRoomId()) && tableId.equals(ack.getTableId())) {
                PlayerInfo playerInfo = onTablePlayers.get(ack.getUserId());
                if (null == playerInfo) {
                    logger.info("NoticeBuyIn(). 用户已站起，playerInfo为空, userId->{}, nickName->{}", ack.getUserId(), getNickName(ack.getUserId()));
                } else {
                    playerInfo.setSeatNum(ack.getSeatNum());
                    playerInfo.setPlayScoreStore(ack.getPlayScoreStore());
                    updatePlayScoreStore(ack.getUserId(), ack.getPlayScoreStore(), true, gameOrderId, "买入Notice");
                }
            }
        } catch (Exception ex) {
            logger.error("NoticeBuyIn error," + toStringUserInfo() + ", msg = " + ex.getMessage(), ex);
        }
    }

    /**
     * Notice_牌局开始
     */
    private void NoticeGameStart(int functionId, byte[] bytes) {
        try {
            this._baseBetScore = 0;
            JoloGame.JoloGame_Notice2Client_GameStartReq ack = JoloGame.JoloGame_Notice2Client_GameStartReq.parseFrom(bytes);
            logger.info("Notice Header, functionId->" + functionId + ", functionName->" + FunctionIdHolder.GetFunctionName(functionId) + toStringReplaceLine()
                    + ", Body, roomId->" + ack.getRoomId() + ", tableId->" + ack.getTableId() + ", gameOrderId->" + ack.getGameOrderId() + ", countDownSec->" + ack.getCountDownSec() + toStringReplaceLine()
                    + toStringUserInfo());

            if (roomId.equals(ack.getRoomId()) && tableId.equals(ack.getTableId())) {
                gameOrderId = ack.getGameOrderId();
                logger.debug("NoticeGameStart, 设置gameOrderId->{}, {}", ack.getGameOrderId(), toStringUserInfo());

                //在牌局开始时，根据概率判断是否要打赏荷官。 add by gx 20180720 16:01
                int ranomNum = RandomTools.getRandomNum(100);
                if (ranomNum <= Config.ROBOT_REWARD_CROUPIER) {
                    ExecuteRewardCroupier();
                }

                //确认准备
                ExecuteReady();

                //复位牌局信息
                resetInitVariable();
            }
        } catch (Exception ex) {
            logger.error("NoticeGameStart error," + toStringUserInfo() + ", msg = " + ex.getMessage(), ex);
        }
    }

    /**
     * Notice_发牌轮_开始
     */
    private void NoticeGiveCardRoundStart(int functionId, byte[] bytes) {
        try {
            resetInitVariable();

            JoloGame.JoloGame_Notice2Client_GiveCardRound_StartReq ack = JoloGame.JoloGame_Notice2Client_GiveCardRound_StartReq.parseFrom(bytes);
            logger.info("NoticeGiveCardRoundStart Notice Header, functionId->" + functionId + ", functionName->" + FunctionIdHolder.GetFunctionName(functionId) + toStringReplaceLine()
                    + ", Body, roomId->" + ack.getRoomId() + ", tableId->" + ack.getTableId() + ", gameOrderId->" + ack.getGameOrderId() + toStringReplaceLine()
                    + ", getPlayerInfoListList->" + ack.getPlayerInfoListList().size() + toStringReplaceLine()
                    + toStringUserInfo());

            if (roomId.equals(ack.getRoomId()) && tableId.equals(ack.getTableId())) {
                gameOrderId = ack.getGameOrderId();
                logger.debug("NoticeGiveCardRoundStart, 设置gameOrderId->{}, {}", ack.getGameOrderId(), toStringUserInfo());
            }

            onTablePlayers.clear();
            for (JoloGame.JoloGame_TablePlay_PlayerInfo joloPlayerInfo : ack.getPlayerInfoListList()) {
                PlayerInfo playerInfo = new PlayerInfo();
                playerInfo.setUserId(joloPlayerInfo.getUserId());
                playerInfo.setNickName(joloPlayerInfo.getNickName());
                playerInfo.setSeatNum(joloPlayerInfo.getSeatNum());
                playerInfo.setPlayScoreStore(joloPlayerInfo.getPlayScoreStore());
                playerInfo.setState(PlayerStateEnum.gameing.getValue());
                playerInfo.setIsBlind(1);

                onTablePlayers.put(joloPlayerInfo.getUserId(), playerInfo);
                updatePlayScoreStore(joloPlayerInfo.getUserId(), joloPlayerInfo.getPlayScoreStore(), true, ack.getGameOrderId(),
                        "牌局开始-已下底注，ante->" + RoomConfigHolder.getInstance().getRoomConfig(ack.getRoomId()).getAnte());
                getInGamePlayersBySeatNum().put(joloPlayerInfo.getSeatNum(), playerInfo);
            }
        } catch (Exception ex) {
            logger.error("NoticeGiveCardRoundStart error," + toStringUserInfo() + ", msg = " + ex.getMessage(), ex);
        }
    }

    /**
     * Notice_下注轮_下注
     */
    private void NoticeBetRoundDoBet(int functionId, byte[] bytes) {
        try {
            JoloGame.JoloGame_Notice2Client_BetRound_DoBetReq ack = JoloGame.JoloGame_Notice2Client_BetRound_DoBetReq.parseFrom(bytes);
            logger.debug("收到下注通知, functionId->" + functionId + ", functionName->" + FunctionIdHolder.GetFunctionName(functionId) + toStringReplaceLine()
                    + ", Body, roomId->" + ack.getRoomId() + ", tableId->" + ack.getTableId() + ", gameOrderId->" + ack.getGameOrderId() + toStringReplaceLine()
                    + ", betUserId->" + ack.getBetUserId() + ",betNickName->" + getNickName(ack.getBetUserId()) + ", BetUserSeatNum->" + ack.getBetUserSeatNum() + ", betScore->" + ack.getBetScore() + toStringReplaceLine()
                    + toStringUserInfo());

            if (roomId.equals(ack.getRoomId()) && tableId.equals(ack.getTableId()) && gameOrderId.equals(ack.getGameOrderId())) {
                //修改用户积分缓存
                updatePlayScoreStore(ack.getBetUserId(), ack.getBetScore() * -1, false, ack.getGameOrderId(), "下注Notice");
            }
        } catch (Exception ex) {
            logger.error("NoticeBetRoundDoBet error," + toStringUserInfo() + ", msg = " + ex.getMessage(), ex);
        }
    }

    /**
     * Notice_通知结算结果
     */
    private void NoticeSettleResult(int functionId, byte[] bytes) {
        try {
            JoloGame.JoloGame_Notice2Client_SettleRound_SettleReq ack = JoloGame.JoloGame_Notice2Client_SettleRound_SettleReq.parseFrom(bytes);
            StringBuilder logStr = new StringBuilder();
            logStr.append("Notice Header, functionId->" + functionId + ", functionName->" + FunctionIdHolder.GetFunctionName(functionId) + toStringReplaceLine()
                    + ", Body, roomId->" + ack.getRoomId() + ", tableId->" + ack.getTableId() + ", gameOrderId->" + ack.getGameOrderId() + toStringReplaceLine()
                    + toStringUserInfo() + toStringReplaceLine());

            toStringPlayersScoreStore(ack.getGameOrderId());

            List<JoloGame.JoloGame_TablePlay_PlayerSettleInfo> allList = new ArrayList<>();
            List<JoloGame.JoloGame_TablePlay_PlayerSettleInfo> winList = ack.getSettleWinListList();
            List<JoloGame.JoloGame_TablePlay_PlayerSettleInfo> loseList = ack.getSettleLoseListList();
            allList.addAll(winList);
            allList.addAll(loseList);

            for (JoloGame.JoloGame_TablePlay_PlayerSettleInfo settleInfo : winList) {
                logStr.append("PlayerSettleInfo->" + toStringReplaceLine());
                logStr.append("    UserId->" + settleInfo.getUserId() + ",NickName->" + getNickName(settleInfo.getUserId()) + ", SeatNum->" + settleInfo.getSeatNum()
                        + ", WinLoseScore->" + settleInfo.getWinLoseScore() + ", PlayScoreStore->" + settleInfo.getPlayScoreStore() + toStringReplaceLine());

                //修改用户积分缓存
                updatePlayScoreStore(settleInfo.getUserId(), settleInfo.getPlayScoreStore(), true, ack.getGameOrderId(), "结算结果");

                //判断是否需要赠送礼物给全桌（如果赢钱玩家是机器人，那么按照概率随机执行赠送礼物给全桌）
                if (settleInfo.getUserId().equals(userId)
                        && settleInfo.getWinLoseScore() > (getBootAmount() * Config.ROBOT_GIVE_GIFT_WIN_BOOTAMOUNT)) {
                    logger.debug("userId->{}, nickName->{}, winLoseScore->{}, bootAmount->{}, value->{}",
                            settleInfo.getUserId(), getNickName(settleInfo.getUserId()), settleInfo.getWinLoseScore(), getBootAmount(), (getBootAmount() * Config.ROBOT_GIVE_GIFT_WIN_BOOTAMOUNT));
                    int ranomNum = RandomTools.getRandomNum(100);
                    if (ranomNum <= Config.ROBOT_GIVE_GIFT) {
                        ExecuteGiveGifts();
                    }
                }
            }
            logger.debug(logStr.toString());

            logger.debug("收到结算消息：onTablePlayers->" + onTablePlayers.size() + ", null != onTablePlayers.get(userId)->" + (null != onTablePlayers.get(userId)) + toStringUserInfo());

            //检查机器人是否应该退出该牌局
            CheckForQuit();

            resetInitVariable();

            //清除Dealer玩法时保存的临时缓存数据
            this.currPlayType = "";
            this.isNeedFoldBy91001005 = false;
        } catch (Exception ex) {
            logger.error("NoticeSettleResult error," + toStringUserInfo() + ", msg = " + ex.getMessage(), ex);
        }
    }

    /**
     * 结算历史
     */
    private void NoticeSettleHistory(int functionId, byte[] bytes) {
        try {
            JoloGame.JoloGame_Notice2Client_SettleRound_HistoryReq ack = JoloGame.JoloGame_Notice2Client_SettleRound_HistoryReq.parseFrom(bytes);
            logger.debug("Notice Header, functionId->" + functionId + ", functionName->" + FunctionIdHolder.GetFunctionName(functionId) + ", Body, GameOrderId->" + ack.getGameOrderId() + ", size->" + ack.getHistoryList().size()
                    + ", alreadyPlayRound->" + alreadyPlayRound + ", playRoundCount->" + playRoundCount + toStringUserInfo());

            alreadyPlayRound++;

        } catch (Exception ex) {
            logger.error("NoticeSettleHistory error," + toStringUserInfo() + ", msg = " + ex.getMessage(), ex);
        }
    }

    //通知：下注倍数
    private void NoticeBetMultipleInfo(int functionId, byte[] bytes) {
        try {
            JoloGame.JoloGame_Notice2Client_BetMultipleInfoReq ack = JoloGame.JoloGame_Notice2Client_BetMultipleInfoReq.parseFrom(bytes);
            logger.info("通知：下注倍数, functionId->{}, functionName->{}, Body, roomId->{}, tableId->{}, GameOrderId->{}, BtnsMultipleCount->{}, {}",
                    functionId, FunctionIdHolder.GetFunctionName(functionId), ack.getRoomId(), ack.getTableId(), ack.getGameOrderId(), ack.getBtnsMultipleCount(), toStringUserInfo());

            if (ack.getRoomId().equals(roomId)
                    && ack.getTableId().equals(tableId)
                    /*&& ack.getGameOrderId().equals(gameOrderId)*/) {
                List<Integer> multipleList = ack.getBtnsMultipleList();
                ExecuteBetMultiple(multipleList);
            }
        } catch (Exception ex) {
            logger.error("NoticeSettleHistory error," + toStringUserInfo() + ", msg = " + ex.getMessage(), ex);
        }
    }

    private String getNickName(String userId) {
        PlayerInfo playerInfo = onTablePlayers.get(userId);
        if (null != playerInfo) {
            return playerInfo.getNickName();
        }

        com.jule.db.entities.User user = EntityProxy.OBJ.get(userId, com.jule.db.entities.User.class);
        if (null != user) {
            return user.getNick_name();
        }
        return "UNKNOWN";
    }


    public void updatePlayScoreStore(String userId, double amount, boolean isAll, String gameOrderId, String updateType) {
        PlayerInfo playerInfo = onTablePlayers.get(userId);
        if (null == playerInfo) {
            logger.info("Function:updatePlayScoreStore(), 用户已站起， onTablePlayers中找不到指定用户。 userId->{}, nickName->{}", userId, getNickName(userId));
            return;
        }
        double oldPlayScoreStore = playerInfo.getPlayScoreStore();
        if (isAll) {
            playerInfo.setPlayScoreStore(amount);
        } else {
            playerInfo.setPlayScoreStore(playerInfo.getPlayScoreStore() + amount);
        }
        logger.debug("更新用户积分, userId->{}, nickName->{}, updateType->{}, oldScore->{}, amount->{}, newScore->{}, gameOrderId->{}. {}",
                playerInfo.getUserId(), playerInfo.getNickName(), updateType, oldPlayScoreStore, amount, playerInfo.getPlayScoreStore(), gameOrderId
                , toStringReplaceLine() + toStringUserInfo());
    }

    /**
     * 重置初始化变量（每局开始时重置）
     */
    private void resetInitVariable() {
        this._baseBetScore = 0;
        setCardModel(null);
        this.cardModel = null;
        standupPlayers.clear();
    }

    /**
     * 检查机器人是否需要退出
     */
    private void CheckForQuit() {
        //todo
    }

    private String toStringUserInfo() {
        return ",CURRENT_CLIENT_USER:::userId->" + userId + ", nickName->" + nickName + ", roomId->" + roomId + ", tableId->" + tableId + ", gameOrderId->" + gameOrderId;
    }

    private void toStringPlayersScoreStore(String gameOrderId) {
        for (PlayerInfo playerInfo : onTablePlayers.values()) {
            logger.debug("牌局结束， gameOrderId->{}, userId->{}, nickName->{}, playerScoreStore->{}", gameOrderId, playerInfo.getUserId(), playerInfo.getNickName(), playerInfo.getPlayScoreStore());
        }
    }
}
