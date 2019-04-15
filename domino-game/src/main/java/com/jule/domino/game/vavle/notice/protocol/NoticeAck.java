package com.jule.domino.game.vavle.notice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xujian on 2017/12/07.
 */
public abstract class NoticeAck implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(NoticeAck.class);

    protected ChannelHandlerContext ctx;
    protected int functionId;

    public NoticeAck(int functionId, ChannelHandlerContext ctx) {
        this.functionId = functionId;
        this.ctx = ctx;
    }

    public boolean readPayLoad(ByteBuf byteBuf) {
        try {
            readPayLoadImpl(byteBuf);
        } catch (Exception e) {
            logger.error("解包错误", e);
            return false;
        }

        return true;
    }

    protected abstract void readPayLoadImpl(ByteBuf byteBuf) throws Exception;

    @Override
    public void run() {
        try {
            processImpl();
        } catch (Exception e) {
            logger.error("业务处理异常", e);
        }
    }

    public abstract void processImpl() throws Exception;

}
