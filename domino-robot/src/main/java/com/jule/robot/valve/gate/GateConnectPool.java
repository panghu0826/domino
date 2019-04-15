package com.jule.robot.valve.gate;

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
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GateSvr连接池
 */
public class GateConnectPool {
    private final static Logger logger = LoggerFactory.getLogger(GateConnectPool.class);
    private final Bootstrap bootstrap;//是否用一个统一的io 线程组
    private static final int size = 10;
    private static final AtomicInteger idx = new AtomicInteger(0);
    private static final List<ChannelHandlerContext> CHANNELS = new LinkedList<>();
    private final String ip;
    private final int port;
    private final String gateServerId;

    /**
     * 构造器
     *
     * @param ipAndPort
     */
    public GateConnectPool(String ipAndPort) {
        this.gateServerId = ipAndPort;
        this.ip = ipAndPort.split(":")[0];
        this.port = Integer.parseInt(ipAndPort.split(":")[1]);
        logger.debug("Create conn pool, ip->" + ip + ", port->" + port);
        this.bootstrap = new Bootstrap().group(new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()))
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true);
        init(this);
    }

    private void init(GateConnectPool gateConnectPool) {
        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("Logger", new LoggingHandler(LogLevel.DEBUG));
                pipeline.addLast("Idl", new IdleStateHandler(15, 15, 15));
                pipeline.addLast("GateFrameDecoder", new FrameDecoder());
                pipeline.addLast("GatePrepender", new FramePrepender());
                pipeline.addLast("GateProcess", new GateMsgHandler(gateConnectPool));
            }
        });

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> reConnect(), 0, 5 * 1000, TimeUnit.MILLISECONDS);
        GateServerGroup.getInstance().registerGameServerGroup(this);
    }

    /**
     * 开始连接，补充连接
     */
    private void reConnect() {
        int needNum = this.size - this.CHANNELS.size();
        logger.debug("reConnect ::: svrId->" + ip + ":" + port + ", this.size -> " + this.size + ", CHANNELS.size -> " + this.CHANNELS.size());
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
    public static ChannelHandlerContext getConnection() {
        logger.debug("getConnection ::: idx -> " + idx.get() + ", CHANNELS.size -> " + CHANNELS.size());
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
    public String getGateServerId() {
        return gateServerId;
    }

    /*
     *
     */
    public void addChannel(ChannelHandlerContext channelHandlerContext) {
        CHANNELS.add(channelHandlerContext);
    }

    /**
     * @param channelHandlerContext
     */
    public void delConnect(ChannelHandlerContext channelHandlerContext) {
        CHANNELS.remove(channelHandlerContext);
    }
}
