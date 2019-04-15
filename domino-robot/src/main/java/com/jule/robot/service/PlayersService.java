package com.jule.robot.service;

import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;
import com.jule.db.entities.RoomConfigModel;
import com.jule.db.proxy.EntityProxy;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.RoomTableRelationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayersService {
    private final static Logger logger = LoggerFactory.getLogger(PlayersService.class);

    private static class SingletonHolder {
        protected static final PlayersService instance = new PlayersService();
    }

    public static final PlayersService getInstance() {
        return PlayersService.SingletonHolder.instance;
    }

    public int getOnlinePlayersNum() {
        int totalOnLinePlayer = 0;

        List<RoomConfigModel> roomConfigList = EntityProxy.OBJ.getResultList(" 1 = 1 ", null, RoomConfigModel.class);
        try {
            int gameId = 71001001;
            for (RoomConfigModel roomConfigModel : roomConfigList) {
                if (roomConfigModel == null) {
                    continue;
                }
                String roomId = roomConfigModel.getRoomId();
                String key4RoomTableRelation = RedisConst.TABLE_INSTANCE.getProfix() + gameId + roomId;
                Map<String, String> tableMap = JedisPoolWrap.getInstance().hGetAll(key4RoomTableRelation);
                for (String key : tableMap.keySet()) {
                    RoomTableRelationModel model = StoredObjManager.getStoredObjInMap(RoomTableRelationModel.class, key4RoomTableRelation, key);
                    if (null != model) {
                        String tableId = model.getTableId();
                        String key4tableSeat = RedisConst.TABLE_SEAT.getProfix() + gameId + roomId + tableId;
                        Map<String, String> seatMap = JedisPoolWrap.getInstance().hGetAll(key4tableSeat);

                        totalOnLinePlayer += seatMap.size();
                    }
                }
            }

        } catch (Exception ex) {
            logger.error("CheckTableIsNeedRobot ERROR, msg = " + ex.getMessage(), ex);
        }
        return totalOnLinePlayer;
    }
}
