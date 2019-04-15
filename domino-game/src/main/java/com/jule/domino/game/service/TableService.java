package com.jule.domino.game.service;

import com.alibaba.fastjson.JSONObject;
import com.jule.core.configuration.GameConfig;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.game.gameUtil.DealCardForTable;
import com.jule.domino.game.gameUtil.GameLogic;
import com.jule.domino.game.gameUtil.GameOrderIdGenerator;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.room.service.RoomOprService;
import com.jule.domino.game.service.holder.CardOfTableHolder;
import com.jule.domino.game.service.holder.CommonConfigHolder;
import com.jule.domino.game.service.holder.RoomConfigHolder;
import com.jule.domino.base.enums.RedisChannel;
import com.jule.domino.base.model.RoomTableRelationModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 桌子管理类：
 * 获取桌子，创建新桌子
 */
@Slf4j
public class TableService {
    /**
     * 所有桌子缓存
     * key：
     * 第一层Map：gameId + roomId
     * 第二层Map：tableId
     */
    private static final Map<String, Map<String, AbstractTable>> ROOM_TABLE_MAP = new ConcurrentHashMap();
    /**
     * 房间中有空余座位，可以加入的桌子
     * Key：gameId + roomId
     * Value：list tableId
     */
    private static final Map<String, ConcurrentLinkedQueue<String>> ROOM_CAN_JOIN_TABLE = new ConcurrentHashMap<>();
    /**
     * 房间中已存在的最大桌子号（创建新桌子ID时，取此值+1）
     * TODO：单服务器时，可使用原子操作。但进行多服务器部署时，这里要修改为从Redis获取自增数量
     */
    private static final Map<String, AtomicInteger> ROOM_MAX_TABLE_ID = new ConcurrentHashMap<>();

    private static class SingletonHolder {
        protected static final TableService instance = new TableService();
    }

    public static final TableService getInstance() {
        return SingletonHolder.instance;
    }

    private TableService() {
    }

    //判断当前房间条件满足开始游戏否
    public void playGame(final AbstractTable table) {
        log.debug("tableId->" + table.getTableId() + ", tableState->" + table.getTableStateEnum().getValue() +
                ", playerCountBySeatNum->" + table.getInGamePlayersBySeatNum().size() +
                ", playerCountByPlayerid->" + table.getInGamePlayersByPlayerId().size());
        log.info(table.getTableStateEnum() + " all players's size:" + table.getInGamePlayersBySeatNum().size());
        log.debug("table state：" + table.getTableStateEnum() + ",all players's size:：" + table.getInGamePlayersBySeatNum().size());

        if (table.getTableStateEnum() == TableStateEnum.IDEL
                && table.getInGamePlayersBySeatNum().size() >= 2) {
            //当GM修改了配置需要重新加载
            table.setCommonConfig(CommonConfigHolder.getInstance().getCommonConfig(table.getPlayType()));
            table.setRoomConfig(RoomConfigHolder.getInstance().getRoomConfig(table.getRoomId()));
            table.initTableStateAttribute();

            table.setCurrGameOrderId(GameOrderIdGenerator.generate());//游戏此回合唯一订单号
            log.debug("play game game order id:" + table.getCurrGameOrderId());

            CardOfTableHolder.PutCardOperationObj(table.getCurrGameOrderId(),
                    new DealCardForTable(new RoomTableRelationModel(table.getPlayType() + "", table.getRoomId(), table.getTableId(),table.getTableStateEnum().getValue()),
                            table.getCurrGameOrderId()));


            GameLogic.gameReady(table);//启动定时器
            log.debug("tableId={},开始游戏!",table.getTableId());
        }
    }

    /**
     * 把Room模块创建的房间实例化出来
     *
     * @return
     */
    public synchronized AbstractTable addNewTable(String gameId, String roomId, String tableId) throws Exception {
        AbstractTable tableInfo = getTableFromAllTableMap(gameId, roomId, tableId);
        if (tableInfo == null) {
            Class clazz = Class.forName(GameConfig.getGameTableName(gameId));
            Class[] pars = new Class[]{String.class, String.class, String.class};
            tableInfo = (AbstractTable) clazz.getConstructor(pars).newInstance(gameId, roomId, tableId);
            addTableInAllTableMap(gameId, roomId, tableInfo);
            addCanJoinTable(gameId, roomId, tableId);
        }
        return tableInfo;
    }

    /**
     * 是否存在桌子
     *
     * @param tableId
     * @return
     */
    public AbstractTable getTable(String gameId, String roomId, String tableId) {
        return getTableFromAllTableMap(gameId, roomId, tableId);
    }

    //添加各个玩法的实体类到map里
    private void addTableInAllTableMap(String gameId, String roomId, AbstractTable tableInfo) {
        Map<String, AbstractTable> tableMap = null;
        if (ROOM_TABLE_MAP.containsKey(gameId + roomId)) {
            tableMap = ROOM_TABLE_MAP.get(gameId + roomId);
        }
        if (null == tableMap) {
            tableMap = new ConcurrentHashMap<>();
        }

        tableMap.put(tableInfo.getTableId(), tableInfo);
        ROOM_TABLE_MAP.put(gameId + roomId, tableMap);
    }

    private AbstractTable getTableFromAllTableMap(String gameId, String roomId, String tableId) {
        if (ROOM_TABLE_MAP.containsKey(gameId + roomId)) {
            Map<String, AbstractTable> tableMap = ROOM_TABLE_MAP.get(gameId + roomId);
            if (null != tableMap && tableMap.containsKey(tableId)) {
                return tableMap.get(tableId);
            }
        }
        return null;
    }

    private boolean removeTableFromAllTableMap(String gameId, String roomId, String tableId) {
        if (ROOM_TABLE_MAP.containsKey(gameId + roomId)) {
            Map<String, AbstractTable> tableMap = ROOM_TABLE_MAP.get(gameId + roomId);
            if (null != tableMap && tableMap.containsKey(tableId)) {
                AbstractTable tableInfo = tableMap.remove(tableId);
                if (null != tableInfo) {
                    tableInfo = null;
                    return true;
                }
            }
        }
        return false;
    }
    //endregion

    //region 操作ROOM_CAN_JOIN_TABLE
    public void addCanJoinTable(String gameId, String roomId, String tableId) {
        ConcurrentLinkedQueue<String> tableIdList = null;
        if (ROOM_CAN_JOIN_TABLE.containsKey(gameId + roomId)) {
            tableIdList = ROOM_CAN_JOIN_TABLE.get(gameId + roomId);
        } else {
            tableIdList = new ConcurrentLinkedQueue<>();
        }

        tableIdList.add(tableId);
        ROOM_CAN_JOIN_TABLE.put(gameId + roomId, tableIdList);
    }

    public boolean removeCanJoinTable(String gameId, String roomId, String tableId) {
        ConcurrentLinkedQueue<String> tableIdList = null;
        if (ROOM_CAN_JOIN_TABLE.containsKey(gameId + roomId)) {
            tableIdList = ROOM_CAN_JOIN_TABLE.get(gameId + roomId);
        }

        if (null != tableIdList && tableIdList.size() > 0) {
            return tableIdList.remove(tableId);
        }
        return false;
    }
    //endregion

    public void publishLeaveTable(AbstractTable table, String userId) {
        JSONObject js = new JSONObject();
        js.put("gameId", "" + table.getPlayType());
        js.put("roomId", table.getRoomId());
        js.put("tableId", table.getTableId());
        js.put("userId", userId);
        log.info("清理玩家在游戏内的缓存，js->{}", js.toString());
        RoomOprService.OBJ.leaveTableHandler(String.valueOf(table.getPlayType()),table.getRoomId(),table.getTableId(),userId);
    }

    //region 操作ROOM_MAX_TABLE_ID
    private int getNewTableId(String roomId) {
        AtomicInteger ai = null;
        if (ROOM_MAX_TABLE_ID.containsKey(roomId)) {
            ai = ROOM_MAX_TABLE_ID.get(roomId);
            return ai.incrementAndGet();
        } else {
            ai = new AtomicInteger();
            ROOM_MAX_TABLE_ID.put(roomId, ai);
            return ai.incrementAndGet();
        }
    }

    public void timeOutLeaveTable() {
        ROOM_TABLE_MAP.forEach((k, v) -> {
            v.forEach((kk, vv) -> {
                vv.timeOutLeaveTable();
            });
        });
    }

    public void tablePropertyChange() {
        ROOM_TABLE_MAP.forEach((k, v) -> {
            v.forEach((kk, vv) -> {
                vv.setPropertyChange(true);
            });
        });
    }

    /**
     * @param table
     */
    public void updateInGamePlayersBySeatNumPlayerStateSiteDown(AbstractTable table) {
        for (PlayerInfo player1 : table.getInGamePlayersBySeatNum().values()) {
            if (player1 != null) {
                player1.setState(PlayerStateEnum.siteDown);
            }
        }
    }
}
