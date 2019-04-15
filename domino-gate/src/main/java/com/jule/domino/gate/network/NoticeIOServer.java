package com.jule.domino.gate.network;

import com.jule.core.network.codec.FrameDecoder;
import com.jule.core.network.codec.FramePrepender;
import com.jule.domino.gate.config.Config;
import com.jule.domino.gate.network.handler.NoticeIoHandler;
import com.jule.domino.gate.service.RegisteService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
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
public class NoticeIOServer {
    private static final ServerBootstrap bootstrap = new ServerBootstrap();
    private static final EventExecutorGroup EVENT_EXECUTORS = new DefaultEventExecutorGroup(1);
    private final static Logger logger = LoggerFactory.getLogger(NoticeIOServer.class);

    public static void connect() {
        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(2);
        try {
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15 * 1000)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            //p.addLast("logger", new LoggingHandler(LogLevel.DEBUG));
                            p.addLast("Idl", new IdleStateHandler(15, 15, 15));
                            p.addLast("FramePrepender", new FramePrepender());
                            p.addLast("FrameDecoder", new FrameDecoder());
                            p.addLast(EVENT_EXECUTORS, "NoticeIoHandler", new NoticeIoHandler());
                        }
                    });

            bootstrap.bind(Config.BIND_IP,Config.NOTICESERVER_BIND_PORT).addListener((ChannelFutureListener) future -> {
                RegisteService.getInstance().onServiceStartUp();
            });
            logger.info("NoticeIOServer bind port " + Config.NOTICESERVER_BIND_PORT);

        } catch (Exception e) {
            logger.error("", e);
        }
    }

}
