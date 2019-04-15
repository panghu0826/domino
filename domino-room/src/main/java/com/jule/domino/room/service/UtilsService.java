package com.jule.domino.room.service;

import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.GameRoomTableSeatRelationModel;
import com.jule.domino.base.service.AvaibleGameSvrId;
import com.jule.domino.room.model.TableInfo;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilsService {
    private final static Logger logger = LoggerFactory.getLogger(UtilsService.class);

    private static class SingletonHolder {
        protected static final UtilsService instance = new UtilsService();
    }

    public static final UtilsService getInstance() {
        return UtilsService.SingletonHolder.instance;
    }

    public synchronized String getGameSvr(TableInfo table, String gameId){
        GameRoomTableSeatRelationModel relationModel = StoredObjManager.getStoredObjsInMap(GameRoomTableSeatRelationModel.class,
                RedisConst.TABLE_GAME_SERVER.getProfix(),
                RedisConst.TABLE_GAME_SERVER.getField() + table.getPlayType() + table.getRoomId() + table.getTableId());
        logger.info("gameSvr:" + (relationModel == null ? null : relationModel.getGameSvr()));
        if (relationModel != null && StringUtils.isNotEmpty(relationModel.getGameSvr())
                && AvaibleGameSvrId.getInstance().isContainGameSvrId(gameId,relationModel.getGameSvr())) {
            return relationModel.getGameSvr();
        } else {

            String gameSvrId = AvaibleGameSvrId.getInstance().getAvaibleGameSvrId(gameId);
            if (!StringUtil.isNullOrEmpty(gameSvrId)) {
                relationModel = new GameRoomTableSeatRelationModel("" + table.getPlayType(), table.getRoomId(), table.getTableId(), 0, gameSvrId);
                StoredObjManager.hset(
                        RedisConst.TABLE_GAME_SERVER.getProfix(),
                        RedisConst.TABLE_GAME_SERVER.getField() + table.getPlayType() + table.getRoomId() + table.getTableId(),
                        relationModel);
                return gameSvrId;//没有时前端提示 "服务器爆满"
            }
        }
        return "";
    }
}
