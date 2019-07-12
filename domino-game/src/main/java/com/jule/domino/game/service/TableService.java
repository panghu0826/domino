package com.jule.domino.game.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.jule.core.configuration.GameConfig;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.gameUtil.GameLogic;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.game.network.protocol.logic.LeaveTableLogic;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.room.service.RoomOprService;
import com.jule.domino.base.model.RoomTableRelationModel;
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> pollingTable(), 5, 2, TimeUnit.MINUTES);
//        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> pollingTable(), 30, 20, TimeUnit.SECONDS);
    }

    private void pollingTable() {
        //gameId + roomId == "110"
        Map<String, AbstractTable> tableMap = ROOM_TABLE_MAP.get("110");
        log.info("当前有多少张桌子：{}", tableMap.size());
        if (tableMap != null) {
            for (AbstractTable table : tableMap.values()) {
//                boolean timeOut = (System.currentTimeMillis() - table.getLastActionTime()) > 30 * 1000;
                boolean timeOut = (System.currentTimeMillis() - table.getLastActionTime()) > 600 * 1000;
                if (timeOut) {
                    log.info("桌子最后一次操作已经过了 {} 分钟。", (System.currentTimeMillis() - table.getLastActionTime()) / 600000);
                    table.getAllPlayers().forEach((k, v) -> {
                        //玩家离桌广播
                        NoticeBroadcastMessages.sendPlayerLeaveNotice(table, v);
                    });
                    table.getAllPlayers().forEach((k, v) -> {
                        //处理玩家离桌的redis信息
                        LeaveTableLogic.getInstance().logic(v, table);
                    });
                    TableService.getInstance().directDestroyTable(String.valueOf(table.getPlayType()), table.getRoomId(), table.getTableId());
                }
            }
        }
    }

    //判断当前房间条件满足开始游戏否
//    public void playGame(final AbstractTable table) {
//        log.debug("table state：" + table.getTableStateEnum() + ",all players's size:：" + table.getInGamePlayersBySeatNum().size());
//        if (table.getTableStateEnum() == TableStateEnum.IDEL
//                && table.getInGamePlayersBySeatNum().size() >= 2) {
//            //当GM修改了配置需要重新加载
//            table.setCommonConfig(CommonConfigHolder.getInstance().getCommonConfig(table.getPlayType()));
//            table.setRoomConfig(RoomConfigHolder.getInstance().getRoomConfig(table.getRoomId()));
//            table.initTableStateAttribute();
//
//            table.setCurrGameOrderId(GameOrderIdGenerator.generate());//游戏此回合唯一订单号
//            log.debug("play game game order id:" + table.getCurrGameOrderId());
//
//            CardOfTableHolder.PutCardOperationObj(table.getCurrGameOrderId(),
//                    new DealCardForTable(new RoomTableRelationModel(table.getPlayType() + "", table.getRoomId(), table.getTableId(),table.getTableStateEnum().getValue()),
//                            table.getCurrGameOrderId()));
//            GameLogic.gameReady(table);//启动定时器
//            log.debug("tableId={},开始游戏!",table.getTableId());
//        }
//    }

    //判断当前房间条件满足开始游戏否
    public void playGame(final AbstractTable table) {


//        if ((table.getTableStateEnum() == TableStateEnum.IDEL
//                || table.getTableStateEnum() == TableStateEnum.GAME_READY)
//                && table.getInGamePlayers().size() >= 2) {
//            log.debug("当前桌子入座人数：{}， 准备人数：{}",table.getInGamePlayersBySeatNum().size(),table.getInGamePlayers().size());
//            if(table.getInGamePlayers().size() >= table.getInGamePlayersBySeatNum().size()){
//                //准备人数大于等于房间座位上的人则直接开始游戏  并启动下注计时器
//                GameLogic.gameStart(table);
//            }else {
//                //游戏开始cd广播
//                NoticeBroadcastMessages.gameStart(table);
//                //游戏开始cd
//                GameLogic.gameReady(table);
//            }
//        }
    }

    /**
     * 把Room模块创建的房间实例化出来
     *
     * @return
     */
    public synchronized AbstractTable addNewTable(String gameId, String roomId, String tableId) throws Exception {
        AbstractTable tableA = getTableFromAllTableMap(gameId, roomId, tableId);
        if(tableA != null){
            return tableA;
        }
        AbstractTable tableB = getTableFromAllTableMap("2", roomId, tableId);
        if(tableB != null){
            return tableB;
        }
        return null;
    }

//    /**
//     * 把Room模块创建的房间实例化出来
//     *
//     * @return
//     */
//    public synchronized AbstractTable addNewTable(String gameId, String roomId, String tableId) throws Exception {
//        AbstractTable tableInfo = getTableFromAllTableMap(gameId, roomId, tableId);
//        if (tableInfo == null) {
//            Class clazz = Class.forName(GameConfig.getGameTableName(gameId));
//            Class[] pars = new Class[]{String.class, String.class, String.class};
//            tableInfo = (AbstractTable) clazz.getConstructor(pars).newInstance(gameId, roomId, tableId);
//            addTableInAllTableMap(gameId, roomId, tableInfo);
//            addCanJoinTable(gameId, roomId, tableId);
//        }
//        return tableInfo;
//    }

    /**
     * 是否存在桌子
     *
     * @param tableId
     * @return
     */
    public AbstractTable getTable(String gameId, String roomId, String tableId) {
        return getTableFromAllTableMap(gameId, roomId, tableId);
    }

//    //添加各个玩法的实体类到map里
//    private void addTableInAllTableMap(String gameId, String roomId, AbstractTable tableInfo) {
//        Map<String, AbstractTable> tableMap = null;
//        if (ROOM_TABLE_MAP.containsKey(gameId + roomId)) {
//            tableMap = ROOM_TABLE_MAP.get(gameId + roomId);
//        }
//        if (null == tableMap) {
//            tableMap = new ConcurrentHashMap<>();
//        }
//
//        tableMap.put(tableInfo.getTableId(), tableInfo);
//        ROOM_TABLE_MAP.put(gameId + roomId, tableMap);
//        log.info("game新增加桌子：roomId {},  tableId {}",ROOM_TABLE_MAP.keySet().toString(),tableInfo.getTableId());
//    }

    private boolean addTableInAllTableMap(String gameId, String roomId, AbstractTable tableInfo) {
        Map<String, AbstractTable> tableMap = null;
        if (ROOM_TABLE_MAP.containsKey(gameId + roomId)) {
            tableMap = ROOM_TABLE_MAP.get(gameId + roomId);
        }
        if (null == tableMap) {
            tableMap = new ConcurrentHashMap<>();
        }
        if (tableMap.containsKey(tableInfo.getTableId())) {
            return false;
        }
        tableMap.put(tableInfo.getTableId(), tableInfo);
        ROOM_TABLE_MAP.put(gameId + roomId, tableMap);
        log.info("新增加桌子：roomId {},  tableId {}", ROOM_TABLE_MAP.keySet().toString(), tableInfo.getTableId());
        return true;
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

    public Collection<AbstractTable> getTableList(String gameId, String roomId){
        if (!ROOM_TABLE_MAP.containsKey(gameId + roomId)) {
            return null;
        }
        return ROOM_TABLE_MAP.get(gameId + roomId).values();
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
        RoomOprService.OBJ.leaveTableHandler(String.valueOf(table.getPlayType()), table.getRoomId(), table.getTableId(), userId);
    }

    private synchronized String getNewTableId(String gameId, String roomId) {
        long newTableId = DBUtil.selectLastId();
//        long newTableId = 10000 + StoredObjManager.incr(RedisConst.NEW_TABLE_ID.getProfix() + gameId + roomId);
        return getTableId(String.valueOf(System.currentTimeMillis()));
    }

    /**
     * tableId
     *
     * @return
     */
    protected static String getTableId(String tableId) {
        try {
            int in = (int) (1 + Math.random() * (9));
            int length = tableId.length();
            if (tableId.length() <= 5) {
                return in + tableId;
            }

            String name = tableId.substring(length - 5, length);
            return in + name;
        } catch (Exception e) {
            return String.valueOf(DBUtil.selectLastId());
        }
    }

    public static void main(String[] args) {
        System.out.println();
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

//    /**
//     * @return
//     */
//    public AbstractTable addExitTable(String gameId, String roomId, String tableId) throws Exception {
//        AbstractTable tableInfo = new AbstractTable(gameId, roomId, tableId);
//        boolean exist = addTableInAllTableMap(gameId, roomId, tableInfo);
//        if (!exist) {
//            addCanJoinTable(gameId, roomId, tableId);
//            return tableInfo;
//        }
//        return getTableFromAllTableMap(gameId, roomId, tableId);
//    }

    /**
     * 创建一个新桌子
     * 看缓存里有没有对应的 有则不再创建
     *
     * @return
     */
    public AbstractTable createNewTable(String gameId, String roomId, int playerNum) throws Exception {
        String tableId = getNewTableId(gameId, roomId);
        Class clazz = Class.forName(GameConfig.getGameTableName(gameId));
        Class[] pars = new Class[]{String.class, String.class, String.class,int.class};
        AbstractTable tableInfo = (AbstractTable) clazz.getConstructor(pars).newInstance(gameId, roomId, tableId, playerNum);
//        AbstractTable tableInfo = new AbstractTable(gameId, roomId, tableId, playerNum);
        addTableInAllTableMap("1", roomId, tableInfo);
        addCanJoinTable("1", roomId, tableId);
        addRedis("1", roomId, tableId);//存入Redis
//        RoomOprService.OBJ.createTableHandler(gameId, roomId, tableId);
        tableInfo.setLastActionTime(System.currentTimeMillis());
        GameLogic.timeOutRemoveTable(tableInfo);
        return tableInfo;
    }

    private void addRedis(String gameId, String roomId, String tableId) {
        RoomTableRelationModel rt = new RoomTableRelationModel(gameId, roomId, tableId, TableStateEnum.IDEL.getValue());
        StoredObjManager.hset(RedisConst.TABLE_INSTANCE.getProfix()+gameId+roomId,
                RedisConst.TABLE_INSTANCE.getField() + tableId, rt);
        log.debug("添加桌子信息到redis：--key: {}, --field: {}", RedisConst.TABLE_INSTANCE.getProfix(), RedisConst.TABLE_INSTANCE.getField() + tableId);
    }

    /**
     * 随机分配一个有空余座位可加入的桌子
     *
     * @param roomId
     * @return
     */
    public synchronized AbstractTable getRandomTable(String roomId, String gameId, String tableId) {
        /**整理一下可加入的桌子信息*/
//        boolean canCreat = checkRoomCanJoinTable(gameId, roomId);
//        if (canCreat) {
//            //当没有空闲桌子可用时，创建一个新桌子
//            try {
//                for (int i = 0; i < 5; i++) {
//                    createNewTable(gameId, roomId, true,0);
//                }
//            } catch (Exception e) {
//                log.error("getRandomTable ERROR while create a new table.", e);
//            }
//        }
        ConcurrentLinkedQueue<String> tableIdList = new ConcurrentLinkedQueue<>();

        if (Strings.isNullOrEmpty(tableId) || Config.ROOM_ASSIGNED_TOGETHER) {
            tableIdList = ROOM_CAN_JOIN_TABLE.get(gameId + roomId);
            log.debug("ROOM_CAN_JOIN_TABLE = {}, tableIdList = {}", ROOM_CAN_JOIN_TABLE.toString(), tableIdList.toString());
        } else {
            ConcurrentLinkedQueue<String> tmp = ROOM_CAN_JOIN_TABLE.get(gameId + roomId);
            Iterator<String> iterator = tmp.iterator();
            while (iterator.hasNext()) {
                String tid = iterator.next();
                if (tid.equals(tableId)) {
                    continue;
                }
                tableIdList.add(tid);
            }
        }

        if (null != tableIdList && tableIdList.size() > 0) {
            AbstractTable tableInfo = null;

            Iterator<String> iter = tableIdList.iterator();
            while (iter.hasNext()) {
                String id = iter.next();
                RoomTableRelationModel ret = StoredObjManager.getStoredObjInMap(RoomTableRelationModel.class,
                        RedisConst.TABLE_INSTANCE.getProfix() + gameId + roomId,
                        RedisConst.TABLE_INSTANCE.getField() + id);
                if (ret.getTableStatus() <= TableStateEnum.GAME_READY.getValue()) {
                    tableInfo = this.getInstance().getTable(gameId, roomId, id);
                    break;
                }
            }

            if (tableInfo != null) {
                log.info("enter table roomId:" + roomId + ",tableId:" + tableInfo.getTableId() + ",GameId = " + tableInfo.getPlayType());
            } else {
                log.error("没找到合适房间");
            }
            return tableInfo;

        }
        return null;
    }

    /**
     * 销毁一个桌子
     *
     * @param roomId
     * @param tableId
     * @return
     */
    public void directDestroyTable(String gameId, String roomId, String tableId) {

        boolean isRemoveTable = removeTableFromAllTableMap(gameId, roomId, tableId);
        boolean isRemoveCanJoin = removeCanJoinTable(gameId, roomId, tableId);

        boolean isDel = StoredObjManager.hdel(RedisConst.TABLE_INSTANCE.getProfix() + gameId + roomId,
                RedisConst.TABLE_INSTANCE.getField() + tableId);

        if (isDel) {
            log.info("删除桌子成功，gameId:" + gameId + ",roomId:" + roomId
                    + ",TableId:" + tableId + ",isRemoveTable:" + isRemoveTable
                    + ",isRemoveCanJoin:" + isRemoveCanJoin);
        } else {
            log.error("删除桌子失败，gameId:" + gameId + ",roomId:" + roomId
                    + ",TableId:" + tableId + ",isRemoveTable:" + isRemoveTable
                    + ",isRemoveCanJoin:" + isRemoveCanJoin);
        }
    }
}
