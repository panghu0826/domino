package com.jule.domino.gate.service;

import com.google.common.base.Strings;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.gate.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author xujian 2017-12-15
 * 服务器注册服务
 */
@Slf4j
public class RegisteService {

    public static String ADDRESS;

    private static class SingletonHolder {
        protected static final RegisteService instance = new RegisteService();
    }

    public static final RegisteService getInstance() {
        return RegisteService.SingletonHolder.instance;
    }

    public RegisteService() {
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            if(Config.GATESERVER_ISLOCAL){
                ip = GameConst.localHost;
            }
            log.info("RegisteService() ip:"+ip);
        } catch (UnknownHostException e) {
            log.error(e.getMessage(),e);
        }
        ADDRESS = Config.BIND_IP + ":" + Config.NOTICESERVER_BIND_PORT;
    }

    /**
     * 向redis 注册服务器
     */
    public void onServiceStartUp() {
        String[] gameIds = Config.GAME_IDS.split(":");
        for(String gameId:gameIds) {
            if (Strings.isNullOrEmpty(gameId)) {
                continue;
            }
            if(StoredObjManager.sadd(RedisConst.GATE_SVR_LIST.getProfix()+gameId,ADDRESS)){
            //if (JedisPoolWrap.getInstance().hSet(SERVER_KEY + gameId, ADDRESS, "")) {
                log.info("注册网关地址->" + ADDRESS + " 成功");
            } else {
                log.info("注册网关地址->" + ADDRESS + " 失败");
            }
        }

    }
}
