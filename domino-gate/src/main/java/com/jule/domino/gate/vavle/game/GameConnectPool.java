package com.jule.domino.gate.vavle.game;

import com.jule.core.network.codec.FrameDecoder;
import com.jule.core.network.codec.FramePrepender;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
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
 * 游戏连接池
 */
public class GameConnectPool {
    private final static Logger logger = LoggerFactory.getLogger(GameConnectPool.class);
    private final Bootstrap bootstrap;//是否用一个统一的io 线程组
    private final int size = 10;
    private volatile int idx = 0;
    private volatile List<ChannelHandlerContext> CHANNELS = new ArrayList<>();// Collections.synchronizedList(new ArrayList<>());
    private final String ip;
    private final int port;
    private final int gameSvrId;


    public boolean canAdd() {
        return CHANNELS.size() < size;
    }

    /**
     * @param gameSvrId
     * @param address
     */
    public GameConnectPool(int gameSvrId, String address) {
        logger.debug("成功初始化game连接池");
        this.gameSvrId = gameSvrId;
        this.ip = address.split(":")[0];
        this.port = Integer.parseInt(address.split(":")[1]);
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
        this.bootstrap = new Bootstrap().group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true);
        //eventLoopGroup.shutdownGracefully();
        init(this);
    }

    private void init(GameConnectPool gameConnectPool) {
        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                //pipeline.addLast("Logger", new LoggingHandler(LogLevel.DEBUG));
                pipeline.addLast("Idl", new IdleStateHandler(3, 3, 3));
                pipeline.addLast("GameFrameDecoder", new FrameDecoder());
                pipeline.addLast("GamePrepender", new FramePrepender());
                pipeline.addLast("GameProcess", new GameMsgHandler(gameConnectPool));
            }
        });
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> reConnect(), 0, 5 * 1000, TimeUnit.MILLISECONDS);
        //放到游戏服组里待使用
        GameServerGroup.getInstance().registerGameServerGroup(this);
    }

    /**
     * 开始连接，补充连接
     */
    private void reConnect() {
        int needNum = this.size - this.CHANNELS.size();
        if (needNum > 0) {
            logger.info("game.vavle size ->" + CHANNELS.size() + ", need->" + needNum);
            logger.debug("链接地址--------------------" + ip + ":" + port);
        }
        if (needNum > 0) {
            for (int i = 0; i < needNum; i++) {
                //try {
                ChannelFuture c = bootstrap.connect(ip, port);//.sync();
                logger.debug("-----------------gate获取连接：" + c.toString() + "连接是否为空：" + (c == null));
                //} catch (Exception ex) {
                //    logger.error(ex.getMessage(), ex);
                //} finally {
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
    public synchronized ChannelHandlerContext getConnection() {
        if (idx >= CHANNELS.size()) {
            idx = 0;
        }
        if (CHANNELS.size() == 0) {
            logger.warn("game pool channel is 0");
            return null;
        }
        return CHANNELS.get(idx++);
    }

    /**
     * @return
     */
    public int getGameServerId() {
        return gameSvrId;
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
