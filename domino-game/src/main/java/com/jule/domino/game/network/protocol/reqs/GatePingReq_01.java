package com.jule.domino.game.network.protocol.reqs;

import com.jule.domino.game.network.protocol.ClientHeader;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.acks.GatePongAck_01;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 心跳消息网关->游戏服务器请求
 */
public class GatePingReq_01 extends ClientReq {

    private final static Logger logger = LoggerFactory.getLogger(GatePingReq_01.class);

    public GatePingReq_01(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf byteBuf) throws Exception {
        //现在是单向心态不需要返回
    }

    @Override
    public void processImpl() {
        ctx.writeAndFlush(new GatePongAck_01(null, ClientHeader.DEFAULT_HEADER));
    }
}
