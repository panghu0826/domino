package com.jule.domino.gate.vavle.game;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xujian 2017-12-15
 * game 连接池组
 */
public class GameServerGroup {
    private final static Logger logger = LoggerFactory.getLogger(GameServerGroup.class);
    private final static Map<Integer, GameConnectPool> GAME_CONNECT_POOL_MAP = new ConcurrentHashMap<>();

    private static class SingletonHolder {
        protected static final GameServerGroup instance = new GameServerGroup();
    }

    public static final GameServerGroup getInstance() {
        return GameServerGroup.SingletonHolder.instance;
    }

    /**
     * 注册连接池
     *
     * @param gameConnectPool
     */
    public synchronized void registerGameServerGroup(GameConnectPool gameConnectPool) {
        if (!GAME_CONNECT_POOL_MAP.containsKey(gameConnectPool.getGameServerId())) {
            logger.debug("网关服注册GameServerId："+gameConnectPool.getGameServerId());
            GAME_CONNECT_POOL_MAP.put(gameConnectPool.getGameServerId(), gameConnectPool);
            logger.debug("网关服连接池"+GAME_CONNECT_POOL_MAP.get(gameConnectPool.getGameServerId())+" "
                    +(int)gameConnectPool.getGameServerId());
        }
    }

    /**
     * @param gameSvrId
     * @return
     */
    public ChannelHandlerContext getConnect(int gameSvrId) {
        logger.debug("网关gameSvrId："+gameSvrId+"-----"+GAME_CONNECT_POOL_MAP.get(gameSvrId));
        logger.debug("网关连接池"+(GAME_CONNECT_POOL_MAP.get(gameSvrId) != null));
        GameConnectPool gameConnectPool = GAME_CONNECT_POOL_MAP.get(gameSvrId);
        if (gameConnectPool != null) {
            return gameConnectPool.getConnection();
        }
        logger.info("GameServerGroup.getConnect() is null" );
        return null;
    }

}
