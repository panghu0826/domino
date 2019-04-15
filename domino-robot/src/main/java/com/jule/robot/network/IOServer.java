package com.jule.robot.network;

import com.jule.core.network.codec.FrameDecoder;
import com.jule.core.network.codec.FramePrepender;
import com.jule.robot.config.Config;
import com.jule.robot.network.codec.RequestDecoder;
import com.jule.robot.network.codec.ResponseEncoder;
import com.jule.robot.network.handler.IoHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xujian on 2016/8/6.
 */
public class IOServer {
    private static final ServerBootstrap bootstrap = new ServerBootstrap();
    private final static Logger logger = LoggerFactory.getLogger(IOServer.class);

    public static void connect() {
        // Configure the server.
        EventLoopGroup bossGroup = PlatformDependent.isWindows() ? new NioEventLoopGroup(1) : new EpollEventLoopGroup(1);
        EventLoopGroup workerGroup = PlatformDependent.isWindows() ? new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()) : new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors());
        try {
            bootstrap.group(bossGroup, workerGroup).channel(PlatformDependent.isWindows() ? NioServerSocketChannel.class : EpollServerSocketChannel.class)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15 * 1000)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("Idl", new IdleStateHandler(75, 0, 0));
                            p.addLast("Logger", new LoggingHandler(LogLevel.DEBUG));
                            p.addLast("FramePrepender", new FramePrepender());
                            p.addLast("FrameDecoder", new FrameDecoder());
                            p.addLast("ResponseEncoder", new ResponseEncoder());
                            p.addLast("RequestDecoder", new RequestDecoder());
                            p.addLast("IoHandler", new IoHandler());
                        }
                    });

            bootstrap.bind(Config.ROBOT_BIND_PORT).sync();
            logger.info("robot service bind port " + Config.ROBOT_BIND_PORT);
            // Wait until the server socket is closed.
            // f.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("", e);
        }
    }

}
