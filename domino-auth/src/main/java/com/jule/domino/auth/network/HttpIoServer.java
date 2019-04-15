package com.jule.domino.auth.network;


import com.jule.core.configuration.ThreadConfig;
import com.jule.domino.auth.config.Config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Created by xujian on 2016/8/6.
 */
public class HttpIoServer {
    private static final ServerBootstrap bootstrap = new ServerBootstrap();
    private final static Logger logger = LoggerFactory.getLogger(HttpIoServer.class);

    public static void accept() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(ThreadConfig.CHILD_GROUP_THREADS);
        try {
                final SslContext ssl;
                SelfSignedCertificate ssc = new SelfSignedCertificate("192.168.0.14");
                ssl = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();

                KeyStore ks = KeyStore.getInstance("JKS");
                InputStream ksInputStream = new FileInputStream("./config/"+ Config.SSL_FILE);
                ks.load(ksInputStream, Config.SSL_KEY.toCharArray());
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(ks, Config.SSL_KEY.toCharArray());
                final SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(kmf.getKeyManagers(), null, null);


            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15 * 1000)
                    .childHandler(new HttpChannelInitializer(sslContext,ssl,Config.QA_MODE));

            bootstrap.bind(Config.BIND_IP,Config.BIND_PORT);
            logger.info("auth service bind port " + Config.BIND_PORT);
        } catch (Exception e) {
            logger.error("", e);
        }
    }
}
