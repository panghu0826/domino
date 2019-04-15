package com.jule.robot.service.thread;

import com.jule.robot.Robot;
import com.jule.robot.util.RunTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateRobotConnThread implements Runnable{
    private final static Logger logger = LoggerFactory.getLogger(CreateRobotConnThread.class);

    private int gameId;
    private String roomId;
    private String tableId;
    private String userId;

    public CreateRobotConnThread(int gameId, String roomId, String tableId, String userId){
        this.gameId = gameId;
        this.roomId = roomId;
        this.tableId = tableId;
        this.userId = userId;
    }

    @Override
    public void run() {
        try{
            RunTime rumTime = new RunTime("CreateRobotConnThread");
            new Robot(gameId, userId, roomId, tableId);
            rumTime.getSecsBuyAlreadyRun(1);
        }catch (Exception ex){
            logger.error("CreateRobotConnThread Exception, msg->{}", ex.getMessage(), ex);
        }
    }
}
