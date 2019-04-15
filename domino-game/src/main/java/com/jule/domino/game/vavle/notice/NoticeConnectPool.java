package com.jule.domino.game.vavle.notice;

import com.jule.core.network.codec.FrameDecoder;
import com.jule.core.network.codec.FramePrepender;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.vavle.notice.codec.RequestEncoder;
import com.jule.domino.game.vavle.notice.codec.ResponseDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xujian 2017-12-18
 */
public class NoticeConnectPool {
    private final static Logger logger = LoggerFactory.getLogger(NoticeConnectPool.class);
    private static Bootstrap bootstrap;
    private static final ReentrantLock LOCK = new ReentrantLock(true);
    private final int size = 5;
    private volatile int idx = 0;
    private volatile List<ChannelHandlerContext> CHANNELS = new ArrayList<>();

    private static class SingletonHolder {
        protected static final NoticeConnectPool instance = new NoticeConnectPool();
    }

    public static final NoticeConnectPool getInstance() {
        return NoticeConnectPool.SingletonHolder.instance;
    }

    /**
     * 通过检查连接池是否已经满了判断是否添加新的链接
     *
     * @return true or false
     */
    private boolean canAdd() {
        return CHANNELS.size() < size;
    }

    public NoticeConnectPool() {
        init();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> reConnect(), 0, 5 * 1000, TimeUnit.MILLISECONDS);
    }

    private static void init() {
        bootstrap = new Bootstrap().group(new NioEventLoopGroup(1))
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
//                        pipeline.addLast("Logger", new LoggingHandler(LogLevel.DEBUG));
                        pipeline.addLast("Idl", new IdleStateHandler(10, 10, 10));
                        pipeline.addLast(new FramePrepender());
                        pipeline.addLast(new FrameDecoder());
                        pipeline.addLast(new RequestEncoder());
                        pipeline.addLast(new ResponseDecoder());
                        pipeline.addLast(new NoticeMsgHandler());
                    }
                });
    }

    /**
     * 重连
     */
    private void reConnect() {
        int needNum = size - CHANNELS.size();
        logger.info("notice pool now size ->" + CHANNELS.size() + ", need->" + needNum + ", bind port: "+Config.NOTICE_FORWARD_PORT);
        if (needNum > 0) {
            for (int i = 0; i < needNum; i++) {
                bootstrap.connect(Config.NOTICE_FORWARD_PORT);
            }
        }
    }

    /**
     * 获取一个链接当前就一个连接可用
     *
     * @return
     */
    public ChannelHandlerContext getConnection() {

        LOCK.lock();
        try {
            if (idx >= CHANNELS.size()) {
                idx = 0;
            }
            if (CHANNELS.size() == 0) {
                logger.warn("current notice pool link is zero please check notice service ");
                return null;
            }

            ChannelHandlerContext channelHandlerContext = CHANNELS.get(idx++);
            if (channelHandlerContext == null || !channelHandlerContext.channel().isActive()) {
                logger.warn("obtain ctx is null! please attention!");
            }
            return channelHandlerContext;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        finally {
            LOCK.unlock();
        }
        return null;
    }

    /**
     * 链接激活调用这个补仓
     *
     * @param channelHandlerContext
     */
    public boolean onChannelActive(ChannelHandlerContext channelHandlerContext) {
        LOCK.lock();
        try {
            if (!canAdd()) {
                return false;
            }
            CHANNELS.add(channelHandlerContext);
            return true;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * 链接失效回掉
     */
    public void onChannelInActive(ChannelHandlerContext channelHandlerContext) {
        LOCK.lock();
        try {
            CHANNELS.remove(channelHandlerContext);
        } finally {
            LOCK.unlock();
        }
    }
}
