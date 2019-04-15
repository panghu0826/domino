package com.jule.domino.base.service;

import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.GameSvrRelationModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Set;

@Slf4j
public class AvaibleGameSvrId {

    private static class SingletonHolder {
        protected static final AvaibleGameSvrId instance = new AvaibleGameSvrId();
    }

    public static final AvaibleGameSvrId getInstance() {
        return AvaibleGameSvrId.SingletonHolder.instance;
    }

    /**
     * 获取当前负载最低的游戏服务器id 这里返回的是数字形式的字符串
     *
     * @return
     */
    public String getAvaibleGameSvrId(String gameId) {
        //StoredObjManager.
        Set<String> ret = JedisPoolWrap.getInstance().zrange(GameConst.GAME_STATE_ + gameId, 0, 0);
        log.info("avaible game server size " + ret.size());
        if (ret != null && ret.size() > 0) {
            for (String s : ret) {
                return s;
            }
        }
        return "";
    }

    /**
     * 查找与游戏服列表对比有则为true
     * @param gameId
     * @param gameSvrId
     * @return
     */
    public boolean isContainGameSvrId(String gameId, String gameSvrId) {
        boolean b = false;
        Set<GameSvrRelationModel> list = StoredObjManager.smembers(RedisConst.GAME_SVR_LIST.getProfix() + gameId, GameSvrRelationModel.class);
        Iterator<GameSvrRelationModel> iterator = list.iterator();
        while (iterator.hasNext()){
            GameSvrRelationModel gameSvrRelationModel = iterator.next();
            if(gameSvrRelationModel.getGameSvrId().equals(gameSvrId)){
                b = true;
            }
        }
        return b;
    }
}
