package com.jule.domino.game.service;

import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.RoomTableRelationModel;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.room.model.TableStatusInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * 通过修改redis 状态维护房间服务器缓存，包括 入桌/退桌 坐下/站起
 */
@Slf4j
public class RoomStateService {
    private final static String ROOM_STAT = "ROOM_STAT_";

    private static class SingletonHolder {
        protected static final RoomStateService instance = new RoomStateService();
    }

    public static final RoomStateService getInstance() {
        return RoomStateService.SingletonHolder.instance;
    }



    /**
     * @param roomId
     * @param tableId
     * @return RoomTableRelationModel
     */
    public RoomTableRelationModel getExistTable( String tableId) {
        RoomTableRelationModel ret = StoredObjManager.getStoredObjInMap(RoomTableRelationModel.class,
                RedisConst.TABLE_INSTANCE.getProfix(),
                RedisConst.TABLE_INSTANCE.getField() + tableId);

        log.info("getExistTable() tableId ->"+tableId+(ret!=null?" "+ret.toString():" ret is null"));
        if (ret != null) {
            return ret;
        }
        return null;
    }

    /**
     * @param tableId
     * @return
     */
    private TableStatusInfo getTableStatusInfo(String tableId) {
        TableStatusInfo ret = null;
        byte[] bytes = JedisPoolWrap.getInstance().hGet(ROOM_STAT, tableId);
        if (bytes != null) {
            ret = TableStatusInfo.fromProtocBuf(bytes);
        }

        return ret;
    }

    /**
     * 需要对结果做断言
     *
     * @return
     */
    private boolean updateTableStatusInfo(String tableId, TableStatusInfo tableStatusInfo) {
        return JedisPoolWrap.getInstance().hSet(ROOM_STAT, tableId, tableStatusInfo.toBuf());
    }

    /**
     * 玩家加入桌子
     */
    public final void onPlayerJoinTable(final AbstractTable table) {
        TableStatusInfo statusInfo = getTableStatusInfo(table.getTableId());
        if (statusInfo != null) {
            log.debug("statusInfo 更新前->" + statusInfo.toString());
            statusInfo.setCurrentWaitingCount(statusInfo.getCurrentWaitingCount() + 1);
            updateTableStatusInfo(table.getTableId(), statusInfo);
            log.debug("statusInfo 更新后->" + statusInfo.toString());
        }
    }
}
