package com.jule.domino.game.gate.network.process.reqs;

import com.jule.domino.game.gate.network.GateFunctionFactory;
import com.jule.domino.game.gate.network.protocol.Req;
import io.netty.buffer.ByteBuf;

public class PingReq_60000 extends Req {
    public PingReq_60000(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
    }

    @Override
    public void processImpl() throws Exception {
        GateFunctionFactory.getInstance().getResponse(functionId | 0x08000000, new byte[0]).send(ctx, reqHeader);
    }
}
