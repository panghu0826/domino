package com.jule.domino.game.network.codec;

import com.jule.domino.game.network.ClientPacketFactory;
import com.jule.domino.game.network.protocol.ClientReq;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Created by xujian on 2017/4/16.
 */
@Slf4j
public class RequestDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
//        logger.debug("do a message decode");
        try {
            int functionId = byteBuf.readInt();
//            logger.debug("decode a functionId = "+functionId);
            ClientReq request = ClientPacketFactory.getInstance().getClientReq(functionId, channelHandlerContext);

            if (request != null && request.readPayLoad(byteBuf)) {
                list.add(request);
            }
        } catch (Exception ex) {
            log.error("decode msg error, ex = " + ex.getMessage());
        }
    }
}
