package com.jule.domino.game.gate.pool.game;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xujian 2017-12-15
 * game 连接池组
 */
public class SubGameServerGroup {
    private final static Logger logger = LoggerFactory.getLogger(SubGameServerGroup.class);
    private final static Map<Integer, SubGameConnectPool> GAME_CONNECT_POOL_MAP = new ConcurrentHashMap<>();

    private static class SingletonHolder {
        protected static final SubGameServerGroup instance = new SubGameServerGroup();
    }

    public static final SubGameServerGroup getInstance() {
        return SubGameServerGroup.SingletonHolder.instance;
    }

    /**
     * 注册连接池
     *
     * @param gameConnectPool
     */
    public synchronized void registerGameServerGroup(SubGameConnectPool gameConnectPool) {
        if (!GAME_CONNECT_POOL_MAP.containsKey(gameConnectPool.getGameServerId())) {
            GAME_CONNECT_POOL_MAP.put(gameConnectPool.getGameServerId(), gameConnectPool);
        }
    }

    /**
     * @param gameSvrId
     * @return
     */
    public ChannelHandlerContext getConnect(int gameSvrId) {
        SubGameConnectPool gameConnectPool = GAME_CONNECT_POOL_MAP.get(gameSvrId);
        if (gameConnectPool != null) {
            return gameConnectPool.getConnection();
        }
        return null;
    }

}
