package com.jule.domino.room.service;

import com.jule.core.jedis.JedisPoolWrap;
import com.jule.domino.room.model.PlayerInfo;
import com.jule.domino.room.model.TableInfo;
import com.jule.domino.room.model.TableStatusInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xujian 2017-12-18
 * 通过修改redis 状态维护房间服务器缓存，包括 入桌/退桌 坐下/站起
 */
public class RoomStateService {
    private final static Logger logger = LoggerFactory.getLogger(RoomStateService.class);
    private final static String ROOM_STAT = "ROOM_STAT_" + 10000;//todo

    private static class SingletonHolder {
        protected static final RoomStateService instance = new RoomStateService();
    }

    public static final RoomStateService getInstance() {
        return RoomStateService.SingletonHolder.instance;
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
    public final void onPlayerJoinTable(final TableInfo table) {
        TableStatusInfo statusInfo = getTableStatusInfo(table.getTableId());
        if (statusInfo != null) {
            logger.debug("statusInfo 更新前->" + statusInfo.toString());
            statusInfo.setCurrentWaitingCount(statusInfo.getCurrentWaitingCount() + 1);
            updateTableStatusInfo(table.getTableId(), statusInfo);
            logger.debug("statusInfo 更新后->" + statusInfo.toString());
        }
    }

    /**
     * 玩家离开桌子
     */
    public final void onPlayerLeaveTable(final TableInfo table) {
        TableStatusInfo statusInfo = getTableStatusInfo(table.getTableId());
        if (statusInfo != null) {
            logger.debug("statusInfo 更新前->" + statusInfo.toString());
            statusInfo.setCurrentWaitingCount(statusInfo.getCurrentWaitingCount() - 1);
            updateTableStatusInfo(table.getTableId(), statusInfo);
            logger.debug("statusInfo 更新后->" + statusInfo.toString());
        }
    }

    /**
     * 玩家坐下
     */
    public final void onPlayerSitDown(final TableInfo table, final PlayerInfo player) {
        TableStatusInfo statusInfo = getTableStatusInfo(table.getTableId());
        if (statusInfo != null) {
            logger.debug("statusInfo 更新前->" + statusInfo.toString());
            statusInfo.setCurrentPlayerCount(statusInfo.getCurrentPlayerCount() + 1);
            statusInfo.setCurrentWaitingCount(statusInfo.getCurrentWaitingCount() - 1);
            statusInfo.getInfoMap().put(player.getPlayerId(), new TableStatusInfo.PlayerInfo(table.getTableId(), player.getPlayerId(), player.getIcon()));
            updateTableStatusInfo(table.getTableId(), statusInfo);
            logger.debug("statusInfo 更新后->" + statusInfo.toString());
        }
    }

    /**
     * 玩家站起
     */
    public final void onPlayerStandUp(final TableInfo table, final PlayerInfo player) {
        TableStatusInfo statusInfo = getTableStatusInfo(table.getTableId());
        if (statusInfo != null) {
            logger.debug("statusInfo 更新前->" + statusInfo.toString());
            statusInfo.setCurrentPlayerCount(statusInfo.getCurrentPlayerCount() - 1);
            statusInfo.setCurrentWaitingCount(statusInfo.getCurrentWaitingCount() + 1);
            statusInfo.getInfoMap().remove(player.getPlayerId());
            updateTableStatusInfo(table.getTableId(), statusInfo);
            logger.debug("statusInfo 更新后->" + statusInfo.toString());
        }
    }

    /**
     * 站起之后在离开相当于事物
     *
     * @param table
     * @param player
     */
    public final void onPlayerStandUpAndLeave(final TableInfo table, final PlayerInfo player) {
        TableStatusInfo statusInfo = getTableStatusInfo(table.getTableId());
        if (statusInfo != null) {
            logger.debug("statusInfo 更新前->" + statusInfo.toString());
            statusInfo.setCurrentPlayerCount(statusInfo.getCurrentPlayerCount() - 1);
            statusInfo.getInfoMap().remove(player.getPlayerId());
            updateTableStatusInfo(table.getTableId(), statusInfo);
            logger.debug("statusInfo 更新后->" + statusInfo.toString());
        }
    }
}
