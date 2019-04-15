package com.jule.domino.dispacher.network.handler;

import com.jule.core.network.codec.PackageMessage;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.network.DispacherFunctionFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
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
        //logger.debug("length = " + length);
        logger.debug("received funcId -> " + String.format("0x%02X %d", functionId, functionId));
        Req.ReqHeader reqeHader = new Req.ReqHeader(functionId, gameId, gameServerId, isAsync, reqNum);
        Req request = DispacherFunctionFactory.getInstance().getRequest(reqeHader, ctx);
        if (request != null && request.readPayLoad(byteBuf)) {
            ctx.fireChannelRead(request);
        }
    }
}
