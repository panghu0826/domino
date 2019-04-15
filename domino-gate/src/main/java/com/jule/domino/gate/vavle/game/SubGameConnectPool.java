package com.jule.domino.gate.vavle.game;

import com.jule.core.network.codec.FrameDecoder;
import com.jule.core.network.codec.FramePrepender;
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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 游戏连接池
 */
public class SubGameConnectPool {
    private final static Logger logger = LoggerFactory.getLogger(SubGameConnectPool.class);
    private final Bootstrap bootstrap;//是否用一个统一的io 线程组
    private final int size = 10;
    private final AtomicInteger idx = new AtomicInteger(0);
    private final List<ChannelHandlerContext> CHANNELS = new LinkedList<>();
    private final String ip;
    private final int port;
    private final int gameSvrId;
    private final int subGameId;

    /**
     * 构造器
     *
     * @param gameSvrId
     */
    public SubGameConnectPool(int subGameId,int gameSvrId, String ip,int port) {
        this.subGameId = subGameId;
        this.gameSvrId = gameSvrId;
        this.ip = ip;
        this.port = port;
        this.bootstrap = new Bootstrap().group(new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()))
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true);
        init(this);
    }

    private void init(SubGameConnectPool gameConnectPool) {

        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                //pipeline.addLast("Logger", new LoggingHandler(LogLevel.DEBUG));
                pipeline.addLast("Idl", new IdleStateHandler(5, 5, 5));
                pipeline.addLast("GameFrameDecoder", new FrameDecoder());
                pipeline.addLast("GamePrepender", new FramePrepender());
                pipeline.addLast("GameProcess", new SubGameMsgHandler(gameConnectPool));
            }
        });

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> reConnect(), 0, 5 * 1000, TimeUnit.MILLISECONDS);
        SubGameServerGroup.getInstance().registerGameServerGroup(this);
    }

    /**
     * 开始连接，补充连接
     */
    private void reConnect() {
        int needNum = this.size - this.CHANNELS.size();
        if (needNum > 0) {
            for (int i = 0; i < needNum; i++) {
                bootstrap.connect(ip, port);
            }
        }
    }

    /**
     * 获取一个链接当前就一个连接可用
     *
     * @return
     */
    public synchronized ChannelHandlerContext getConnection() {
        if (idx.get() >= CHANNELS.size()) {
            idx.set(0);
        }
        if (CHANNELS.size() == 0) {
            return null;
        }
        return CHANNELS.get(idx.getAndIncrement());
    }

    /**
     * @return
     */
    public int getGameServerId() {
        return gameSvrId;
    }

    /*
     *
     */
    public synchronized void addChannel(ChannelHandlerContext channelHandlerContext) {
        CHANNELS.add(channelHandlerContext);
    }

    /**
     * @param channelHandlerContext
     */
    public synchronized void delConnect(ChannelHandlerContext channelHandlerContext) {
        CHANNELS.remove(channelHandlerContext);
    }
}
