package com.jule.domino.game.gate.service;

import com.alibaba.fastjson.JSONObject;
import com.jule.core.jedis.JedisPoolWrap;
import com.jule.domino.game.gate.model.SubGameGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 游戏服务发现服务
 */
public class LsbService {
    private final static Logger logger = LoggerFactory.getLogger(LsbService.class);
    private final static String SERVER_KEY = "teen_patti_gold_GAME";

    private final static Map<Integer, SubGameGroup> SUB_GAME_GROUP_MAP = new ConcurrentHashMap<>();

    private static class SingletonHolder {
        protected static final LsbService instance = new LsbService();
    }

    public static final LsbService getInstance() {
        return LsbService.SingletonHolder.instance;
    }

    public void onServerStartUp() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> discover(), 0, 30 * 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * 服务监听启动后调用
     */
    private void discover() {
        Map<String, String> ret = JedisPoolWrap.getInstance().hGetAll(SERVER_KEY);
        if (ret == null || ret.size() == 0) {
            logger.warn("Get SubGame List Fail");
        }

        //负责插入于更新在线人数
        ret.forEach((flag, json) -> {
            String[] subGameId_ServerId = flag.split("_");
            int subGameId = Integer.parseInt(subGameId_ServerId[0]);

            if (!SUB_GAME_GROUP_MAP.containsKey(subGameId)) {
                SUB_GAME_GROUP_MAP.put(subGameId, new SubGameGroup(subGameId));
            }
            JSONObject jsonObject = JSONObject.parseObject(json);
            SUB_GAME_GROUP_MAP.get(subGameId).updateSubGameInfo(jsonObject);
        });
    }

}
