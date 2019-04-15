package com.jule.domino.dispacher.network;

import com.jule.core.configuration.ThreadConfig;
import com.jule.core.network.handler.WsRequestHandler;
import com.jule.domino.dispacher.network.handler.BinaryWebSocketFrameHandler;
import com.jule.domino.dispacher.network.handler.DispacherHandler;
import com.jule.domino.dispacher.network.mail.MailDiscoveService;
import com.jule.domino.dispacher.config.Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;

/**
 * Created by xujian on 2016/8/6.
 * 监听通知服务器实现类
 */
public class WSIOServer {

    private final static Logger logger = LoggerFactory.getLogger(WSIOServer.class);
    private static final ServerBootstrap bootstrap = new ServerBootstrap();
    private static final EventExecutorGroup EVENT_EXECUTORS = new DefaultEventExecutorGroup(50);

    public static void connect() {

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(20);
        EventLoopGroup workerGroup = new NioEventLoopGroup(ThreadConfig.CHILD_GROUP_THREADS);
        try {
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15 * 1000)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
//                            pipeline.addLast("logger", new LoggingHandler(LogLevel.DEBUG));
                            if (Config.ENABLE_SSL && JoLoSslContext.DEFAULT != null) {
                                SSLEngine sslEngine = JoLoSslContext.DEFAULT.createSSLEngine();
                                sslEngine.setUseClientMode(false);
                                sslEngine.setNeedClientAuth(false);
                                pipeline.addLast(new SslHandler(sslEngine));
                            }
                            pipeline.addLast("Idl", new IdleStateHandler(60, 60, 60));
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new WsRequestHandler("/auth"));
                            pipeline.addLast(new WebSocketServerProtocolHandler("/auth"));
                            pipeline.addLast(new BinaryWebSocketFrameHandler());
                            pipeline.addLast(EVENT_EXECUTORS, new DispacherHandler());
                        }
                    });

            bootstrap.bind(Config.BIND_IP, Config.BIND_PORT).addListener((ChannelFutureListener) future -> {
                MailDiscoveService.getInstance().onServerStartUp();
            });
            logger.info("service bind ip "+Config.BIND_IP);
            logger.info("service bind port " + Config.BIND_PORT);

        } catch (Exception e) {
            logger.error("", e);
        }
    }

}
