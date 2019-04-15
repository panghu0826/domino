package com.jule.domino.room.service;

import JoloProtobuf.RoomSvr.JoloRoom;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.jule.core.jedis.JedisPoolWrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class JedisService {
    private final static Logger logger = LoggerFactory.getLogger(JedisService.class);
    private final static String ROOM_STAT = "ROOM_STAT_";

    private static class SingletonHolder {
        protected static final JedisService instance = new JedisService();
    }

    public static final JedisService getInstance() {
        return JedisService.SingletonHolder.instance;
    }

    /**
     * 从redis获取动态信息
     *
     * @param tableIds
     * @return
     */
    public List<JoloRoom.JoloRoom_Table_StatusInfo> getJoloRoomTableStatusInfos(String gameId,String... tableIds) {
        List<JoloRoom.JoloRoom_Table_StatusInfo> ret = new ArrayList<>();
        List<byte[]> bytes = JedisPoolWrap.getInstance().hMget(ROOM_STAT+gameId, tableIds);
        for (byte[] aByte : bytes) {
            if (aByte != null) {
                try {
                    ret.add(JoloRoom.JoloRoom_Table_StatusInfo.parseFrom(aByte));
                } catch (InvalidProtocolBufferException e) {
                    logger.debug("getJoloRoomTableStatusInfos", e);
                }
            } else {
                ret.add(null);
            }
        }

        return ret;
    }

    /**
     * @param tableId
     * @return
     */
    public JoloRoom.JoloRoom_Table_StatusInfo getJoloRoomJoloRoomTableStatusInfo(String gameId,String tableId) {
        JoloRoom.JoloRoom_Table_StatusInfo ret = null;
        byte[] bytes = JedisPoolWrap.getInstance().hGet(ROOM_STAT+gameId, tableId);
        if (bytes != null) {
            try {
                ret = JoloRoom.JoloRoom_Table_StatusInfo.parseFrom(bytes);
            } catch (InvalidProtocolBufferException e) {
                logger.debug("getJoloRoomJoloRoomTableStatusInfo", e);
            }
        }

        return ret;
    }

    /**
     * 需要对结果做断言
     *
     * @return
     */
    public boolean updateJoloRoomJoloRoomTableStatusInfo(String gameId,String tableId, MessageLite messageLite) {
        return JedisPoolWrap.getInstance().hSet(ROOM_STAT+gameId, tableId, messageLite);
    }

//    /**
//     *
//     */
//    public boolean storeAllRoomInformation(String key,String field){
//        return StoredObjManager.setStoredObjInMap(RedisRoomCachingObject.getInstance(),key,field);
//    }
}
