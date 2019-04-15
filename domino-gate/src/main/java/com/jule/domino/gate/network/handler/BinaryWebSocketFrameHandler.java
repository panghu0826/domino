package com.jule.domino.gate.network.handler;

import com.jule.core.network.codec.PackageMessage;
import com.jule.domino.gate.network.GateFunctionFactory;
import com.jule.domino.gate.network.protocol.Req;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author xujian
 */
public class BinaryWebSocketFrameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {
    private final static Logger logger = LoggerFactory.getLogger(BinaryWebSocketFrameHandler.class);
    private static ConcurrentHashMap<Integer, PackageMessage> datePack = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) {

        Channel channel = ctx.channel();
        Integer hashcode = channel.hashCode();
        PackageMessage pm = datePack.get(hashcode);
        if (pm == null) {
            pm = new PackageMessage();
            pm.setSendTime(System.currentTimeMillis());
            pm.setSendCount(new AtomicLong(1));
            datePack.put(hashcode, pm);
        } else {
            AtomicLong acl = pm.getSendCount();
            if (acl.get() % 30L == 0) {
                long time = System.currentTimeMillis() - pm.getSendTime();
                if (time <= 10000) {
                    //logger.error("发送30条数据，耗时小于10s，实际耗时是:" + time + "毫秒，关闭socket");
                    datePack.remove(hashcode);
                    //将这个channel地址，写进redis
                    String list[] = channel.remoteAddress().toString().split(":");
                    String ipl = list[0];
                    String ip = ipl.substring(1);
                    ctx.channel().close();
                } else {
                    //logger.info("发送30条数据的耗时是:" + time);
                    pm.setSendTime(System.currentTimeMillis());
                    acl.set(1);
                    pm.setSendCount(acl);
                }
            } else {
                acl.getAndIncrement();
            }
        }


        ByteBuf byteBuf = msg.content();
        int length = byteBuf.readInt();
        int functionId = byteBuf.readInt();
        int gameId = byteBuf.readInt();
        int gameServerId = byteBuf.readInt();
        boolean isAsync = byteBuf.readInt() == 1;
        int reqNum = byteBuf.readInt();
        int tmp1 = byteBuf.readInt();
        int tmp2 = byteBuf.readInt();
        logger.debug("gameServerId -> " + gameServerId);
        logger.debug("received funcId -> " + String.format("0x%02X %s", functionId, functionId));
        logger.debug("length -> " + length);
        logger.debug("gameId -> " + gameId);
        logger.debug("reqNum -> " + reqNum);
        logger.debug("tmp1 -> " + tmp1);
        logger.debug("tmp2 -> " + tmp2);
        Req.ReqHeader reqeHader = new Req.ReqHeader(functionId, gameId, gameServerId, isAsync, reqNum);
        Req request = GateFunctionFactory.getInstance().getRequest(reqeHader, ctx);
        if (request != null && request.readPayLoad(byteBuf)) {
            ctx.fireChannelRead(request);
            logger.debug("transfer success ");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
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
