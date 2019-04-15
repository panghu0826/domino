package com.jule.domino.dispacher.network.process;

import com.jule.core.common.log.LoggerUtils;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.network.mail.MailServerGroup;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lyb 2018-06-21
 */
@Slf4j
public class MailProxyReq extends Req {
    public MailProxyReq(int functionId) {
        super(functionId);
    }

    private byte[] bytes;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
    }

    @Override
    public void processImpl() throws Exception {
        ChannelHandlerContext channelHandlerContext = null;
        ByteBuf byteBuf = null;
        try {
            channelHandlerContext = MailServerGroup.getInstance().getConnect();
            LoggerUtils.mailLog.info("MailProxyReq con is null ?"+(channelHandlerContext==null?true:false)+
            ",reqHeader:"+reqHeader.toString());
            if (channelHandlerContext != null) {
                byteBuf = channelHandlerContext.alloc().buffer(bytes.length + 28);
                byteBuf.writeInt(reqHeader.functionId);
                byteBuf.writeInt(reqHeader.gameId);
                byteBuf.writeInt(reqHeader.gameServerId);
                byteBuf.writeInt(reqHeader.isAsync ? 1 : 0);
                byteBuf.writeInt(reqHeader.reqNum);
                //临时写到预留字段里面标识是那个链接
                byteBuf.writeLong(Long.valueOf(ctx.channel().id().toString(), 16));
                byteBuf.writeBytes(bytes);
                channelHandlerContext.writeAndFlush(byteBuf);
            } else {
                log.error("channelHandlerContext is null");
            }
        } catch (Exception e) {
            log.error("ReqHeader:{}," + System.getProperty("line.separator") + "Exception msg:{}", reqHeader.toString(), e.getMessage(), e);
        }

    }
}
