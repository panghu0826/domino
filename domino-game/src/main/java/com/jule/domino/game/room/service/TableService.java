package com.jule.domino.game.room.service;

import com.google.common.base.Strings;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.base.model.RoomTableRelationModel;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.dao.bean.RoomConfigModel;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.holder.RoomConfigHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 桌子管理类：
 * 获取桌子，创建新桌子
 */
public class TableService {
    private final static Logger logger = LoggerFactory.getLogger(TableService.class);
    /**
     * 所有桌子缓存(后续保存到Redis里)
     * key：
     * 第一层Map：gameId + roomId
     * 第二层Map：tableId
     */
    private static final Map<String, Map<String, AbstractTable>> ROOM_TABLE_MAP = new ConcurrentHashMap();
    /**
     * TODO 移除不能加入的GameSvr所在的桌子
     * 房间中有空余座位，可以加入的桌子
     * Key：roomId
     * Value：list tableId
     */
    private static final Map<String, ConcurrentLinkedQueue<String>> ROOM_CAN_JOIN_TABLE = new ConcurrentHashMap<>();

    /**
     * 房间中已存在的最大桌子号（创建新桌子ID时，取此值+1）
     * TODO：单服务器时，可使用原子操作。但进行多服务器部署时，这里要修改为从Redis获取自增数量
     * <p>
     * private static final Map<String, AtomicInteger> ROOM_MAX_TABLE_ID = new ConcurrentHashMap<>();
     */
    private static class SingletonHolder {
        protected static final TableService instance = new TableService();
    }

    public static final TableService getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * 初始化游戏内存
     */
    public void init() {
        Map<String, RoomConfigModel> map = RoomConfigHolder.getInstance().getAllConfig();

        logger.debug("tableService:" + map.toString());
        String[] gameIds = Config.GAME_IDS.split(":");
        logger.error("init table cnt = {}", Config.ROOM_INIT_DESK_NUM);

        map.forEach((s, roomConfig) -> {
            String roomId = roomConfig.getRoomId();
            for (String gameId : gameIds) {
                //每个room初始创建4张桌子
                for (int i = 0; i < Config.ROOM_INIT_DESK_NUM; i++) {
                    try {
                        AbstractTable tableInfo = createNewTable(gameId, roomId, false);
                        if (tableInfo == null) {
                            break;
                        }
                    } catch (Exception e) {
                        logger.error("init room ERROR, create table ERROR。", e);
                    }
                }
                logger.info("初始化桌子->gameId:" + gameId + " roomId:" + s);
            }

        });

        logger.debug("初始化结束---：");
    }

    /**
     * 创建一个新桌子
     * 看缓存里有没有对应的 有则不再创建
     *
     * @return
     */
    public AbstractTable createNewTable(String gameId, String roomId, boolean force) throws Exception {
        if (force == false) {
            Map<String, RoomTableRelationModel> map = StoredObjManager.getStoredObjsInMap(RoomTableRelationModel.class, RedisConst.TABLE_INSTANCE.getProfix() + gameId + roomId);
            if (map != null && map.size() >= Config.ROOM_INIT_DESK_NUM) {
                Iterator<RoomTableRelationModel> iterator = map.values().iterator();
                while (iterator.hasNext()) {
                    RoomTableRelationModel model = iterator.next();
                    AbstractTable tableInfo = new AbstractTable(gameId, model.getRoomId(), model.getTableId());
                    addTableInAllTableMap(gameId, roomId, tableInfo);
                    addCanJoinTable(gameId, roomId, model.getTableId());
                }
                return null;
            }
        }
        String tableId = getNewTableId(gameId, roomId) + "";
        AbstractTable tableInfo = new AbstractTable(gameId, roomId, tableId);
        addTableInAllTableMap(gameId, roomId, tableInfo);
        addCanJoinTable(gameId, roomId, tableId);
        //存入Redis
        addRedis(gameId, roomId, tableId);
        RoomOprService.OBJ.createTableHandler(gameId, roomId, tableId);

        return tableInfo;
    }

    /**
     * @return
     */
    public AbstractTable addExitTable(String gameId, String roomId, String tableId) throws Exception {
        AbstractTable tableInfo = new AbstractTable(gameId, roomId, tableId);

        boolean exist = addTableInAllTableMap(gameId, roomId, tableInfo);
        if (!exist) {
            addCanJoinTable(gameId, roomId, tableId);
            return tableInfo;
        }
        return getTableFromAllTableMap(gameId, roomId, tableId);
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

        logger.info("directDestroyTable()，gameId:" + gameId + ",roomId:" + roomId
                + ",TableId:" + tableId + ",isRemoveTable:" + isRemoveTable
                + ",isRemoveCanJoin:" + isRemoveCanJoin);
    }

    /**
     * 销毁一个桌子
     *
     * @param roomId
     * @param tableId
     * @return
     */
    public boolean destroyTable(String gameId, String roomId, String tableId) {
        int leftNum = getLeftSeatNum(gameId, roomId);
        StringBuilder sb = new StringBuilder();
        sb.append("gameId:" + gameId + ",roomId:" + roomId + ",TableId:" + tableId + ",LeftSeatNum:" + leftNum);

        if (leftNum > Config.ROOM_REMOVE_DESK_NIL_SEAT) {
            List<String> list = StoredObjManager.hvals(RedisConst.TABLE_USERS.getProfix() + gameId + roomId + tableId);
            //List<String> list = StoredObjManager.hvals(RedisConst.TABLE_SEAT.getProfix() + gameId + roomId + tableId);
            sb.append("room player's size:" + list.size());
            if (list.size() <= 0) {
                boolean isRemoveTable = removeTableFromAllTableMap(gameId, roomId, tableId);
                boolean isRemoveCanJoin = removeCanJoinTable(gameId, roomId, tableId);

                boolean isDel = StoredObjManager.hdel(RedisConst.TABLE_INSTANCE.getProfix() + gameId + roomId,
                        RedisConst.TABLE_INSTANCE.getField() + tableId);

                if (isDel) {
                    RoomOprService.OBJ.destoryTableHandler(gameId,roomId, tableId);
                }
                logger.info(sb.toString() + "isRemoveTable:" + isRemoveTable + ",isRemoveCanJoin:" + isRemoveCanJoin + ",isDelRedis:" + isDel);

                return isRemoveTable && isRemoveCanJoin;
            }
        }
        logger.info(sb.toString());
        return false;
    }

    /**
     * 随机分配一个有空余座位可加入的桌子
     *
     * @param roomId
     * @return
     */
    public synchronized AbstractTable getRandomTable(String roomId, String gameId, String tableId) {
        /**整理一下可加入的桌子信息*/
        boolean canCreat = checkRoomCanJoinTable(gameId, roomId);
        if (canCreat) {
            //当没有空闲桌子可用时，创建一个新桌子
            try {
                for (int i = 0; i < 5; i++) {
                    createNewTable(gameId, roomId, true);
                }
            } catch (Exception e) {
                logger.error("getRandomTable ERROR while create a new table.", e);
            }
        }
        ConcurrentLinkedQueue<String> tableIdList = new ConcurrentLinkedQueue<>();

        if (Strings.isNullOrEmpty(tableId) || Config.ROOM_ASSIGNED_TOGETHER) {
            tableIdList = ROOM_CAN_JOIN_TABLE.get(gameId + roomId);
            logger.debug("ROOM_CAN_JOIN_TABLE = {}, tableIdList = {}", ROOM_CAN_JOIN_TABLE.toString(), tableIdList.toString());
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

            if (tableInfo != null){
                logger.info("enter table roomId:" + roomId + ",tableId:" + tableInfo.getTableId() + ",GameId = " + tableInfo.getPlayType());
            }else {
                logger.error("没找到合适房间");
            }
            return tableInfo;

        }
        return null;
    }

    /**
     * 获取当前负载人数 根据桌子判断，粗略计算 忽略桌子是否缺人
     *
     * @return
     */
    public int getOnLineNum() {
        return ROOM_TABLE_MAP.size() * 5;
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

    //region 操作ROOM_TABLE_MAP
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
        if (tableIdList.contains(tableId)) {
            return;
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

    private synchronized long getNewTableId(String gameId, String roomId) {
        long newTableId = 10000 + StoredObjManager.incr(RedisConst.NEW_TABLE_ID.getProfix() + gameId + roomId);
//        logger.info("create new TableId :" + newTableId);
        return newTableId;
    }
    //endregion

    private void addRedis(String gameId, String roomId, String tableId) {
        RoomTableRelationModel rt = new RoomTableRelationModel(gameId, roomId, tableId, TableStateEnum.IDEL.getValue());
        StoredObjManager.hset(RedisConst.TABLE_INSTANCE.getProfix() + gameId + roomId,
                RedisConst.TABLE_INSTANCE.getField() + tableId, rt);
        logger.debug("添加桌子信息到redis：--key: {}, --field: {}",RedisConst.TABLE_INSTANCE.getProfix() + gameId + roomId,RedisConst.TABLE_INSTANCE.getField() + tableId);
    }

    /***
     * 检查不符合的移除出去
     * @param gameId
     * @param  roomId
     * return false 需要增加桌子 ture 不需要
     * 当前空位剩余6个空位时，开启新的桌子
     * 每次新开两张桌子
     */
    private boolean checkRoomCanJoinTable(String gameId, String roomId) {
        ConcurrentLinkedQueue<String> TableIds = ROOM_CAN_JOIN_TABLE.get(gameId + roomId);
        if (TableIds == null || TableIds.isEmpty()) {
            return false;
        }

        //判断一下
        Iterator<String> checkIterator = TableIds.iterator();
        while (checkIterator.hasNext()) {
            String tableId = checkIterator.next();
            List<String> list = StoredObjManager.hvals(RedisConst.TABLE_SEAT.getProfix() + gameId + roomId + tableId);
            if (list.size() >= GameConst.TABLE_MAX_PLAYER_NUM - 1 ) {
                checkIterator.remove();
            }
        }

        //判断一下状态
        Iterator<String> iter = TableIds.iterator();
        while (iter.hasNext()) {
            RoomTableRelationModel ret = StoredObjManager.getStoredObjInMap(RoomTableRelationModel.class,
                    RedisConst.TABLE_INSTANCE.getProfix() + gameId + roomId,
                    RedisConst.TABLE_INSTANCE.getField() + iter.next());
            if (ret.getTableStatus() < TableStateEnum.GAME_READY.getValue()) {
                return false;
            }
        }

        int leftNum = 0;
        Iterator<String> iterator = TableIds.iterator();
        while (iterator.hasNext()) {
            String tableId = iterator.next();
            /**座位上的人*/
            List<String> list = StoredObjManager.hvals(RedisConst.TABLE_SEAT.getProfix() + gameId + roomId + tableId);
            logger.info("checkRoomCanJoinTable tableId" + tableId + " players's size:" + list.size());
            leftNum += GameConst.TABLE_MAX_PLAYER_NUM - list.size();
        }
        return Config.ROOM_ADD_DESK_NIL_SEAT >= leftNum - 1;
    }

    private int getLeftSeatNum(String gameId, String roomId) {
        int leftNum = 0;
        ConcurrentLinkedQueue<String> TableIds = ROOM_CAN_JOIN_TABLE.get(gameId + roomId);
        if (TableIds == null || TableIds.isEmpty()) {
            return leftNum;
        }
        Iterator<String> iterator = TableIds.iterator();
        while (iterator.hasNext()) {
            String tableId = iterator.next();
            /**座位上的人*/
            List<String> list = StoredObjManager.hvals(RedisConst.TABLE_SEAT.getProfix() + gameId + roomId + tableId);
            logger.info("checkRoomCanJoinTable tableId" + tableId + " players's size:" + list.size());
            leftNum += GameConst.TABLE_MAX_PLAYER_NUM - list.size();
        }
        return leftNum;
    }

    public void addRoomCanJoinTable(String gameId, String roomId, String tableId) {
        try {
            if (getTableFromAllTableMap(gameId, roomId, tableId) == null) {
                return;
            }
            ConcurrentLinkedQueue<String> TableIds = ROOM_CAN_JOIN_TABLE.get(gameId + roomId);
            if (TableIds.contains(tableId)) {
                logger.info("exit addRoomCanJoinTable() rooid:" + roomId + ",tableid:" + tableId);
                return;
            }
            TableIds.offer(tableId);
            logger.info("addRoomCanJoinTable() rooid:" + roomId + ",tableid:" + tableId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    /**
     * 房间在线人数
     * @param id
     * @return
     */
    public int countPlayers (int id){
        int players = 0;
        try {
            for (String key : ROOM_TABLE_MAP.keySet()){
                Map<String, AbstractTable> tables = ROOM_TABLE_MAP.get(key);
                for (String k : tables.keySet()){
                    AbstractTable table = tables.get(k);
                    if (table == null){
                        continue;
                    }

                    if (table.getRoomConfig().getId() == id){
                        players += table.getInGamePlayersBySeatNum().size();
                    }
                }
            }
        }catch (Exception ex){
            logger.error("获取房间人数异常",ex);
        }
        return players;
    }

}
