package com.jule.robot.service.thread;

import com.jule.robot.model.GateServerInfo;
import com.jule.robot.service.JedisService;
import com.jule.robot.valve.gate.GateConnectPool;
import com.jule.robot.valve.gate.GateServerGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 维护GateSvr列表
 */
public class UpdateGateSvrListThread implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(UpdateGateSvrListThread.class);

    public void run() {
        while (true) {
            try {
                GateServerInfo svrInfo = new GateServerInfo();
                svrInfo.setGateSvrId("10.0.0.62:8080");

                boolean isHaveConnPool = GateServerGroup.getInstance().isHaveConnectPool(svrInfo.getGateSvrId());
                logger.debug("gateSvrId = " + svrInfo.getGateSvrId() + ", ip=" + svrInfo.getIp() + ", port=" + svrInfo.getPort() + ", isHasConnPool = " + isHaveConnPool);
                if (!isHaveConnPool) {
                    //创建连接池
                    GateConnectPool connPool = new GateConnectPool(svrInfo.getGateSvrId());
                }

                Thread.sleep(1000 * 30);//没用到
            } catch (Exception ex) {
                logger.error("UpdateGateSvrListThread ERROR, msg = " + ex.getMessage(), ex);
            }
        }
    }
}
