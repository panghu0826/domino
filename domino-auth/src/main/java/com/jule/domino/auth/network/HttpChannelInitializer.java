package com.jule.domino.auth.network;

import com.jule.domino.auth.network.handler.RouteHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final EventExecutorGroup EVENT_EXECUTORS = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors());
    private final SSLContext sslContext;
    private final SslContext sslCtx;
    private boolean isQa = false;

    public HttpChannelInitializer(SSLContext sslContext, SslContext sslCtx, boolean isQa) {
        this.sslContext = sslContext;
        this.sslCtx = sslCtx;
        this.isQa = isQa;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        //测试模式、自签
        if (isQa && sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
        //线上模式、证书
        if (!isQa && sslContext != null){
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false); //服务器端模式
            sslEngine.setNeedClientAuth(false); //不需要验证客户端
            pipeline.addLast(new SslHandler(sslEngine));
        }

        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65535));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(EVENT_EXECUTORS, new RouteHandler());
    }
}
