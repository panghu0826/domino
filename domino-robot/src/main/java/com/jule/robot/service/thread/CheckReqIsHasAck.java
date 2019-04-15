package com.jule.robot.service.thread;

import com.jule.robot.service.holder.RobotClientHolder;
import com.jule.robot.service.websocket.BaseWebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;

public class CheckReqIsHasAck implements Runnable  {
    private final static Logger logger = LoggerFactory.getLogger(CheckReqIsHasAck.class);

    @Override
    public void run() {
        logger.debug("检查是否存在Req未收到回复的情况， req列表数量->{}", RobotClientHolder.reqSendTimeMap.size());
        for(String key : RobotClientHolder.reqSendTimeMap.keySet()){
            Object[] arrObj = RobotClientHolder.reqSendTimeMap.get(key);
            Date sendDate = (Date)arrObj[0];
            int functionId = (int)arrObj[1];
            Date now = new Date();
            long timeDifference = now.getTime() - sendDate.getTime();
            if(timeDifference > 2000){
                logger.error("发现未收到返回的Req，key->{}, functionId->{}, timeDifference->{}", key, functionId, timeDifference);
            }
        }
    }
}
