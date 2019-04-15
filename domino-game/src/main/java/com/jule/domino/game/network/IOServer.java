package com.jule.domino.game.network;

import com.jule.core.configuration.ThreadConfig;
import com.jule.core.network.codec.FrameDecoder;
import com.jule.core.network.codec.FramePrepender;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.network.codec.RequestDecoder;
import com.jule.domino.game.network.codec.ResponseEncoder;
import com.jule.domino.game.network.handler.IoHandler;
import com.jule.domino.game.service.RegisterService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
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
//        EventLoopGroup bossGroup = PlatformDependent.isWindows() ? new NioEventLoopGroup(1) : new EpollEventLoopGroup(1);
//        EventLoopGroup workerGroup = PlatformDependent.isWindows() ? new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()) : new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors());
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(ThreadConfig.CHILD_GROUP_THREADS);

//        EventLoopGroup bossGroup = new EpollEventLoopGroup(1);
//        EventLoopGroup workerGroup = new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors());
        try {
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
//            bootstrap.group(bossGroup, workerGroup).channel(EpollServerSocketChannel.class)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15 * 1000)
                    //.handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("Idl", new IdleStateHandler(15, 15, 15));
                            p.addLast("FramePrepender", new FramePrepender());
                            p.addLast("FrameDecoder", new FrameDecoder());
                            p.addLast("ResponseDecoder", new ResponseEncoder());
                            p.addLast("RequestEncoder", new RequestDecoder());
                            p.addLast("IoHandler", new IoHandler());
                        }
                    });

            bootstrap.bind(Config.BIND_IP, Config.BIND_PORT).addListener(
                    (ChannelFutureListener) future -> RegisterService.getInstance().onServiceStartUp());
            logger.info("game service bind port " + Config.BIND_PORT);
            // Wait until the server socket is closed.
            // f.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("", e);
        }
    }
}
