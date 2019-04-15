package com.jule.domino.room.network.handler;

import com.jule.domino.base.service.holder.FunctionIdsHolder;
import com.jule.domino.room.network.protocol.ClientReq;
import com.jule.domino.room.service.ChangeTableService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class IoHandler extends SimpleChannelInboundHandler<ClientReq> {

    public static final AttributeKey<String> IP = AttributeKey.valueOf("ip");
    private final static Logger logger = LoggerFactory.getLogger(IoHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //logger.error(ctx.toString() + "break line", cause);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
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
            switch(request.getFunctionId()){
                case FunctionIdsHolder.Room_REQ_ChangeTable:
                    logger.info("rcv：changeTable ");
                    ChangeTableService.getInstance().changeTable(request);
                    break;
                default:
                    request.run();
                    break;
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            logger.debug(ctx.toString() + " idle close connection");
            if (event.state() == IdleState.READER_IDLE) {
                ctx.close();
            } else if (event.state() == IdleState.WRITER_IDLE) {
                ctx.close();
            } else if (event.state() == IdleState.ALL_IDLE) {
                ctx.close();
            }
        }
    }
}
