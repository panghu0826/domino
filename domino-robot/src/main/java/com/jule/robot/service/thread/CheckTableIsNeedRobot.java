package com.jule.robot.service.thread;

import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;
import com.jule.db.entities.RoomConfigModel;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.RoomTableRelationModel;
import com.jule.robot.service.holder.RobotUserHolder;
import com.jule.robot.service.holder.RoomConfigHolder;
import com.jule.robot.util.RunTime;
import com.jule.robot.valve.gate.RobotThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * 检查房间是否需要加入机器人
 */
public class CheckTableIsNeedRobot implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(CheckTableIsNeedRobot.class);
    private int gameId;

    public CheckTableIsNeedRobot(int gameId){
        this.gameId = gameId;
    }

    public void run() {
        try {
            RunTime rumTime = new RunTime("CheckTableIsNeedRobot");
            for (RoomConfigModel roomConfigModel : RoomConfigHolder.getInstance().getAllRoomConfig()) {
                String roomId = roomConfigModel.getRoomId();
                String key4RoomTableRelation = new StringBuilder().append(RedisConst.TABLE_INSTANCE.getProfix()).append(gameId).append(roomId).toString();
                logger.debug("========="+key4RoomTableRelation);
                Map<String, String> tableMap = JedisPoolWrap.getInstance().hGetAll(key4RoomTableRelation);
                if (null == tableMap || tableMap.size() == 0){
                    logger.debug("gameId = {},roomId = {},tableMap = null, key = {}",gameId,roomId,key4RoomTableRelation);
                }
                for (String key : tableMap.keySet()) {
                    RoomTableRelationModel model = StoredObjManager.getStoredObjInMap(RoomTableRelationModel.class, key4RoomTableRelation, key);
                    if (null != model) {
                        String tableId = model.getTableId();
                        String key4tableSeat = new StringBuilder().append(RedisConst.TABLE_SEAT.getProfix()).append(gameId).append(roomId).append(tableId).toString();
                        Map<String, String> seatMap = JedisPoolWrap.getInstance().hGetAll(key4tableSeat);
                        if (seatMap != null && seatMap.size() > 0){
                            logger.debug("桌子上有{}人game={},room={},table={}", seatMap.size(),gameId, model.getRoomId(), tableId);
                        }

                        if(logger.isInfoEnabled()){
                            if(seatMap.size() > 0 && seatMap.size() <= 1) {
                                boolean isHaveRealUser = false;
                                for(String userId : seatMap.values()){
                                    if (!RobotUserHolder.getRobotMap().containsKey(userId)) {
                                        isHaveRealUser = true; //桌子上有真人玩家
                                        break;
                                    }
                                }
                                logger.info("发现有人的桌子，key4tableSeat->{}, seatMapSize->{}, isHaveRealUser->{}", key4tableSeat, seatMap.size(), isHaveRealUser);
                            }
                        }

                        if(null != seatMap && seatMap.size() >= 1){
                            //开一个新线程执行单桌的补位检查，用以保证现在所处的大循环能够快速结束，以免造成每2秒执行一次的大循环发生并发堆积的情况
                            RobotThreadPoolManager.getInstance().addTask(new CheckSingleTableIsNeedRobot(gameId, roomId, tableId, roomConfigModel.getAnte()));
                        }
                    }
                }
            }
            //logger.debug("totalOnLinePlayer = " + totalOnLinePlayer);
            rumTime.getSecsBuyAlreadyRun(1);
        } catch (Exception ex) {
            logger.error("CheckTableIsNeedRobot ERROR, msg = " + ex.getMessage(), ex);
        }
    }

//    public void run() {
////        if(WaitUserHolder.GAMEID_CHECKING_MAP.containsKey(gameId+"")){
////            logger.warn("上一个检查线程还在执行中，return当前线程。gameId->{}", gameId);
////            return;
////        }
//
//        try {
//            WaitUserHolder.GAMEID_CHECKING_MAP.put(gameId+"", gameId+"");
//            int totalOnLinePlayer = 0;
//            for (RoomConfigModel roomConfigModel : RoomConfigHolder.getInstance().getAllRoomConfig()) {
//                String roomId = roomConfigModel.getRoomId();
//                String key4RoomTableRelation = new StringBuilder().append(RedisConst.TABLE_INSTANCE.getProfix()).append(gameId).append(roomId).toString();
//                Map<String, String> tableMap = JedisPoolWrap.getInstance().hGetAll(key4RoomTableRelation);
//                logger.debug("检查房间是否有需要加入的桌子, gameId->{}, roomId->{}, key4RoomTableRelation->{}， tableMap->{}",
//                        gameId, roomConfigModel.getRoomId(), key4RoomTableRelation, tableMap);
//                for (String key : tableMap.keySet()) {
//                    RoomTableRelationModel model = StoredObjManager.getStoredObjInMap(RoomTableRelationModel.class, key4RoomTableRelation, key);
//                    if (null != model) {
//                        String tableId = model.getTableId();
//                        String cacheKeyPrefix = new StringBuilder().append(gameId).append("|").append(roomId).append("|").append(tableId).append("|").toString(); //缓存前缀（验证机器人入场时间间隔的缓存）
//
//                        //logger.debug("mapKey = " + key + ", roomId = " + model.getRoomId() + ", tableId = " + tableId);
//                        String key4tableSeat = new StringBuilder().append(RedisConst.TABLE_SEAT.getProfix()).append(gameId).append(roomId).append(tableId).toString();
//                        Map<String, String> seatMap = JedisPoolWrap.getInstance().hGetAll(key4tableSeat);
//                        if(null == seatMap){
//                            return;
//                        }
//                        //logger.debug("SeatMAP = "+seatMap);
//
//                        totalOnLinePlayer += seatMap.size();
//
//                        /*
//                         * 检查桌子只有一个用户，并且该用户不是机器人时，该用户的等待时间（等待时间以checkTableThread的轮询次数来判断）
//                         */
//                        if(seatMap.size() == 1){
//                            Object[] arrUserId = seatMap.values().toArray();
//                            String realPlayerUserId = (String)arrUserId[0];
//                            if(!RobotUserHolder.isNullTableForJoinRobot(gameId+"", roomId, tableId)){
//                                if(RobotUserHolder.getRobotMap().containsKey(realPlayerUserId)){
//                                    //如果桌上唯一的用户是机器人，那么不补位其它机器人
//                                    continue;
//                                }
//                            }
//                            String keyTableUserId = new StringBuilder().append(cacheKeyPrefix).append(realPlayerUserId).toString();
//                            if(logger.isDebugEnabled()) {
//                                logger.debug("seatMap.size->1, gameId->" + gameId + ", roomId->" + roomId + ", tableId->" + tableId + ", realPlayerUserId = " + realPlayerUserId
//                                        + " ,mapIsContains->" + WaitUserHolder.SINGLE_USER_WAIT_MAP.containsKey(keyTableUserId)
//                                        + ", SINGLE_USER_WAIT_MAP.value->" + (null == WaitUserHolder.SINGLE_USER_WAIT_MAP.get(keyTableUserId) ? "null" : WaitUserHolder.SINGLE_USER_WAIT_MAP.get(keyTableUserId).get()));
//                            }
//                            if(WaitUserHolder.SINGLE_USER_WAIT_MAP.containsKey(keyTableUserId)){
//                                AtomicInteger atomicNumber = WaitUserHolder.SINGLE_USER_WAIT_MAP.get(keyTableUserId);
//                                if(null != atomicNumber
//                                        && atomicNumber.get() * Config.CHECK_THREAD_INTERVAL_SEC >= RobotConfigHandler.getRobotCommonConfig().getSinglePlayerWaitSec()){
//                                    //达到入桌条件
//                                    atomicNumber = null;
//                                    WaitUserHolder.SINGLE_USER_WAIT_MAP.remove(keyTableUserId); //清除计数缓存
//
//                                    joinRobot(gameId, roomId, tableId);
//                                }else {
//                                    //未达到入桌条件，增加计数
//                                    atomicNumber.incrementAndGet();
//                                    WaitUserHolder.SINGLE_USER_WAIT_MAP.put(keyTableUserId, atomicNumber);
//                                }
//                            }else {
//                                AtomicInteger atomicNumber = new AtomicInteger();
//                                atomicNumber.incrementAndGet();
//                                WaitUserHolder.SINGLE_USER_WAIT_MAP.put(keyTableUserId, atomicNumber);
//                            }
//                        }
//
//                        /*
//                         * 检查桌子有两个用户时，他们已坐在一起的游戏时间（游戏时间以checkTableThread的轮询次数来判断）
//                         */
//                        if(seatMap.size() == 2) {
//                            Object[] arrUserId = seatMap.values().toArray();
//                            String realPlayerUserId_1 = (String)arrUserId[0];
//                            String realPlayerUserId_2 = (String)arrUserId[1];
//
//                            if(!RobotUserHolder.isNullTableForJoinRobot(gameId+"", roomId, tableId)){
//                                if(RobotUserHolder.getRobotMap().containsKey(realPlayerUserId_1)
//                                        && RobotUserHolder.getRobotMap().containsKey(realPlayerUserId_2)){
//                                    //如果桌上的用户都是机器人，那么不补位其它机器人
//                                    continue;
//                                }
//                            }
//                            String keyTableUserId = cacheKeyPrefix + realPlayerUserId_1+"|"+realPlayerUserId_2;
//                            if(logger.isDebugEnabled()) {
//                                logger.debug("seatMap.size->2, gameId->" + gameId + ", roomId->" + roomId + ", tableId->" + tableId + ", realPlayerUserId_1->" + realPlayerUserId_1 + ", realPlayerUserId_2->" + realPlayerUserId_2
//                                        + " ,mapIsContains->" + WaitUserHolder.TWO_USER_WAIT_MAP.containsKey(keyTableUserId)
//                                        + ", TWO_USER_WAIT_MAP.value->" + (null == WaitUserHolder.TWO_USER_WAIT_MAP.get(keyTableUserId) ? "null" : WaitUserHolder.TWO_USER_WAIT_MAP.get(keyTableUserId).get()));
//                            }
//                            if(WaitUserHolder.TWO_USER_WAIT_MAP.containsKey(keyTableUserId)){
//                                AtomicInteger atomicNumber = WaitUserHolder.TWO_USER_WAIT_MAP.get(keyTableUserId);
//                                if(null != atomicNumber
//                                        && atomicNumber.get() * Config.CHECK_THREAD_INTERVAL_SEC >= RobotConfigHandler.getRobotCommonConfig().getTwoPlayerWaitSec()){
//                                    //达到入桌条件
//                                    WaitUserHolder.TWO_USER_WAIT_MAP.remove(keyTableUserId); //清除计数缓存
//
//                                    joinRobot(gameId, roomId, tableId);
//                                }else {
//                                    //未达到入桌条件，增加计数
//                                    atomicNumber.incrementAndGet();
//                                    WaitUserHolder.TWO_USER_WAIT_MAP.put(keyTableUserId, atomicNumber);
//                                }
//                            }else {
//                                AtomicInteger atomicNumber = new AtomicInteger();
//                                atomicNumber.incrementAndGet();
//                                WaitUserHolder.TWO_USER_WAIT_MAP.put(keyTableUserId, atomicNumber);
//                            }
//                        }
//
//                        if(seatMap.size() > 0) {
//                            int robotNum = getRobotNum(seatMap);
//                            if(logger.isDebugEnabled()) {
//                                logger.debug("gameId->" + gameId + ", roomId->" + roomId + ", tableId->" + tableId + ", 当前牌桌内游戏人数 = " + seatMap.size() + ", 机器人数：" + robotNum
//                                        + seatMap);
//                            }
////                                if(logger.isDebugEnabled()){
////                                    for (String userId : seatMap.values()) {
////                                        com.jule.db.entities.User user = EntityProxy.OBJ.get(userId, com.jule.db.entities.User.class);
////                                        logger.debug("---->>>gameId->" + gameId + ", roomId->" + roomId + ", tableId->" + tableId
////                                                + ", userId->" + userId + ", nickName->" + user.getNick_name() + ", isRobot->" + RobotUserHolder.getRobotMap().containsKey(userId));
////                                    }
////                                }
//                        }
//                    }
//                }
//            }
//            //logger.debug("totalOnLinePlayer = " + totalOnLinePlayer);
//        } catch (Exception ex) {
//            logger.error("CheckTableIsNeedRobot ERROR, msg = " + ex.getMessage(), ex);
//        }
//        finally {
////            logger.warn("CheckTableIsNeedRobot Success. gameId->{}", gameId);
//            WaitUserHolder.GAMEID_CHECKING_MAP.remove(gameId+"");
//        }
//    }

//    private void joinRobot(int gameId, String roomId, String tableId) throws InterruptedException {
//        String userId = RobotUserHolder.getUser();
//
//        long minBuyIn = RobotConfigHandler.getRobotCommonConfig().getBuyinMin();
//        long maxBuyIn = RobotConfigHandler.getRobotCommonConfig().getBuyinMax();
//        long buyInScoreRate = RandomTools.getRandomNum(70)+30;
//        long minScore4JoinTable = RoomConfigHolder.getInstance().getRoomConfig(roomId).getMinScore4JoinTable();
//        long buyInScore =  minScore4JoinTable * buyInScoreRate;
//        if(buyInScore <= 100){
//            buyInScore = getRandomNum(990)+4000;
//        }
//
//        //获得买入货币
//        com.jule.db.entities.User user = EntityProxy.OBJ.get(userId, com.jule.db.entities.User.class);
//        if(null != user){
//            boolean isSuccAddMoney = RobotMoneyPoolHolder.robotBuyinFromPool(gameId+"", userId, user, buyInScore);
//
//            logger.warn("Need Join Robot, userId = " + userId + ", roomId = " + roomId + ", tableId = " + tableId+", buyInScore = "+ buyInScore
//                    +", MinScore4JoinTable->"+minScore4JoinTable+". buyInScoreRate->"+buyInScoreRate+", isSuccSetMoney->"+isSuccAddMoney);
//
//            //给机器人账号增加所需代入的货币
//            if(isSuccAddMoney){
//                new Robot(gameId, userId, roomId, tableId);
//            }else{
//                RobotUserHolder.addUser(userId);
//                logger.error("补位机器人失败，因：获取货币失败。userId->{}", userId);
//            }
//        }else{
//            logger.error("补位机器人失败，因：在DB中找不到机器人账号。userId->{}", userId);
//        }
//    }

    /**
     * 判断桌内有没有机器人
     */
    private int getRobotNum(Map<String, String> seatMap) {
        int robotNum = 0;
        Iterator<Map.Entry<String, String>> iter = seatMap.entrySet().iterator();
        while (iter.hasNext()) {
            String userId = iter.next().getValue();
            if (RobotUserHolder.getRobotMap().containsKey(userId)) {
                robotNum++;
            }
        }
        return robotNum;
    }

    public long getRandomNum(int maxNum){
        Random random = new Random(System.currentTimeMillis()+System.nanoTime());
        return random.nextInt(maxNum)+1;
    }
}
