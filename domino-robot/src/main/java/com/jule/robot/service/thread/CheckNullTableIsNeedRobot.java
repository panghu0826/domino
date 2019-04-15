package com.jule.robot.service.thread;

import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;
import com.jule.db.entities.RoomConfigModel;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.RoomTableRelationModel;
import com.jule.robot.config.Config;
import com.jule.robot.service.holder.RobotClientHolder;
import com.jule.robot.service.holder.RobotUserHolder;
import com.jule.robot.service.holder.RoomConfigHolder;
import com.jule.robot.valve.gate.RobotJoinTableThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CheckNullTableIsNeedRobot implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(CheckNullTableIsNeedRobot.class);
    private int gameId;

    public CheckNullTableIsNeedRobot(int gameId){
        this.gameId = gameId;
//        logger.warn("CheckTableIsNeedRobot gameList->{}, SINGLE_USER_WAIT_MAP->{}, TWO_USER_WAIT_MAP->{}", GAMEID_LIST.size(), SINGLE_USER_WAIT_MAP.size(), TWO_USER_WAIT_MAP.size());
    }

    public void run() {
        try {
            if(Config.ROBOT_JOIN_NULL_TABLE == 1){
                logger.warn(">>>>>>>>>>>>>>>>>>>检查是否有空桌需要加入机器人-开始。 gameId->{}", gameId);
                int totalOnLinePlayer = 0;
                for (RoomConfigModel roomConfigModel : RoomConfigHolder.getInstance().getAllRoomConfig()) {
                    String roomId = roomConfigModel.getRoomId();
                    String key4RoomTableRelation = new StringBuilder().append(RedisConst.TABLE_INSTANCE.getProfix()).append(gameId).append(roomId).toString();
                    Map<String, String> tableMap = JedisPoolWrap.getInstance().hGetAll(key4RoomTableRelation);
                    for (String key : tableMap.keySet()) {
                        RoomTableRelationModel model = StoredObjManager.getStoredObjInMap(RoomTableRelationModel.class, key4RoomTableRelation, key);
                        if (null != model) {
                            String tableId = model.getTableId();
                            String cacheKeyPrefix = new StringBuilder().append(gameId).append("|").append(roomId).append("|").append(tableId).append("|").toString(); //缓存前缀（验证机器人入场时间间隔的缓存）
                            String key4tableSeat = new StringBuilder().append(RedisConst.TABLE_SEAT.getProfix()).append(gameId).append(roomId).append(tableId).toString();
                            Map<String, String> seatMap = JedisPoolWrap.getInstance().hGetAll(key4tableSeat);

                            boolean isClearedDirtyDate = false; //是否清除了脏数据（Redis中残留的已断开连接的机器人账号数据）
                            if(seatMap.size() > 0){
                                logger.info("桌子状态检查：gameId->{}, roomId->{}, tableId->{}, seatMap.size->{}, ante->{}, key4tableSeat->{}",
                                        gameId, roomId, tableId, seatMap.size(), roomConfigModel.getAnte(), key4tableSeat);
                                for(String keyField : seatMap.keySet()){
                                    String userId = seatMap.get(keyField);
                                    boolean isRobot = RobotUserHolder.getRobotMap().containsKey(userId);
                                    if(isRobot){
                                        boolean isActiveRobotClient = RobotClientHolder.getClientMap().containsKey(userId);
                                        if(!isActiveRobotClient) {
                                            isClearedDirtyDate = true;
                                            boolean isSuccDel = StoredObjManager.hdel(key4tableSeat, keyField);
                                            logger.error("桌子残留机器人数据：gameId->{}, roomId->{}, tableId->{}, seatMap.size->{}, ante->{}, userId->{}, isRobot->{}, isSuccDelRedis->{}, isActiveRobotClient->{}",
                                                    gameId, roomId, tableId, seatMap.size(), roomConfigModel.getAnte(), userId, isRobot, isSuccDel, isActiveRobotClient);
                                        }
                                    }
                                }
                            }

                            if(isClearedDirtyDate) {
                                seatMap = JedisPoolWrap.getInstance().hGetAll(key4tableSeat);
                                logger.error("桌子状态检查-Redis数据清除后：gameId->{}, roomId->{}, tableId->{}, seatMap.size->{}, ante->{}, key4tableSeat->{}",
                                        gameId, roomId, tableId, seatMap.size(), roomConfigModel.getAnte(), key4tableSeat);
                            }
                            totalOnLinePlayer += seatMap.size();

                            //如果需要增加机器人到空桌中
                            if(seatMap.size() == 0 && Config.ROBOT_JOIN_NULL_TABLE == 1 && roomConfigModel.getAnte() <= Config.ROBOT_JOIN_NULL_MAX_ANTE){
                                int joinNullTableMaxRobotNum = Config.ROBOT_DESK_ROBOT_NUM;
                                logger.warn("向 空桌 补位机器人 {} 个，gameId->{}, roomId->{}, tableId->{}, ante->{}", joinNullTableMaxRobotNum, gameId, roomId, tableId, roomConfigModel.getAnte());
                                for(int i=0; i<joinNullTableMaxRobotNum; i++){
                                    RobotUserHolder.setNullTableToJoinRobot(gameId+"", roomId, tableId);
                                    RobotJoinTableThreadPoolManager.getInstance().addTask(new JoinRobotToTableThread(gameId, roomId, tableId, "加入空桌"));
                                }
                                Thread.sleep(500); //暂停500毫秒，给之前入桌的机器人留出处理时间。避免同时并发入桌，造成Game服务器崩溃
                            }
                        }
                    }
                }
                logger.warn("<<<<<<<<<<<<<<<<<<<检查是否有空桌需要加入机器人-结束。 gameId->{}", gameId);
            }
            //logger.debug("totalOnLinePlayer = " + totalOnLinePlayer);
        } catch (Exception ex) {
            logger.error("CheckTableIsNeedRobot ERROR, msg = " + ex.getMessage(), ex);
        }
        finally {
//            logger.warn("CheckTableIsNeedRobot Success. gameId->{}", gameId);
        }
    }

//    private void joinRobot(int gameId, String roomId, String tableId) throws InterruptedException {
//        String userId = RobotUserHolder.getUser();
//
//        long minBuyIn = RobotConfigHandler.getRobotCommonConfig().getBuyinMin();
//        long maxBuyIn = RobotConfigHandler.getRobotCommonConfig().getBuyinMax();
//        long buyInScoreRate = RandomTools.getRandomNum((int)(maxBuyIn - minBuyIn)) + minBuyIn;
//        long minScore4JoinTable = RoomConfigHolder.getInstance().getRoomConfig(roomId).getMinScore4JoinTable();
//        long buyInScore =  minScore4JoinTable * buyInScoreRate / 100;
//        if(buyInScore <= 100){
//            buyInScore = RandomTools.getRandomNum(990)+4000;
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
}
