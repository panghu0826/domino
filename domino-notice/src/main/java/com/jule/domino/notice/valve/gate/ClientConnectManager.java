package com.jule.domino.notice.valve.gate;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端链接管理器
 */
@Deprecated
public class ClientConnectManager {
    private final static Logger logger = LoggerFactory.getLogger(ClientConnectManager.class);
    public final static Map<Long, ChannelHandlerContext> CHANNELS = new ConcurrentHashMap<>();

    private static class SingletonHolder {
        protected static final ClientConnectManager instance = new ClientConnectManager();
    }

    public static final ClientConnectManager getInstance() {
        return ClientConnectManager.SingletonHolder.instance;
    }

    /**
     * @param channelHandlerContext
     */
    public void addConnect(ChannelHandlerContext channelHandlerContext) {
        CHANNELS.put(Long.valueOf(channelHandlerContext.channel().id().toString(), 16), channelHandlerContext);
    }

    /**
     * @param channelId
     * @return
     */
    public ChannelHandlerContext getConnect(long channelId) {
        return CHANNELS.get(channelId);
    }

    /**
     * @param channelHandlerContext
     */
    public void delConnect(ChannelHandlerContext channelHandlerContext) {
        CHANNELS.remove(Long.valueOf(channelHandlerContext.channel().id().toString(), 16), channelHandlerContext);
    }
}
