package com.jule.robot.service;

import com.jule.core.jedis.JedisPoolWrap;
import com.jule.robot.model.GateServerInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JedisService {
    private final static Logger logger = LoggerFactory.getLogger(JedisService.class);
    private final static String ROOM_STAT = "ROOM_STAT";
    private final static String GATESVR_LIST_KEY = "GATE_GROUP_";

    private static class SingletonHolder {
        protected static final JedisService instance = new JedisService();
    }

    public static final JedisService getInstance() {
        return JedisService.SingletonHolder.instance;
    }

    /**
     * 获得GateSvr列表
     * @param gameId
     * @return
     */
    public List<GateServerInfo> GetGateSvrList(int gameId){
        List<GateServerInfo> svrList = new ArrayList<GateServerInfo>();
        String redisKey = GATESVR_LIST_KEY + gameId;
        Map<String, String> allGateSvrMap = JedisPoolWrap.getInstance().hGetAll(redisKey);
        logger.debug("Redis key->"+ redisKey + ", allGateSvrMap->" + (null==allGateSvrMap?"null":allGateSvrMap));
        if(null != allGateSvrMap){
            Set<String> keys = allGateSvrMap.keySet();
            for(String key : keys){
                String ipPort = key;
                logger.debug("allGateSvrMap key->"+ key + ", ipPort->" + ipPort);
                if(!StringUtils.isBlank(ipPort)){
                    String[] arrIpPort = ipPort.split(":");
                    if(arrIpPort.length == 2){
                        GateServerInfo svrInfo = new GateServerInfo();
                        svrInfo.setGateSvrId(key);
                        svrInfo.setIp(arrIpPort[0]);
                        svrInfo.setPort(Integer.parseInt(arrIpPort[1]));
                        svrList.add(svrInfo);
                    }
                }
            }
        }
        return svrList;
    }
}
