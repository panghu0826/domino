package com.jule.robot.network.handler;

import com.jule.core.utils.fifo.FIFORunnableQueue;
import com.jule.robot.network.protocol.ClientReq;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class IoHandler extends SimpleChannelInboundHandler<ClientReq> {

    public static final AttributeKey<FIFORunnableQueue<ClientReq>> EXEC = AttributeKey.valueOf("queue");
    public static final AttributeKey<String> IP = AttributeKey.valueOf("ip");
    private final static Logger logger = LoggerFactory.getLogger(IoHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("连接异常断线.......", cause);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().attr(EXEC).set(new FIFORunnableQueue<ClientReq>() {
        });
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        ctx.channel().attr(IP).set(ip);
        logger.debug("link opened from " + ctx.channel().attr(IP).get());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("link closed from " + ctx.channel().attr(IP).get());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ClientReq request) throws Exception {
        if (request != null) {
            channelHandlerContext.channel().attr(EXEC).get().execute(request);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                logger.debug(ctx.toString() + " read idle close connection");
                //ctx.close();
            } else if (event.state() == IdleState.WRITER_IDLE) {
                //logger.debug(ctx.toString() + " write idle");
            } else if (event.state() == IdleState.ALL_IDLE) {
                //logger.debug(ctx.toString() + " all idle");
            }
        }
    }
}
