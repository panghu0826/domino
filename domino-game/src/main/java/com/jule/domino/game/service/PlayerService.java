package com.jule.domino.game.service;

import com.jule.domino.game.model.PlayerInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
@Slf4j
public class PlayerService {
    @Getter@Setter
    private static final Map<String, String> PLAYER_MAP = new ConcurrentHashMap();

    private static class SingletonHolder {
        protected static final PlayerService instance = new PlayerService();
    }

    public static final PlayerService getInstance() {
        return PlayerService.SingletonHolder.instance;
    }
    /**
     * 是否存在玩家
     */
    public PlayerInfo getPlayer(String userId){

        return null;
    }

    /**
     * 玩家登陆
     */
    public void onPlayerLogin(String playerId) {
        if (PLAYER_MAP.put(playerId, playerId) != null) {
            log.debug("玩家 " + playerId + " 重连登陆");
        }
    }

    /**
     * 玩家登出
     */
    public void onPlayerLoutOut(String playerId) {
        PLAYER_MAP.remove(playerId);
    }
}
