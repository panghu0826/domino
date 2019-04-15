package com.jule.domino.game.vavle.notice.protocol;

import com.google.protobuf.MessageLite;
import com.jule.domino.game.service.RegisterService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xujian on 2017/12-18
 */
public class NoticeReq {

    private final static Logger logger = LoggerFactory.getLogger(NoticeReq.class);
    private int functionId;
    private int gameId;
    protected MessageLite body;

    public ByteBuf encode(final ChannelHandlerContext channelHandlerContext) {
        byte[] bytes = body == null ? new byte[0] : body.toByteArray();
        ByteBuf buffer = channelHandlerContext.alloc().buffer(28 + bytes.length);
        try {
            buffer.writeInt(functionId);
            buffer.writeInt(gameId);
            buffer.writeInt(RegisterService.GAME_SERVER_ID);
            buffer.writeInt(1);
            buffer.writeInt((int) (System.currentTimeMillis() / 1000));
            buffer.writeLong(0);
            buffer.writeBytes(bytes);
            logger.debug("send " + functionId + " ->" + body.toString());
            return buffer;
        } catch (Exception e) {
            ReferenceCountUtil.release(buffer);
            logger.error("NoticeReq Encode Error", e);
            return null;
        }
    }

    /**
     * @param functionId
     * @param messageLite
     */
    public NoticeReq(int gameId,int functionId, MessageLite messageLite) {
        this.functionId = functionId;
        this.body = messageLite;
        this.gameId = gameId;
    }
}
