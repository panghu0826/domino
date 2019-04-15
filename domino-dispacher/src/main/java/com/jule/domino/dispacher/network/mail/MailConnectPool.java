package com.jule.domino.dispacher.network.mail;

import com.jule.core.network.codec.FrameDecoder;
import com.jule.core.network.codec.FramePrepender;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 邮件连接池
 */
@Slf4j
public class MailConnectPool {
    private AtomicLong al = new AtomicLong(0L);
    private final Bootstrap bootstrap;//是否用一个统一的io 线程组
    private final int size = 10;
    private volatile int idx = 0;
    private volatile List<ChannelHandlerContext> CHANNELS = new ArrayList<>();// Collections.synchronizedList(new ArrayList<>());
    private final String ip;
    private final int port;
    private final int mailSvrId;


    public boolean canAdd() {
        return CHANNELS.size() < size;
    }

    /**
     * @param mailSvrId
     * @param address
     */
    public MailConnectPool(int mailSvrId, String address) {
        this.mailSvrId = mailSvrId;
        this.ip = address.split(":")[0];
        this.port = Integer.parseInt(address.split(":")[1]);
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
        this.bootstrap = new Bootstrap().group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true);
        init(this);
    }

    private void init(MailConnectPool mailConnectPool) {
        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                //pipeline.addLast("Logger", new LoggingHandler(LogLevel.DEBUG));
                pipeline.addLast("Idl", new IdleStateHandler(3, 3, 3));
                pipeline.addLast("GameFrameDecoder", new FrameDecoder());
                pipeline.addLast("GamePrepender", new FramePrepender());
                pipeline.addLast("GameProcess", new MailMsgHandler(mailConnectPool));
            }
        });
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> reConnect(), 0, 3 * 1000, TimeUnit.MILLISECONDS);
        MailServerGroup.getInstance().registerGameServerGroup(this);
    }

    /**
     * 开始连接，补充连接
     */
    private void reConnect() {

        int needNum = this.size - this.CHANNELS.size();
        if (needNum > 0) {
            log.info("mailSvr size ->" + CHANNELS.size() + ", need->" + needNum + "ipAddress:" + ip + ":" + port);
        }
        if (needNum > 0) {
            for (int i = 0; i < needNum; i++) {
                bootstrap.connect(ip, port);
            }
        }
        if(al.incrementAndGet()%10==0){
            log.info("mailSvr size ->" + CHANNELS.size() + ", need->" + needNum + "ipAddress:" + ip + ":" + port);
        }
        if(al.get()>1-000-000-000){
            al = new AtomicLong(0L);
        }
    }

    /**
     * 获取一个链接当前就一个连接可用
     *
     * @return
     */
    public synchronized ChannelHandlerContext getConnection() {
        if (idx >= CHANNELS.size()) {
            idx = 0;
        }
        if (CHANNELS.size() == 0) {
            log.warn("mail pool channel is 0");
            return null;
        }
        return CHANNELS.get(idx++);
    }

    /**
     * @return
     */
    public int getMailSvrId() {
        return mailSvrId;
    }

    /*
     *
     */
    public void addChannel(ChannelHandlerContext channelHandlerContext) {

        CHANNELS.add(channelHandlerContext);
    }

    /**
     * @param channelHandlerContext
     */
    public void delConnect(ChannelHandlerContext channelHandlerContext) {

        CHANNELS.remove(channelHandlerContext);
    }
}
