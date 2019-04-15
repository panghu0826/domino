package com.jule.domino.notice.service;

import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.notice.model.GateServerInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JedisService {
    private final static Logger logger = LoggerFactory.getLogger(JedisService.class);

    private static class SingletonHolder {
        protected static final JedisService instance = new JedisService();
    }

    public static final JedisService getInstance() {
        return JedisService.SingletonHolder.instance;
    }

    /**
     * 获得用户所在GateSvr的IP和端口
     *
     * @param userId
     * @return IP:Port
     */
    public String getUserInWhichGateSvr(String gameId, String userId) {
        String redisKey = GameConst.USER_LINK_INFO + gameId;
        String gateSvr = JedisPoolWrap.getInstance().hGetString(redisKey, userId);
        logger.debug("Redis key->" + redisKey + ", userId->" + userId + ", gateSvr->" + gateSvr);
        if (StringUtils.isNotBlank((gateSvr))) {
            return gateSvr;
        }
        return null;
    }

    /**
     * 获得用户所在GateSvr的IP和端口
     *
     * @param userIds
     * @return IP:Port
     */
    public List<String> getAllUserInWhichGateSvr2(String gameId, List<String> userIds) {
        String redisKey = GameConst.USER_LINK_INFO + gameId;
        List<String> gateSvrs = JedisPoolWrap.getInstance().hMget2(redisKey, userIds);
        logger.debug("Redis key->" + redisKey + ", userIds->" + userIds + ", gateSvr->" + gateSvrs);
        return gateSvrs;
    }

    /**
     * 获得GateSvr列表
     *
     * @param gameId
     * @return
     */
    public List<GateServerInfo> GetGateSvrList(String gameId) {
        List<GateServerInfo> svrList = new ArrayList<>();
        String redisKey = RedisConst.GATE_SVR_LIST.getProfix() + gameId;
        Set<String> allGateSvr = StoredObjManager.smember(redisKey);
        logger.debug("Redis key->" + redisKey + ", allGateSvrMap->" + (null == allGateSvr ? "null" : allGateSvr));
        if (null != allGateSvr) {
            for (String key : allGateSvr) {
                String ipPort = key;
                logger.debug("allGateSvrMap key->" + key + ", ipPort->" + ipPort);
                if (!StringUtils.isBlank(ipPort)) {
                    String[] arrIpPort = ipPort.split(":");
                    if (arrIpPort.length == 2) {
                        GateServerInfo svrInfo = new GateServerInfo();
                        svrInfo.setGateSvrId(key);
                        svrInfo.setIp(arrIpPort[0]);
                        svrInfo.setPort(arrIpPort[1]);
                        svrList.add(svrInfo);
                    }
                }
            }
        }
        return svrList;
    }
}
