package com.jule.domino.room.network;

import com.jule.core.configuration.ThreadConfig;
import com.jule.core.network.codec.FrameDecoder;
import com.jule.core.network.codec.FramePrepender;
import com.jule.domino.room.config.Config;
import com.jule.domino.room.network.codec.RequestDecoder;
import com.jule.domino.room.network.codec.ResponseEncoder;
import com.jule.domino.room.network.handler.IoHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xujian on 2016/8/6.
 */
public class IOServer {
    private static final ServerBootstrap bootstrap = new ServerBootstrap();
    private final static Logger logger = LoggerFactory.getLogger(IOServer.class);
    private static final EventExecutorGroup ROOM_EVENT_EXECUTORS = new DefaultEventExecutorGroup(2);

    public static void  connect() {
        // Configure the server.
//        EventLoopGroup bossGroup = new EpollEventLoopGroup(1);
//        EventLoopGroup workerGroup = new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors());
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(ThreadConfig.CHILD_GROUP_THREADS);
        try {
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15 * 1000)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("Idl", new IdleStateHandler(15, 15, 15));
                            p.addLast("FramePrepender", new FramePrepender());
                            p.addLast("FrameDecoder", new FrameDecoder());
                            p.addLast("ResponseEncoder", new ResponseEncoder());
                            p.addLast("RequestDecoder", new RequestDecoder());
                            p.addLast(ROOM_EVENT_EXECUTORS, "IoHandler", new IoHandler());
                        }
                    });

            bootstrap.bind(Config.ROOM_BIND_IP,Config.ROOM_BIND_PORT).sync();
            logger.info("room service bind port " + Config.ROOM_BIND_PORT);
            // Wait until the server socket is closed.
            // f.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("IOServer connect error", e);
        }
    }

}
