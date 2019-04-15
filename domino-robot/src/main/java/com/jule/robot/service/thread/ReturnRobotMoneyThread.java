package com.jule.robot.service.thread;

import com.jule.robot.service.holder.RobotUserHolder;

public class ReturnRobotMoneyThread implements Runnable {
    @Override
    public void run() {
        //退还异常机器人的货币
        RobotUserHolder.returnMoneyByExceptionRobot();
    }
}
