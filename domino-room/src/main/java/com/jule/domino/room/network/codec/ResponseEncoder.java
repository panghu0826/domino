package com.jule.domino.room.network.codec;

import com.jule.domino.room.network.protocol.ClientAck;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by xujian on 2017/4/16.
 */
public class ResponseEncoder extends MessageToMessageEncoder<ClientAck> {
    private final static Logger logger = LoggerFactory.getLogger(ResponseEncoder.class);


    @Override
    protected void encode(ChannelHandlerContext ctx, ClientAck msg, List<Object> out) throws Exception {
        ByteBuf byteBuf = ctx.alloc().buffer(1024);
        if(msg.encode(byteBuf)){
            out.add(byteBuf);
        }
    }
}
