package com.jule.domino.game.vavle.notice.codec;

import com.jule.domino.game.vavle.notice.protocol.NoticeReq;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by xujian on 2017/4/16.
 */
public class RequestEncoder extends MessageToMessageEncoder<NoticeReq> {
    private final static Logger logger = LoggerFactory.getLogger(RequestEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, NoticeReq msg, List<Object> out) throws Exception {
        ByteBuf byteBuf = msg.encode(ctx);
        if (byteBuf != null) {
            out.add(byteBuf);
        }
    }
}
