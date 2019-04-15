package com.jule.domino.game.service;

import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.game.config.Config;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.GameRoomTableSeatRelationModel;
import com.jule.domino.base.service.AvaibleGameSvrId;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;

@Slf4j
public class TerminateService {
    /**
     * 清理掉本服务器的桌子
     */
    public static void destroy() {
        log.error("the server is exit, destroy is start");
        String[] gameIds = Config.GAME_IDS.split(":");
        for (String gameId : gameIds) {
            StoredObjManager.zrem(GameConst.GAME_STATE_ + gameId, RegisterService.GAME_SERVER_ID+"");
        }

        //把本服务器桌子发给消息中间件
        Map<String, GameRoomTableSeatRelationModel> map = StoredObjManager.hgetAll(RedisConst.TABLE_GAME_SERVER.getProfix(),
                GameRoomTableSeatRelationModel.class);
        Iterator<GameRoomTableSeatRelationModel> iterator = map.values().iterator();
        while (iterator.hasNext()) {
            GameRoomTableSeatRelationModel model = iterator.next();
            if (model.getGameSvr().equals(RegisterService.GAME_SERVER_ID)) {
                String svrId = AvaibleGameSvrId.getInstance().getAvaibleGameSvrId(model.getGameId());
                model.setGameSvr(svrId);
            }
        }

    }
}
