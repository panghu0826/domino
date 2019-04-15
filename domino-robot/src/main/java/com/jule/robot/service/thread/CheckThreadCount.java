package com.jule.robot.service.thread;

import com.jule.robot.service.holder.RobotClientHolder;
import com.jule.robot.valve.gate.CreateRobotConnThreadPoolManager;
import com.jule.robot.valve.gate.RobotJoinTableThreadPoolManager;
import com.jule.robot.valve.gate.RobotThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckThreadCount implements Runnable{
    private final static Logger logger = LoggerFactory.getLogger(CheckThreadCount.class);

    @Override
    public void run() {
        try{
            logger.warn("");
            logger.warn(RobotThreadPoolManager.getInstance().getPoolInfo());
            logger.warn(RobotJoinTableThreadPoolManager.getInstance().getPoolInfo());
            logger.warn(CreateRobotConnThreadPoolManager.getInstance().getPoolInfo());
            logger.warn("当前活跃机器人数量->{}", RobotClientHolder.getCurrClientCnt());
            logger.warn("");
        }catch (Exception ex){
            logger.error("CheckThreadCount Exception. ", ex);
        }
    }
}
