package com.jule.domino.game.room.network.protocol.reqs;

import com.jule.domino.game.network.protocol.ClientHeader;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.room.network.protocol.acks.DummyAck_01;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyReq_01 extends ClientReq {

    private final static Logger logger = LoggerFactory.getLogger(DummyReq_01.class);

    public DummyReq_01(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf byteBuf) throws Exception {

    }

    @Override
    public void processImpl() {
        //logger.debug("收到心跳消息 " + functionId);
        ctx.writeAndFlush(new DummyAck_01(null, ClientHeader.DEFAULT_HEADER));
    }
}
