package com.jule.domino.game.gw.netty;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 网关消息接收器
 *
 * @author
 *
 * @since 2018/11/15 18:52
 */
@Slf4j@Sharable
public class GwcIoHandler extends ChannelInboundHandlerAdapter {

    //初始化线程池
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("连接创建");
        GwcMsgSerivce.OBJ.regIoHandler(ctx);
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("连接失效");
        GwcMsgSerivce.OBJ.removeIoHandler(ctx);
    }

    /**
     * 当有数据时，自动调用，读取msg
     * @param ctx
     * @param msg
     * @throws Exception
     */
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //提交异步线程处理
        executor.submit(() -> handler(ctx, msg));
    }

    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("消息异常", cause);
        GwcMsgSerivce.OBJ.removeIoHandler(ctx);
        ctx.close();
    }

    private void handler(ChannelHandlerContext ctx, Object msg){
        try {
            //构造消息对象
            GwcMsg gwcMsg = GwcMsgSerivce.OBJ.decode(msg);
            if (gwcMsg == null){
                log.error("解码失败");
                return;
            }

            //获得消息处理器
            AbstractGwcHander hander = GwcMsgSerivce.OBJ.getHandler(gwcMsg.getCmd());
            if (hander == null){
                log.warn("消息msgId= {},无对应的处理器");
                return;
            }

            //处理消息
            hander.process(ctx, gwcMsg);
        }catch (Exception ex){
            log.error("读取异常",ex);
        }
    }

}
