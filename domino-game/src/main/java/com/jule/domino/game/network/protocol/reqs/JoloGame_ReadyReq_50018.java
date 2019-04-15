package com.jule.domino.game.network.protocol.reqs;

import com.jule.domino.game.network.protocol.ClientReq;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JoloGame_ReadyReq_50018 extends ClientReq {

    public JoloGame_ReadyReq_50018(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf byteBuf) throws Exception {

    }

    @Override
    public void processImpl() throws Exception {

    }


}
