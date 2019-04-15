package com.jule.domino.notice.network;

import com.jule.core.configuration.ThreadConfig;
import com.jule.core.network.codec.FrameDecoder;
import com.jule.core.network.codec.FramePrepender;
import com.jule.domino.notice.config.Config;
import com.jule.domino.notice.network.codec.RequestDecoder;
import com.jule.domino.notice.network.codec.ResponseEncoder;
import com.jule.domino.notice.network.handler.IoHandler;
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
    private static final EventExecutorGroup EVENT_EXECUTORS = new DefaultEventExecutorGroup(1);

    public static void connect() {
        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(ThreadConfig.CHILD_GROUP_THREADS);
        try {
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15 * 1000)
                    //.handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("Idl", new IdleStateHandler(15, 15, 15));
                            //p.addLast("Logger", new LoggingHandler(LogLevel.DEBUG));
                            p.addLast("FramePrepender", new FramePrepender());
                            p.addLast("FrameDecoder", new FrameDecoder());
                            p.addLast("ResponseEncoder", new ResponseEncoder());
                            p.addLast("RequestDecoder", new RequestDecoder());
                            p.addLast(EVENT_EXECUTORS, "IoHandler", new IoHandler());
                        }
                    });

            bootstrap.bind(Config.NOTICE_BIND_IP,Config.NOTICE_BIND_PORT).sync();
            logger.info("notice service bind port " + Config.NOTICE_BIND_PORT);
            // Wait until the server socket is closed.
            // f.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("", e);
        }
    }

}
