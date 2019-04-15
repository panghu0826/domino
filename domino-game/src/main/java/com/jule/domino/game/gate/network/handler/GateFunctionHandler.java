package com.jule.domino.game.gate.network.handler;

import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.gate.pool.net.ChannelManageCenter;
import com.jule.domino.game.gate.service.UserService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
public class GateFunctionHandler extends SimpleChannelInboundHandler<Req> {
    private final static Logger logger = LoggerFactory.getLogger(GateFunctionHandler.class);
    private final static ExecutorService executorService = Executors.newFixedThreadPool(8);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //ClientConnectManager.getInstance().addConnect(ctx);
        ChannelManageCenter.getInstance().addChannel(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //ClientConnectManager.getInstance().delConnect(ctx);
        log.error("消息通道 channelInactive");
        ChannelManageCenter.getInstance().removeTempSession(ctx);
        UserService.getInstance().onUserBreak(ctx,false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Req msg) {
        /*
         * msg.run();
         * modify by gx 20181023 避免msg对象run方法的执行时间长，而导致后面新来的消息被阻塞排队，因此将每个msg当成独立线程去执行，确保接收消息不出现延迟。
         */
        executorService.submit(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("消息通道 exceptionCaught");
        //ClientConnectManager.getInstance().delConnect(ctx);
        ChannelManageCenter.getInstance().removeTempSession(ctx);
        UserService.getInstance().onUserBreak(ctx,true);
    }
}
