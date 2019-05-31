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
public class GameServerGroup {
    private final static Logger logger = LoggerFactory.getLogger(GameServerGroup.class);
    private final static Map<Integer, GameConnectPool> GAME_CONNECT_POOL_MAP = new ConcurrentHashMap<>();
    private GameConnectPool gameConnectPool;

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
            GAME_CONNECT_POOL_MAP.put(gameConnectPool.getGameServerId(), gameConnectPool);
            logger.debug("现在的游戏服务器id：{}",gameConnectPool.getGameServerId());
            this.gameConnectPool = gameConnectPool;
        }
    }

    /**
     * @param gameSvrId
     * @return
     */
    public ChannelHandlerContext getConnect(int gameSvrId) {
        logger.debug("gameSvrId："+gameSvrId+"-----"+GAME_CONNECT_POOL_MAP.get(gameSvrId));
        logger.debug("---------------"+(GAME_CONNECT_POOL_MAP.get(gameSvrId) != null));
        if(gameConnectPool == null) {
            logger.debug("通过gameSvrId获取链接！！！");
            gameConnectPool = GAME_CONNECT_POOL_MAP.get(gameSvrId);
        }else {
            logger.debug("自己存的链接！！！");
        }
        if (gameConnectPool != null) {
            return gameConnectPool.getConnection();
        }
        logger.info("GameServerGroup.getConnect() is null" );
        return null;
    }

}
