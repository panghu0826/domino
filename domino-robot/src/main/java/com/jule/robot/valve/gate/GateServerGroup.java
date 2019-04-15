package com.jule.robot.valve.gate;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xujian 2017-12-15
 * game 连接池组
 */
public class GateServerGroup {
    private final static Logger logger = LoggerFactory.getLogger(GateServerGroup.class);
    private final static Map<String, GateConnectPool> GATE_CONNECT_POOL_MAP = new ConcurrentHashMap<String, GateConnectPool>();

    private static class SingletonHolder {
        protected static final GateServerGroup instance = new GateServerGroup();
    }

    public static final GateServerGroup getInstance() {
        return GateServerGroup.SingletonHolder.instance;
    }

    /**
     * 注册连接池
     *
     * @param gateConnectPool
     */
    public synchronized void registerGameServerGroup(GateConnectPool gateConnectPool) {
        if (!GATE_CONNECT_POOL_MAP.containsKey(gateConnectPool.getGateServerId())) {
            GATE_CONNECT_POOL_MAP.put(gateConnectPool.getGateServerId(), gateConnectPool);
            logger.debug("!!!-----> GATE_CONNECT_POOL_MAP -> " + (null != GATE_CONNECT_POOL_MAP ? GATE_CONNECT_POOL_MAP : "null"));
        }
    }

    /**
     * 判断是否存在此GateSvr的连接池
     *
     * @param gateServerId
     * @return
     */
    public synchronized boolean isHaveConnectPool(String gateServerId) {
        GateConnectPool gateConnectPool = GATE_CONNECT_POOL_MAP.get(gateServerId);
        if (gateConnectPool != null) {
            return true;
        }
        return false;
    }

    /**
     * @param gateServerId
     * @return
     */
    public ChannelHandlerContext getConnect(String gateServerId) {
        logger.debug("getConnect， gateServerId -> " + gateServerId);
        GateConnectPool gateConnectPool = GATE_CONNECT_POOL_MAP.get(gateServerId);
        logger.debug("getConnect， gateServerId -> " + gateServerId + ", null == gateConnectPool -> " + (null == gateConnectPool));
        if (gateConnectPool != null) {
            return gateConnectPool.getConnection();
        }
        return null;
    }

}
