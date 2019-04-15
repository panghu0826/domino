package com.jule.robot.service.thread;

import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.enums.RedisConst;
import com.jule.robot.config.Config;
import com.jule.robot.service.holder.RobotUserHolder;
import com.jule.robot.service.holder.WaitUserHolder;
import com.jule.robot.util.RandomTools;
import com.jule.robot.util.RunTime;
import com.jule.robot.valve.gate.RobotConfigHandler;
import com.jule.robot.valve.gate.RobotJoinTableThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 检查单个桌子是否需要增加陪打机器人
 */
public class CheckSingleTableIsNeedRobot implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(CheckSingleTableIsNeedRobot.class);
    private int gameId;
    private String roomId;
    private String tableId;
    private long ante;
    private static int RATE_PLAYERCNT_2 = 20;//10
    private static int RATE_PLAYERCNT_3 = RATE_PLAYERCNT_2 + 20;//30%
    private static int RATE_PLAYERCNT_4 = RATE_PLAYERCNT_3 + 60;//30%
    private static int RATE_PLAYERCNT_5 = RATE_PLAYERCNT_4 + 20;//20%
    private static int RATE_PLAYERCNT_6 = RATE_PLAYERCNT_5 + 20;//20%


    public CheckSingleTableIsNeedRobot(int gameId, String roomId, String tableId, long ante) {
        this.gameId = gameId;
        this.roomId = roomId;
        this.tableId = tableId;
        this.ante = ante;

        String robotJoinPct = StoredObjManager.get(RedisConst.ROBOT_JOIN_PCT.getProfix());
        String[] num = robotJoinPct.split(",");
        if (num.length >= 5) {
            RATE_PLAYERCNT_2 = Integer.parseInt(num[0]);
            RATE_PLAYERCNT_3 = RATE_PLAYERCNT_2+Integer.parseInt(num[1]);
            RATE_PLAYERCNT_4 = RATE_PLAYERCNT_3+Integer.parseInt(num[2]);
            RATE_PLAYERCNT_5 = RATE_PLAYERCNT_4+Integer.parseInt(num[3]);
            RATE_PLAYERCNT_6 = RATE_PLAYERCNT_5+Integer.parseInt(num[4]);
        }
    }

    @Override
    public void run() {
        try {
            RunTime runTime = new RunTime("CheckSingleTableIsNeedRobot");

            String key4tableSeat = new StringBuilder().append(RedisConst.TABLE_SEAT.getProfix()).append(gameId).append(roomId).append(tableId).toString();
            Map<String, String> seatMap = JedisPoolWrap.getInstance().hGetAll(key4tableSeat);
            if (null == seatMap || seatMap.size() == 0 || seatMap.size() > 2) {
                return;
            }

            boolean isHaveRealUser = false;
            for (String userId : seatMap.values()) {
                if (!RobotUserHolder.getRobotMap().containsKey(userId)) {
                    isHaveRealUser = true; //桌子上有真人玩家
                    break;
                }
            }

            logger.debug("发现有人的桌子，key4tableSeat->{}, seatMapSize->{}, isHaveRealUser->{}", key4tableSeat, seatMap.size(), isHaveRealUser);

            //如果牌桌不是可以让机器人在空桌时加入的桌子，并且桌上玩家都是机器人，那么不进行补位操作
            if (!RobotUserHolder.isNullTableForJoinRobot(gameId + "", roomId, tableId)) {
                if (!isHaveRealUser) {
                    logger.debug("桌子上都是机器人，不进行补位。gameId->{}, roomId->{}, tableId->{}, playerCnt->{}, key4tableSeat->{}", gameId, roomId, tableId, seatMap.size(), key4tableSeat);
                    //如果桌上的用户都是机器人，那么不补位其它机器人
                    return;
                }
            }

            //检查是否达到指定人数的等待时间，需要机器人补位入桌
            checkIsNeedJoinRobotByWaitSec(gameId, roomId, tableId, seatMap.size());
            runTime.getSecsBuyAlreadyRun(1);
        } catch (Exception ex) {
            logger.error("CheckSingleTableIsNeedRobot Exception, gameId->{},roomId->{},tableId->{},msg->{}",
                    gameId, roomId, tableId, ex.getMessage(), ex);
        }
    }

    private void checkIsNeedJoinRobotByWaitSec(int gameId, String roomId, String tableId, int onSeatPlayerCnt) {
        StringBuilder cacheKeyPrefix = new StringBuilder().append(gameId).append("|").append(roomId).append("|").append(tableId).append("|"); //缓存前缀（验证机器人入场时间间隔的缓存）
        String keyTableUserId = cacheKeyPrefix.append(onSeatPlayerCnt).toString();

        if (onSeatPlayerCnt > 3) {
            return;
        }

        Map<String, AtomicInteger> waitMap = null;
        int configWaitSec = 999999;
        switch (onSeatPlayerCnt) {
            case 1:
                waitMap = WaitUserHolder.SINGLE_USER_WAIT_MAP;
                configWaitSec = RobotConfigHandler.getRobotCommonConfig().getSinglePlayerWaitSec();
                break;
            case 2:
                waitMap = WaitUserHolder.TWO_USER_WAIT_MAP;
                configWaitSec = 5; //写死两人时机器人入场时间为5秒
                break;
            case 3:
                waitMap = WaitUserHolder.THREE_USER_WAIT_MAP;
                configWaitSec = RobotConfigHandler.getRobotCommonConfig().getTwoPlayerWaitSec(); //因为要增加机器人入场人数，但配置表中没有第三个人的入场时间，因此将两个人的配置调整为三人入场配置
                break;
        }

        //直接设定机器人加入时间2秒
        configWaitSec = 2;

        AtomicInteger atomicNumber = new AtomicInteger(1);
        if (waitMap.containsKey(keyTableUserId)) {
            atomicNumber = waitMap.get(keyTableUserId);
        }

        if (null != atomicNumber
                && atomicNumber.get() * Config.CHECK_THREAD_INTERVAL_SEC >= configWaitSec) {
            //达到入桌条件
            if (waitMap.containsKey(keyTableUserId)) {
                waitMap.remove(keyTableUserId); //清除计数缓存
            }

            /*
             * 根据概率，判断应加入桌子的机器人数量
             */
            int rateForJoinNumber = RandomTools.getRandomNum(100);
            int needJoinRobotNumber = 2;
            if (rateForJoinNumber < RATE_PLAYERCNT_2) {
                needJoinRobotNumber = 2;
            } else if (rateForJoinNumber >= RATE_PLAYERCNT_2 && rateForJoinNumber <= RATE_PLAYERCNT_3) {
                needJoinRobotNumber = 3;
            } else if (rateForJoinNumber > RATE_PLAYERCNT_3 && rateForJoinNumber <= RATE_PLAYERCNT_4) {
                needJoinRobotNumber = 4;
            } else if (rateForJoinNumber > RATE_PLAYERCNT_4 && rateForJoinNumber <= RATE_PLAYERCNT_5) {
                needJoinRobotNumber = 5;
            }else if (rateForJoinNumber > RATE_PLAYERCNT_5) {
                needJoinRobotNumber = 6;
            }

            int needNum = needJoinRobotNumber - onSeatPlayerCnt;
            if (needNum <= 0){
                return;
            }

            for (int i = 1; i <= needNum; i++) {
                //开启机器人入桌的独立线程池，来处理机器人入桌逻辑
                RobotJoinTableThreadPoolManager.getInstance().addTask(new JoinRobotToTableThread(gameId, roomId, tableId, "陪打机器人"));
            }
        } else {
            //未达到入桌条件，增加计数
            atomicNumber.incrementAndGet();
            waitMap.put(keyTableUserId, atomicNumber);
        }
    }
}
