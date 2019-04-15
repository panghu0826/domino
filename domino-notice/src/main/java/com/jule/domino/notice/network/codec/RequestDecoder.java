package com.jule.domino.notice.network.codec;

import com.jule.domino.notice.network.ClientPacketFactory;
import com.jule.domino.notice.network.protocol.ClientReq;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by xujian on 2017/4/16.
 */
public class RequestDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final static Logger logger = LoggerFactory.getLogger(RequestDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int functionId = byteBuf.readInt();
        ClientReq request = ClientPacketFactory.getInstance().getClientReq(functionId, channelHandlerContext);
        if (request != null && request.readPayLoad(byteBuf)) {
            list.add(request);
        }
    }
}
