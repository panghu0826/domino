package com.jule.domino.notice.valve.gate;

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
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GateSvr连接池
 */
public class GateConnectPool {
    private final static Logger logger = LoggerFactory.getLogger(GateConnectPool.class);
    private final Bootstrap bootstrap;//是否用一个统一的io 线程组
    private static final int size = 5;
    private static final AtomicInteger idx = new AtomicInteger(0);
    private static final Map<String,List<ChannelHandlerContext>> CHANNELS = new HashMap<>();
    private final String ip;
    private final int port;
    @Getter
    private final String gateServerId;

    public boolean canAdd(String ipAndPort) {
        List<ChannelHandlerContext> list = CHANNELS.get(ipAndPort);
        int num = list==null?0:list.size();
        return num < size;
    }
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
        this.bootstrap = new Bootstrap().group(new NioEventLoopGroup(1))
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true);
        init(this,ipAndPort);
    }

    private void init(GateConnectPool gateConnectPool,String ipAndPort) {
        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("Idl", new IdleStateHandler(10, 10, 10));
                pipeline.addLast("GateFrameDecoder", new FrameDecoder());
                pipeline.addLast("GatePrepender", new FramePrepender());
                pipeline.addLast("GateProcess", new GateMsgHandler(gateConnectPool));
            }
        });

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> reConnect(ipAndPort), 0, 5 * 1000, TimeUnit.MILLISECONDS);
        GateServerGroup.getInstance().registerGameServerGroup(this);
    }

    /**
     * 开始连接，补充连接
     */
    private void reConnect(String ipAndPort) {
        List<ChannelHandlerContext> list = CHANNELS.get(ipAndPort);
        int num = list==null?0:list.size();
        int needNum = this.size - num;
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
    public static synchronized ChannelHandlerContext getConnection(String ipAndPort) {
        List<ChannelHandlerContext> list = CHANNELS.get(ipAndPort);
        if(list==null){
            return null;
        }
        logger.debug("getConnection ::: idx -> " + idx.get() + ", CHANNELS.size -> " + (list==null?-1:list.size()));
        if (idx.get() >= list.size()) {
            idx.set(0);
        }
        if (list.size() == 0) {
            return null;
        }
        return list.get(idx.getAndIncrement());
    }

    /*
     *
     */
    public void addChannel(String gateServerId,ChannelHandlerContext channelHandlerContext) {
        List<ChannelHandlerContext> list = CHANNELS.get(gateServerId);
        if(list==null) {
            list = new ArrayList<>();
        }
        list.add(channelHandlerContext);
        CHANNELS.put(gateServerId,list);
    }

    /**
     * @param channelHandlerContext
     */
    public void delConnect(String gateServerId,ChannelHandlerContext channelHandlerContext) {
        List<ChannelHandlerContext> list = CHANNELS.get(gateServerId);
        if(list!=null) {
            list.remove(channelHandlerContext);
        }
    }
}
