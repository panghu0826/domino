package com.jule.domino.game.network.handler;

import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.play.AbstractTable;
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
    public static final AttributeKey<PlayerInfo> ATTACH_PLAYER = AttributeKey.valueOf("player");
    private final static Logger logger = LoggerFactory.getLogger(IoHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        ctx.channel().attr(IP).set(ip);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ClientReq request) throws Exception {
        //logger.debug("channelRead0, requestIsNull->"+(null==request));
        if (request != null) {
            AbstractTable table = request.getTable();
            //logger.debug("channelRead0, tableIsNull->"+(null==table));
            if (table != null) {
                table.getFifoRunnableQueue().execute(request);
            }else {
                request.run();
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //logger.debug("userEventTriggered " + ctx.channel().attr(IP).get()+", class="+evt.getClass()+", idleClass="+IdleStateEvent.class+", isTrue="+IdleStateEvent.class.isAssignableFrom(evt.getClass()));
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                ctx.close();
                //logger.debug(ctx.toString() + " read idle close connection");
            } else if (event.state() == IdleState.WRITER_IDLE) {
                ctx.close();
                //logger.debug(ctx.toString() + " write idle");
            } else if (event.state() == IdleState.ALL_IDLE) {
                ctx.close();
                //logger.debug(ctx.toString() + " all idle");
            }
        }
    }
}
