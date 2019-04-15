package com.jule.domino.gate.model;

import com.alibaba.fastjson.JSONObject;
import com.jule.domino.gate.config.Config;
import com.jule.domino.gate.vavle.game.SubGameConnectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xujian 2018-02-26
 */
public class SubGameGroup {
    private final static Logger logger = LoggerFactory.getLogger(SubGameGroup.class);
    private int subGameId;
    private Map<Integer, SubGame> subGameServerIdToSubGame = new ConcurrentHashMap<>();

    public SubGameGroup(int subGameId) {
        this.subGameId = subGameId;
        logger.info("Init SubGameGroup->" + subGameId);
    }

    /**
     * 更新游戏服务器信息
     */
    public void updateSubGameInfo(JSONObject object) {
        int subServerId = object.getIntValue("serverId");
        String ip = object.getString("ip");
        int port = object.getIntValue("port");
        int onlineNum = object.getIntValue("onlineNum");
        logger.info("Sync SubGame(" + subGameId + ") State->" + object.toString());
        Object ret = subGameServerIdToSubGame.put(subServerId, new SubGame(subGameId, subServerId, ip, port, onlineNum));
        if (ret == null) {
            new SubGameConnectPool(subGameId, subServerId, ip, port);
        }
    }

    /**
     * 找出一个可用的服务器id
     *
     * @return
     */
    public int getAvaiableSubGameServerId() {
        int _onlineNum = -1;
        int _serverId = -1;
        for (Map.Entry<Integer, SubGame> entry : subGameServerIdToSubGame.entrySet()) {
            int onlineNum = entry.getValue().getOnlineNum();
            if (onlineNum < Config.MAX_ONLINE && onlineNum >= 0) {
                //小于0 视为服务器已挂
                if (_onlineNum < onlineNum) {
                    _onlineNum = onlineNum;
                    _serverId = entry.getKey();
                }
            }
        }
        if (_onlineNum == -1) {
            logger.warn("All SubGame(" + subGameId + ") Server Is Full");
            //全都负载高取一个负载最小的
            _onlineNum = Integer.MAX_VALUE;
            _serverId = -1;
            for (Map.Entry<Integer, SubGame> entry : subGameServerIdToSubGame.entrySet()) {
                int onlineNum = entry.getValue().getOnlineNum();
                if (onlineNum >= 0) {
                    //小于0 视为服务器已挂
                    if (_onlineNum > onlineNum) {
                        _onlineNum = onlineNum;
                        _serverId = entry.getKey();
                    }
                }
            }
        }
        return _serverId;
    }

    @Override
    public String toString() {
        return "SubGameGroup{" +
                "subGameId=" + subGameId +
                ", subGameServerIdToSubGame=" + subGameServerIdToSubGame +
                '}';
    }
}
