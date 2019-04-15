package com.jule.domino.game.service;

import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.base.model.RoomTableRelationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * 记录玩家在哪个房间里
 */
public class UserTableService {
    private final static Logger logger = LoggerFactory.getLogger(UserTableService.class);
    private static final Map<String, RoomTableRelationModel> USERID_TO_TABLE = new HashMap<>();

    private static class SingletonHolder {
        protected static final UserTableService instance = new UserTableService();
    }

    public static final UserTableService getInstance() {
        return UserTableService.SingletonHolder.instance;
    }

    /**
     * 玩家进入桌子
     */
    public void onPlayerInTable(String gameId, PlayerInfo player, AbstractTable table) {
        USERID_TO_TABLE.put(player.getPlayerId(), new RoomTableRelationModel(gameId,table.getRoomId(), table.getTableId(),table.getTableStateEnum().getValue()));
    }

    /**
     * 玩家主动离开桌子
     */
    public void onPlayerOutTable(PlayerInfo player) {
        USERID_TO_TABLE.remove(player.getPlayerId());
    }

    /**
     * 玩家离线，需要检查是观战还是参战状态
     */
    public AbstractTable getTableByUserId(long userId) {
        RoomTableRelationModel roomTable = USERID_TO_TABLE.remove(userId + "");
        if (roomTable != null) {
            return TableService.getInstance().getTable(roomTable.getGameId(),roomTable.getRoomId(), roomTable.getTableId());
        }
        return null;
    }

    /**
     * 获取玩家所在的桌子
     * @param userId
     * @return
     */
    public AbstractTable getTableByUserId(String userId) {
        RoomTableRelationModel roomTable = USERID_TO_TABLE.get(userId);
        if (roomTable != null) {
            return TableService.getInstance().getTable(roomTable.getGameId(),roomTable.getRoomId(), roomTable.getTableId());
        }
        return null;
    }
}
