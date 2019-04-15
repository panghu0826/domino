package com.jule.domino.dispacher.network.mail;

import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.GameSvrRelationModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author lyb 2018-06-21
 * 游戏服务发现服务
 */
@Slf4j
public class MailDiscoveService {

    /**
     * 本地缓存的游戏服务器列表
     */
    private final static Map<String, Integer> CACHED_MAILSERVER_TABLE = new LinkedHashMap();

    private static class SingletonHolder {
        protected static final MailDiscoveService instance = new MailDiscoveService();
    }

    public static final MailDiscoveService getInstance() {
        return MailDiscoveService.SingletonHolder.instance;
    }

    public void onServerStartUp() {
        discover();
        //开始发现线程
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> discover(), 15000, 30 * 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * 服务监听启动后调用
     */
    private void discover() {
        Set<GameSvrRelationModel> list = StoredObjManager.smembers(RedisConst.MAIL_SVR_LIST.getProfix(), GameSvrRelationModel.class);
        if (list == null || list.size() == 0) {
            log.warn("get mail server error");
            //throw new Error("get mail server error");
        }
        list.forEach(svrRelationModel -> {
            String field = svrRelationModel.getAddress() + svrRelationModel.getGameSvrId();
            String value = StoredObjManager.hget(RedisConst.MAIL_SVR_EXPIRE.getProfix() + svrRelationModel.getGameSvrId(),
                    RedisConst.MAIL_SVR_EXPIRE.getField() + field);
            long lastTime = 0;
            if (StringUtils.isNotEmpty(value)) {
                lastTime = Long.parseLong(value);
            }
            if (System.currentTimeMillis() - lastTime > GameConst.offlineGameSec * 1000L) {
                StoredObjManager.srem(RedisConst.MAIL_SVR_LIST.getProfix(), svrRelationModel);
                return;
            }
            int _svrId = Integer.parseInt(svrRelationModel.getGameSvrId());
            log.debug(RedisConst.MAIL_SVR_LIST + "注册的mailSvrId：" + _svrId);
            String address = svrRelationModel.getAddress();
            if (!CACHED_MAILSERVER_TABLE.containsKey(address)) {
                CACHED_MAILSERVER_TABLE.put(address, _svrId);
                log.info("发现MAIL服务器->" + address + ",MailSvrId->" + _svrId);
                new MailConnectPool(_svrId, address);
            }

        });
    }

}
