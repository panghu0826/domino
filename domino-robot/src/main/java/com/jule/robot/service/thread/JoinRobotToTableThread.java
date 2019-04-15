package com.jule.robot.service.thread;

import com.jule.db.entities.RoomConfigModel;
import com.jule.db.proxy.EntityProxy;
import com.jule.robot.service.holder.RobotClientHolder;
import com.jule.robot.service.holder.RobotMoneyPoolHolder;
import com.jule.robot.service.holder.RobotUserHolder;
import com.jule.robot.service.holder.RoomConfigHolder;
import com.jule.robot.util.NumUtils;
import com.jule.robot.util.RandomTools;
import com.jule.robot.util.RunTime;
import com.jule.robot.valve.gate.CreateRobotConnThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoinRobotToTableThread implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(RobotClientHolder.class);
    private int gameId;
    private String roomId;
    private String tableId;
    private String sourceFrom;

    public JoinRobotToTableThread(int gameId, String roomId, String tableId, String sourceFrom){
        this.gameId = gameId;
        this.roomId = roomId;
        this.tableId = tableId;
        this.sourceFrom = sourceFrom;
    }

    @Override
    public void run() {
        try{
            joinRobot(gameId, roomId, tableId, sourceFrom);
        }catch (Exception ex){
            logger.error("JoinRobotToTableThread Exception, msg->{}", ex.getMessage(), ex);
        }
    }

    /**
     * 加入机器人到牌桌
     * @param gameId
     * @param roomId
     * @param tableId
     * @param sourceFrom
     */
    private void joinRobot(int gameId, String roomId, String tableId, String sourceFrom) {
        String userId = "";
        try{
            RunTime rumTime = new RunTime("JoinRobotToTableThread");
            userId = RobotUserHolder.getUser(sourceFrom+",gameId="+gameId+",roomId="+roomId+",tableId="+tableId);

            RoomConfigModel roomConfig = RoomConfigHolder.getInstance().getRoomConfig(roomId);
            long minScore4JoinTable = roomConfig.getMinScore4JoinTable();
            long ante = roomConfig.getAnte();
            double randomDouble = RandomTools.getRandomDouble(0.39);
            //double buyInScore = (ante * 10 * 20) * (RandomTools.getRandomDouble(2.60) + 0.79);
            double buyInScore = NumUtils.double2Decimal((ante * 25) * (RandomTools.getRandomDouble(6.60) + 1));
            //获得买入货币
            com.jule.db.entities.User user = EntityProxy.OBJ.get(userId, com.jule.db.entities.User.class);
            if(null != user){
                boolean isSuccAddMoney = RobotMoneyPoolHolder.robotBuyinFromPool(gameId+"", userId, user, buyInScore);

                logger.info("Need Join Robot, userId = " + userId + ", roomId = " + roomId + ", tableId = " + tableId+", buyInScore = "+ buyInScore
                        +", MinScore4JoinTable->"+minScore4JoinTable+". randomDouble->"+randomDouble+", isSuccSetMoney->"+isSuccAddMoney);

                //给机器人账号增加所需代入的货币
                if(isSuccAddMoney){
                    CreateRobotConnThreadPoolManager.getInstance().addTask(new CreateRobotConnThread(gameId, roomId, tableId, userId));
                }else{
                    RobotUserHolder.addUser(userId);
                    logger.error("补位机器人失败，因：获取货币失败。userId->{}", userId);
                }
            }else{
                logger.error("补位机器人失败，因：在DB中找不到机器人账号。userId->{}", userId);
            }
            rumTime.getSecsBuyAlreadyRun(1);
        }catch (Exception ex){
            logger.error("JoinRobot Exception, userId->{},roomId->{},tableId->{},gameId->{}, msg->{}",
                    userId, roomId, tableId, gameId, ex.getMessage(), ex);
        }
    }
}
