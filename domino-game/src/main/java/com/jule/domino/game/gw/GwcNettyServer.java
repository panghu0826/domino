package com.jule.domino.game.gw;

import com.jule.domino.game.gw.netty.GwcIoHandler;
import com.jule.domino.game.gw.netty.GwcRegisterService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


/**
 * gwc 网关控制器连接
 *
 * @author
 *
 * @since 2018/11/15 14:56
 */
@Slf4j@Setter
public class GwcNettyServer {

    public static final GwcNettyServer OBJ = new GwcNettyServer();

    private  ServerBootstrap server = new ServerBootstrap();
    //绑定host
    private String hosts = "192.168.0.14";
    //绑定端口
    private int port = 54001;

    /**
     * 绑定端口地址
     * @param hosts
     * @param port
     * @return
     */
    public GwcNettyServer bind(String hosts, int port){
        this.hosts = hosts;
        this.port = port;
        return this;
    }

    /**
     * 绑定端口
     * @param port
     * @return
     */
    public GwcNettyServer bind(int port){
        this.port = port;
        return this;
    }

    /**
     * 启动
     */
    public void start(){
        try {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup(5);

            server.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("IoHandler", new GwcIoHandler());
                        }
                    });

            server.bind(hosts, port)
                  .addListener((ChannelFutureListener) future -> GwcRegisterService.OBJ.checkChannel());

            log.info("启动netty服务器完成,绑定{}：{}", hosts, port);
        }catch (Exception ex){
            log.error("启动netty服务器异常 ex={}",ex);
        }
    }

}
