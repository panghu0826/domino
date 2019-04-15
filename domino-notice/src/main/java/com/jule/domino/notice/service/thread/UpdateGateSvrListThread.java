package com.jule.domino.notice.service.thread;

import com.jule.domino.notice.config.Config;
import com.jule.domino.notice.model.GateServerInfo;
import com.jule.domino.notice.service.JedisService;
import com.jule.domino.notice.valve.gate.GateConnectPool;
import com.jule.domino.notice.valve.gate.GateServerGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 维护GateSvr列表
 */
public class UpdateGateSvrListThread implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(UpdateGateSvrListThread.class);
    public void run() {
        while (true) {
            try {
                String[] gameIds = Config.NOTICE_BIND_GAMEID.split(":");
                for(String gameId : gameIds){
                    List<GateServerInfo> svrInfoList = JedisService.getInstance().GetGateSvrList(gameId);
                    logger.debug("gateSvrInfoList size = " + svrInfoList.size()+" gameId:"+gameId);
                    if (null != svrInfoList && svrInfoList.size() > 0) {
                        for (GateServerInfo svrInfo : svrInfoList) {
                            boolean isHaveConnPool = GateServerGroup.getInstance().isHaveConnectPool(svrInfo.getGateSvrId());
                            logger.debug("gateSvrId = " + svrInfo.getGateSvrId() + ", ip=" + svrInfo.getIp() + ", port=" + svrInfo.getPort() + ", isHasConnPool = " + isHaveConnPool);
                            if (!isHaveConnPool) {
                                //创建连接池
                                GateConnectPool connPool = new GateConnectPool(svrInfo.getGateSvrId());
                                //注册连接池
                                //GateServerGroup.getInstance().registerGameServerGroup(connPool);
                            }
                        }
                    }
                }
                Thread.sleep(1000 * 30);
            } catch (Exception ex) {
                logger.error("UpdateGateSvrListThread ERROR, msg = " + ex.getMessage(), ex);
            }
        }
    }
}
