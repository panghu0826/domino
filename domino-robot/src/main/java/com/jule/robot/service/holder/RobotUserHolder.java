package com.jule.robot.service.holder;

import com.jule.db.entities.User;
import com.jule.db.proxy.EntityProxy;
import com.jule.robot.service.websocket.AuthWebsocketClient;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class RobotUserHolder {
    private final static Logger logger = LoggerFactory.getLogger(RobotUserHolder.class);
    private final static LinkedBlockingQueue userQueue = new LinkedBlockingQueue();
    @Getter
    @Setter
    private final static Map<String, String> robotMap = new HashMap<>();
    private static Set<String> joinRobotNullTableList = new HashSet<>();

    public static void InitRobotUser() {
        try {
            try {
                List<String> robotList = (List<String>) EntityProxy.OBJ.queryNativeSQL("select id " +
                        "     from user " +
                        "     where channel_id = 'robot' and money = 0 ",null);
                Collections.shuffle(robotList);
                for (String userId : robotList) {
                    robotMap.put(userId, userId);
                    addUser(userId);
                }
                logger.warn("Total robot count -> " + robotList.size());
            } catch (Exception e) {
                logger.debug("No override properties found");
            }


//            int redisUserCnt = 0;
//
//            //先取出redis中的机器人账号，并加入到用户队列中
//            String userId;
//            while(null != (userId = getUserFromRedis())){
//                logger.debug("找到redis中的机器人账号："+userId);
//                addUser(userId);
//                redisUserCnt++;
//            }
//            logger.debug("从Rdies中找到了 "+redisUserCnt+" 个用户.");
//
//            int minRobotSize = 3;
//            if(minRobotSize - redisUserCnt > 0){
//                logger.debug("Redis中账号不足，需要创建 "+ (minRobotSize - redisUserCnt) +" 个新的游客.");
//                //当Redis中账号不足时，创建新的游客账号
//                for(int i=0; i < 10-redisUserCnt; i++){
//                    CreateGuest();
//                }
//            }
        } catch (Exception ex) {
            logger.error("InitRobotUser error.", ex);
        }
    }

    public static void addUser(String userId) {
        try{
            if (!userQueue.contains(userId)) {
                userQueue.put(userId);
            }
            logger.warn("向机器人用户列表中增加一个账号："+userId+", 账号列表剩余数量->" + userQueue.size());
        }catch (Exception ex){
            logger.error("Robot addUser error. msg->"+ex.getMessage(), ex);
        }
    }

    public static String getUser(String sourceFrom) throws InterruptedException {
        Object objUserId = null;
        if (userQueue.size() > 0) {
            objUserId = userQueue.take();
            logger.warn("获取一个机器人账号，来源->{}，userId->{}, 账号列表剩余数量->{}", sourceFrom, objUserId, userQueue.size());
        } else {
            //InitRobotUser();
            logger.warn("机器人账号不足，重新加载");
        }


        return (String) objUserId;
    }

    public static void CreateGuest() {
        logger.debug("申请创建一个新的游客账号。");
        //调用login方法，生成guest账号
        AuthWebsocketClient client = new AuthWebsocketClient(FunctionIdHolder.GAME_ID_TeenPatti_Normal, "create_robot", "", "");
    }

    /**
     * 获得因异常退出所导致的，身上有货币的机器人账号列表
     * @return
     */
    public static void returnMoneyByExceptionRobot(){
        List<User> robotList = EntityProxy.OBJ.getResultList("obj.channel_id = 'robot' and obj.money > 0 ",null, User.class);
        logger.warn("Exception quit robot cnt->{}", robotList.size());
        for(User user : robotList){
            RobotMoneyPoolHolder.robotReturnMoneyToPool(user.getId(), user.getMoney());
        }
    }

    public static int getRobotCnt(){
        return userQueue.size();
    }

    /**
     * 将一个桌子信息，放置到已加入机器人的空桌子列表中
     * @param gameId
     * @param roomId
     * @param tableId
     */
    public static void setNullTableToJoinRobot(String gameId, String roomId, String tableId){
        joinRobotNullTableList.add(gameId+"_"+roomId+"_"+tableId);
    }

    /**
     * 判断桌子是否是加入了机器人的空桌子，如果是，那么此桌子内机器人将不执行站起离桌操作（因为真人离开而触发的离桌操作）。
     * @param gameId
     * @param roomId
     * @param tableId
     * @return
     */
    public static boolean isNullTableForJoinRobot(String gameId, String roomId, String tableId ){
        return joinRobotNullTableList.contains(gameId+"_"+roomId+"_"+tableId);
    }
}
