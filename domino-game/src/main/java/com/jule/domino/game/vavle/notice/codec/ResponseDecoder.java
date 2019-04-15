package com.jule.domino.game.vavle.notice.codec;

import com.jule.domino.game.vavle.notice.protocol.MuliMsgAck;
import com.jule.domino.game.vavle.notice.protocol.NoticeAck;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by xujian on 2017/4/16.
 */
public class ResponseDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final static Logger logger = LoggerFactory.getLogger(ResponseDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int functionId = msg.readInt();
        msg.readInt();
        msg.readInt();
        msg.readInt();
        msg.readInt();
        msg.readInt();
        msg.readInt();
        NoticeAck noticeAck = getNoticeAck(functionId, ctx);
        if (noticeAck != null && noticeAck.readPayLoad(msg)) {
            out.add(noticeAck);
        }
    }

    /**
     * 找到对应的对象
     *
     * @return
     */
    private NoticeAck getNoticeAck(int functionId, ChannelHandlerContext ctx) {
        switch (functionId) {
            case MuliMsgAck.FUNCTION_ID:
                return new MuliMsgAck(ctx);
            case -16777215:
                return null;//心跳不用处理
            default:
//       todo         logger.warn("received unkonw msg->" + functionId);
                return null;
        }
    }
}
