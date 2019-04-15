package com.jule.domino.gate.vavle.room;

import com.jule.core.network.codec.FrameDecoder;
import com.jule.core.network.codec.FramePrepender;
import com.jule.domino.gate.config.Config;
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

/**
 * @author xujian 2017-12-15
 * 房间链接工厂
 */
public class RoomConnectPool {
    private final static Logger logger = LoggerFactory.getLogger(RoomConnectPool.class);
    private static Bootstrap bootstrap;
    public static final int size = 10;
    private static volatile int idx = 0;
    public static final List<ChannelHandlerContext> CHANNELS = new ArrayList<>();

    static {
        init();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> reConnect(), 0, 5 * 1000, TimeUnit.MILLISECONDS);
    }

    private static void init() {
        bootstrap = new Bootstrap().group(new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()))
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        //pipeline.addLast("Logger", new LoggingHandler(LogLevel.DEBUG));
                        pipeline.addLast("Idl", new IdleStateHandler(3, 3, 3));
                        pipeline.addLast(new FrameDecoder());
                        pipeline.addLast(new FramePrepender());
                        pipeline.addLast(new RoomMsgHandler());
                    }
                });
    }

    /**
     * 重连
     */
    private static void reConnect() {
        int needNum = size - CHANNELS.size();
        if(needNum > 0) {
            logger.info("room.vavle size ->" + CHANNELS.size() + ", need->" + needNum);
        }
        if (needNum > 0) {
            for (int i = 0; i < needNum; i++) {
                //try {
                    bootstrap.connect(Config.ROOM_FORWARD_PORT);//.sync();
                //}catch (Exception ex){
                //    logger.error(ex.getMessage(),ex);
                //}finally {
                //    bootstrap.group().shutdownGracefully();
                //}
            }
        }
    }

    /**
     * 获取一个链接当前就一个连接可用
     *
     * @return
     */
    public synchronized static ChannelHandlerContext getConnection() {
        if (idx >= CHANNELS.size()) {
            idx = 0;
        }
        if (CHANNELS.size() == 0) {
            return null;
        }
        return CHANNELS.get(idx++);
    }
}
