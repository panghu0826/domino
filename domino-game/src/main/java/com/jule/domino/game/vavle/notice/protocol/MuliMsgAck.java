package com.jule.domino.game.vavle.notice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class MuliMsgAck extends NoticeAck {
    public static final int FUNCTION_ID = MuliMsgReq.FUNCTION_ID | 0xFF000000;

    public MuliMsgAck(ChannelHandlerContext ctx) {
        super(FUNCTION_ID, ctx);
    }

    @Override
    public void readPayLoadImpl(ByteBuf byteBuf) throws Exception {

    }

    @Override
    public void processImpl() throws Exception {

    }
}
