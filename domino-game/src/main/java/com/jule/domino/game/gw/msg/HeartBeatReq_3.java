package com.jule.domino.game.gw.msg;

import com.jule.domino.game.gw.netty.AbstractGwcHander;
import com.jule.domino.game.gw.netty.GwcMsg;
import com.jule.domino.game.gw.netty.GwcMsgID;
import io.netty.channel.ChannelHandlerContext;

/**
 * 心跳消息
 */
public class HeartBeatReq_3 extends AbstractGwcHander {

    public HeartBeatReq_3() {
        super(GwcMsgID.HeartbeatReq, GwcMsgID.HeartbeatRes);
    }

    @Override
    public void process(ChannelHandlerContext ctx, GwcMsg msg) throws Exception {
        log.debug("收到心跳消息cmd = {}", msg.getCmd());
        //回复心跳
        sendMsg(ctx, new byte[0]);
    }
}
