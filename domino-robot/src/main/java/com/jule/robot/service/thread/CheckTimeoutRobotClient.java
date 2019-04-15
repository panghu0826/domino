package com.jule.robot.service.thread;

import com.jule.robot.config.Config;
import com.jule.robot.model.ClientInfo;
import com.jule.robot.service.holder.RobotClientHolder;
import com.jule.robot.util.RunTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CheckTimeoutRobotClient implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(CheckTimeoutRobotClient.class);

    @Override
    public void run() {
        try {
            RunTime runTime = new RunTime("CheckTimeoutRobotClient");
            Map<String, ClientInfo> clientMap = RobotClientHolder.getClientMap();
            for(String userId : clientMap.keySet()){
                ClientInfo clientInfo = clientMap.get(userId);
                if(null != clientInfo){
                    long diffTime = (System.currentTimeMillis() - clientInfo.getCreateTime()) / 1000; //得到相差的秒数
                    int timeoutLimitSecond = 60 * Config.ROBOT_TIMEOUT_MINUTE;
                    if(diffTime > timeoutLimitSecond && Config.TEST_TYPE_IS_STRESS == 0){
                        logger.error("发现连接超过指定时限的用户，关闭此用户的socket连接。userId->{}, createTime->{}, diffTime->{}秒, 超时时限->{}",
                                userId, clientInfo.getCreateTime(), diffTime, timeoutLimitSecond);
                        RobotClientHolder.closeClient(userId, clientInfo.getClient(), "连接超时的机器人");
                    }
                }
            }
            runTime.getSecsBuyAlreadyRun();
        }catch (Exception ex){
            logger.error("CheckTimeoutRobotClient Exception. ", ex);
        }
    }
}
