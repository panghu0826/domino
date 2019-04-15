package com.jule.domino.game.gate.network.handler;

import com.jule.core.network.codec.PackageMessage;
import com.jule.core.network.msg.GateCoder;
import com.jule.core.network.msg.MessagePack;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.gate.network.GateFunctionFactory;
import com.jule.domino.game.gate.network.protocol.Req;
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
        long start = System.currentTimeMillis();
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

        GateCoder.OBJ.encryptionAndDecryption(byteBuf, Config.GAME_ENCRYPE_ISOPEN, Config.GAME_ENCRYPE_KEY.getBytes(),0);

        //解析消息头
        MessagePack pack = GateCoder.OBJ.decodeHeader(byteBuf, Config.GATE_MSG_REVERSAL);
        logger.debug("msgHeader = {}", pack.toString());

        //重新构建转发消息头
        Req.ReqHeader reqeHader = new Req.ReqHeader(pack.getFunctionId(), pack.getGameId(), pack.getGameSvrId(), pack.isAsync(), pack.getReqNum());
        Req request = GateFunctionFactory.getInstance().getRequest(reqeHader, ctx);
        if (request != null && request.readPayLoad(byteBuf)) {
            ctx.fireChannelRead(request);
            logger.debug("transfer success ");
        }
        if(pack.getFunctionId() != GateFunctionFactory.__function__id_60000) {
            long useTimes = System.currentTimeMillis() - start;
            if(useTimes > 500) {
                logger.error("BinaryWebSocketFrameHandler 读客户端消息，解析对应Req对象。 耗时->{} 毫秒", System.currentTimeMillis() - start); //TODO:临时增加
            }
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
