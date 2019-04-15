package com.jule.domino.notice.network.protocol.reqs;

import com.jule.domino.notice.network.protocol.acks.DummyAck_01;
import com.jule.domino.notice.network.protocol.ClientHeader;
import com.jule.domino.notice.network.protocol.ClientReq;
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
        ctx.writeAndFlush(new DummyAck_01(null, ClientHeader.DEFAULT_HEADER));
    }
}
