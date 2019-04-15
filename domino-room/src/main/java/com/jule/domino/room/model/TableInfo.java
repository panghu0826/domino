package com.jule.domino.room.model;

import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.base.model.GameRoomTableSeatRelationModel;
import com.jule.domino.base.model.RoomTableRelationModel;
import com.jule.domino.room.dao.bean.CommonConfigModel;
import com.jule.domino.room.dao.bean.RoomConfigModel;
import com.jule.domino.room.service.RoomStateService;
import com.jule.domino.room.service.holder.CommonConfigHolder;
import com.jule.domino.room.service.holder.RoomConfigHolder;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * 游戏服务：房间信息类
 * 描述：存储房间基础信息（基础下注值、允许的下注倍数、各阶段倒计时秒数），以及已进入房间玩家列表
 */
public class TableInfo {
    private final static Logger logger = LoggerFactory.getLogger(TableInfo.class);

    private RoomTableRelationModel roomTableRelation;

    //region 牌桌内的基础属性
    private String roomId;
    private String tableId;
    //private int seatCount; //座位数量
    @Getter
    private long ante; //底注
    private ConcurrentLinkedQueue<Integer> nullSeatList = new ConcurrentLinkedQueue<>(); //可用的空座位列表
    @Getter
    @Setter
    private String playType; //玩法类型
    //endregion

    //全部玩家列表 key：userId
    private final Map<String, PlayerInfo> allPlayers = new ConcurrentHashMap<>();//房间所有人
    public TableInfo(String gameId, String roomId, String tableId) throws Exception {
        setPlayType(gameId);
        this.roomId = roomId;
        this.tableId = tableId;

        roomTableRelation = new RoomTableRelationModel(gameId, roomId, tableId, TableStateEnum.IDEL.getValue());

        //初始化空座位
        addNullSeat(1);
        addNullSeat(2);
        addNullSeat(3);
        addNullSeat(4);

        //初始化牌桌配置
        RoomConfigModel roomConfig = RoomConfigHolder.getInstance().getRoomConfig(roomId);
        if (null == roomConfig) {
            throw new Exception("can't found RoomConfig, roomId->" + roomId);
        }
        CommonConfigModel commonConfig = CommonConfigHolder.getInstance().getCommonConfig(Integer.parseInt(getPlayType()));
        if (null == commonConfig) {
            throw new Exception("can't found CommonConfig, roomId->" + roomId+",playType = "+playType);
        }

        this.ante = roomConfig.getAnte();
    }

    //region only getter methods
    public String getRoomId() {
        return roomId;
    }

    public String getTableId() {
        return tableId;
    }

    public Map<String, PlayerInfo> getAllPlayers() {
        return allPlayers;
    }

    //endregion

    //region 加入牌桌、坐下、站起、离桌

    /**
     * 加入牌桌
     *
     * @param player
     * @param gameId
     * @return
     */
    public PlayerInfo joinTable(PlayerInfo player, String gameId) {//入桌
        RoomStateService.getInstance().onPlayerJoinTable(this);
        player.setState(PlayerStateEnum.spectator);
        StoredObjManager.hset(RedisConst.TABLE_USERS.getProfix() + gameId + getRoomId() + getTableId(),
                RedisConst.TABLE_USERS.getField() + player.getPlayerId(), "" + PlayerStateEnum.spectator.getValue());
        //if (GameConst.testReconnect) {
            //设置玩家所在位置
            StoredObjManager.hset(RedisConst.USER_TABLE_SEAT.getProfix(), RedisConst.USER_TABLE_SEAT.getField() + player.getPlayerId(),
                    new GameRoomTableSeatRelationModel(gameId, player.getRoomId(), player.getTableId(), 0, ""));
       // }
        /** 以后多个Room or Game时需要再加
         JedisPoolWrap.getInstance().pub(new JSONObject().accumulate("userId", player.getPlayerId())
         .accumulate("roomId",this.getRoomId())
         .accumulate("tableId", this.getTableId()).toString(), RedisChannel.JOIN_TABLE_CHANNEL.getChannelName());
         */
        return allPlayers.put(player.getPlayerId(), player);
    }

    /**
     * 离桌
     *
     * @param userId
     */
    public void returnLobby(String userId) {
        allPlayers.remove(userId);
        Boolean delSucc = StoredObjManager.hdel(RedisConst.TABLE_USERS.getProfix() + this.getPlayType() + getRoomId() + getTableId(),
                RedisConst.TABLE_USERS.getField() + userId);
        logger.info("returnLobby userId:{},gameId:{},roomId:{},tableId:{},isDeleted:{}" , userId,this.getPlayType(),getRoomId(),getTableId(),delSucc);
    }

    private PlayerInfo removePlayer(String userId) {//离桌
        return allPlayers.remove(userId);
    }
    //endregion

    //判断是否存在这个人
    public boolean isHasPlayer(String playerId) {
        return allPlayers.containsKey(playerId);
    }

    //region 获得玩家信息

    /**
     * 获取桌子单个玩家
     */
    public PlayerInfo getPlayer(String playerId) {
        return allPlayers.get(playerId);
    }


    //region 空座位管理
    private void addNullSeat(int seatNum) {
        nullSeatList.add(seatNum);
    }

    private boolean removeNullSeat(int seatNum) {
        if (null != nullSeatList && nullSeatList.size() > 0) {
            return nullSeatList.remove(seatNum);
        }
        return false;
    }

    public int getNulSeatNum() {
        Random random = new Random(System.currentTimeMillis());
        int seatNum = (int) (nullSeatList.toArray())[random.nextInt(nullSeatList.size())];
//        log.debug("座位号-----------------------------："+seatNum);
//        this.removeNullSeat(seatNum);
//        if(nullSeatList.size() < 1){
//            log.debug("删除桌子成功：--------------------------------"+tableId);
//            TableService.destroyTable(this.roomId,this.tableId);
//        }
        return seatNum;
    }
}
