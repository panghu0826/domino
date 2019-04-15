package com.jule.domino.gate.service;

import com.jule.core.jedis.JedisPoolWrap;
import com.jule.domino.base.enums.GameConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JedisService {
    private final static Logger logger = LoggerFactory.getLogger(JedisService.class);


    private static class SingletonHolder {
        protected static final JedisService instance = new JedisService();
    }

    public static final JedisService getInstance() {
        return JedisService.SingletonHolder.instance;
    }

    /**
     * 需要对结果做断言
     *
     * @return
     */
    public boolean server_information(String gameId,String user_id, String ip_port) {
        return JedisPoolWrap.getInstance().hSet(GameConst.USER_LINK_INFO+gameId, user_id, ip_port);
    }
}
