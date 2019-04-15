package com.jule.domino.game.notice.pool;

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
    private final static Map<String, GateConnectPool> GATE_CONNECT_POOL_MAP = new ConcurrentHashMap<>();

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
//            GATE_CONNECT_POOL_MAP.put("192.168.0.14:10052", gateConnectPool);
            logger.debug("!!!-----> GATE_CONNECT_POOL_MAP -> " + (null != GATE_CONNECT_POOL_MAP ? GATE_CONNECT_POOL_MAP : "null"));
        }else{
            logger.debug("11111111111111111111111111111111");
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
     * @param ipAndPort
     * @return
     */
    public synchronized ChannelHandlerContext getConnect(String ipAndPort) {
        GateConnectPool gateConnectPool = GATE_CONNECT_POOL_MAP.get(ipAndPort);
        logger.debug("需要根据 {} 获取链接",ipAndPort);
        logger.debug("gate连接池数量：{}",GATE_CONNECT_POOL_MAP.size());
        for(String str : GATE_CONNECT_POOL_MAP.keySet()){
            logger.debug("=================================================================="+str);
        }
        logger.debug("getConnect， gateServerId -> " + ipAndPort + ", null == gateConnectPool -> " + (null == gateConnectPool));
        if (gateConnectPool != null) {
            return gateConnectPool.getConnection(ipAndPort);
        }
        return null;
    }

}
