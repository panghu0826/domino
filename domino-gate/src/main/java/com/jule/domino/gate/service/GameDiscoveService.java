package com.jule.domino.gate.service;

import com.google.common.base.Strings;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.GameSvrRelationModel;
import com.jule.domino.gate.config.Config;
import com.jule.domino.gate.vavle.game.GameConnectPool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author xujian 2017-12-15
 * 游戏服务发现服务
 */
public class GameDiscoveService {
    private final static Logger logger = LoggerFactory.getLogger(GameDiscoveService.class);

    /**
     * 本地缓存的游戏服务器列表
     */
    private final static Map<String, Integer> CACHED_GAMESERVER_TABLE = new LinkedHashMap();

    private static class SingletonHolder {
        protected static final GameDiscoveService instance = new GameDiscoveService();
    }

    public static final GameDiscoveService getInstance() {
        return GameDiscoveService.SingletonHolder.instance;
    }

    public void onServerStartUp() {
        String[] gameIds = Config.GAME_IDS.split(":");
        for (String gameId : gameIds) {
            if (Strings.isNullOrEmpty(gameId)) {
                continue;
            }
            discover(gameId);
            //开始发现线程
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> discover(gameId), 15000, 30 * 1000, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 服务监听启动后调用
     */
    private void discover(String gameId) {
        //Map<String, String> ret = JedisPoolWrap.getInstance().hGetAll(GameConst.SERVER_KEY+gameId);
        Set<GameSvrRelationModel> list = StoredObjManager.smembers(RedisConst.GAME_SVR_LIST.getProfix() + gameId, GameSvrRelationModel.class);
        if (list == null || list.size() == 0) {
            logger.warn("获取游戏服务器列表失败");
        }
        list.forEach(gameSvrRelationModel -> {
            String field = gameSvrRelationModel.getAddress() + gameSvrRelationModel.getGameSvrId();
            String value = StoredObjManager.hget(RedisConst.GAME_SVR_EXPIRE.getProfix() + gameId,
                    RedisConst.GAME_SVR_EXPIRE.getField() + field);
            long lastTime = 0;
            if (StringUtils.isNotEmpty(value)) {
                lastTime = Long.parseLong(value);
            }
            if (System.currentTimeMillis() - lastTime > GameConst.offlineGameSec * 1000L) {
                StoredObjManager.srem(RedisConst.GAME_SVR_LIST.getProfix() + gameId, gameSvrRelationModel);
               return;
            }
            int _gameSvrId = Integer.parseInt(gameSvrRelationModel.getGameSvrId());
            logger.debug(RedisConst.GAME_SVR_LIST + "--------------------------------------------服务器注册的gamesvrId：" + _gameSvrId);
            String address = gameSvrRelationModel.getAddress();
            if (!CACHED_GAMESERVER_TABLE.containsKey(address)) {
                CACHED_GAMESERVER_TABLE.put(address, _gameSvrId);
                logger.info("发现GAME服务器->" + address + ",GameSvrId->" + _gameSvrId);
                new GameConnectPool(_gameSvrId, address);
            }

        });
       /* list.forEach((address, gameSvrId) -> {
            int _gameSvrId = Integer.parseInt(gameSvrId);
            logger.debug(GameConst.SERVER_KEY+"--------------------------------------------服务器注册的gamesvrId："+_gameSvrId);
            if (!CACHED_GAMESERVER_TABLE.containsKey(address)) {
                CACHED_GAMESERVER_TABLE.put(address, _gameSvrId);
                logger.info("发现GAME服务器->" + address + ",GameSvrId->" + _gameSvrId);
                new GameConnectPool(_gameSvrId, address);
            }
        });
        */
    }

}
