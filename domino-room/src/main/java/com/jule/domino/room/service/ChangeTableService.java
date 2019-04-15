package com.jule.domino.room.service;

import com.alibaba.fastjson.JSONObject;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.enums.RedisChannel;
import com.jule.domino.room.network.protocol.ClientReq;
import com.jule.domino.room.network.protocol.reqs.JoloRoom_ApplyChangeTableReq_40002;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ChangeTableService {
    private final static Logger logger = LoggerFactory.getLogger(ChangeTableService.class);
    private Map<Integer, JoloRoom_ApplyChangeTableReq_40002> reqHashMap = new ConcurrentHashMap();

    private static class SingletonHolder {
        protected static final ChangeTableService instance = new ChangeTableService();
    }

    public static final ChangeTableService getInstance() {
        return ChangeTableService.SingletonHolder.instance;
    }

    public void ReqHandler(String hashcode) {
        JoloRoom_ApplyChangeTableReq_40002 req = reqHashMap.get(Integer.parseInt(hashcode));
        if (req != null) {
            req.run();
        }
        reqHashMap.remove(hashcode);
        //log.info("exc change abel hashcode:"+hashcode);
    }

    public void changeTable(ClientReq req) {
        JoloRoom_ApplyChangeTableReq_40002 req_40002 = (JoloRoom_ApplyChangeTableReq_40002) req;
        saveReq(req_40002);

        String userId = req_40002.req.getUserId();
        String gameId = req_40002.req.getGameId();
        String roomId = req_40002.req.getRoomId();
        String tableId = req_40002.req.getTableId();

        //离桌子
        JSONObject js = new JSONObject();
        js.put("gameId", gameId);
        js.put("roomId", roomId);
        js.put("tableId", tableId);
        js.put("userId", userId);
        js.put("hashcode", req_40002.hashCode());
        StoredObjManager.publish(js.toString(), RedisChannel.CHANGE_TABLE_CHANNEL.getChannelName());
        log.info("send to leave table hashcode:"+req_40002.hashCode());
    }

    public void saveReq(JoloRoom_ApplyChangeTableReq_40002 req_40002) {
        reqHashMap.put(req_40002.hashCode(), req_40002);
    }
}
