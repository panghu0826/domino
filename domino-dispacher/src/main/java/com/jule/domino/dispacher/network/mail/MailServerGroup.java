package com.jule.domino.dispacher.network.mail;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lyb 2018-06-21
 * mail 连接池组
 */
@Slf4j
public class MailServerGroup {
    private final static Map<Integer, MailConnectPool> GAME_CONNECT_POOL_MAP = new ConcurrentHashMap<>();

    private static class SingletonHolder {
        protected static final MailServerGroup instance = new MailServerGroup();
    }

    public static final MailServerGroup getInstance() {
        return MailServerGroup.SingletonHolder.instance;
    }

    /**
     * 注册连接池
     *
     * @param gameConnectPool
     */
    public synchronized void registerGameServerGroup(MailConnectPool gameConnectPool) {
        if (!GAME_CONNECT_POOL_MAP.containsKey(gameConnectPool.getMailSvrId())) {
            GAME_CONNECT_POOL_MAP.put(gameConnectPool.getMailSvrId(), gameConnectPool);
        }
    }

    /**
     * 可以不传mailSvrId,随机选择一个邮件服务器处理
     *
     * @return
     */
    public ChannelHandlerContext getConnect() {
        int index = (int) Math.floor(Math.random() * GAME_CONNECT_POOL_MAP.values().size());
        MailConnectPool[] ary = GAME_CONNECT_POOL_MAP.values().toArray(new MailConnectPool[0]);
        log.debug("getConnect():index:{},ary.length:{}", index, ary.length);
        MailConnectPool pool = ary.length > 0 ? ary[index] : null;
        if (pool != null) {
            return pool.getConnection();
        }
        return null;
    }

    /**
     * 可以不传mailSvrId,随机选择一个邮件服务器处理
     *
     * @param mailSvrId
     * @return
     */
    public ChannelHandlerContext getConnect(int mailSvrId) {
        MailConnectPool gameConnectPool = GAME_CONNECT_POOL_MAP.get(mailSvrId);
        log.debug("mailSvrId：" + mailSvrId + "-----gameConnectPool is not null?" + (GAME_CONNECT_POOL_MAP.get(mailSvrId) != null));
        if (gameConnectPool != null) {
            return gameConnectPool.getConnection();
        }
        return null;
    }

}
