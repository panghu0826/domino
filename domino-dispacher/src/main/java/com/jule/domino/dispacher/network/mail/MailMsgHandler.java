package com.jule.domino.dispacher.network.mail;

import com.jule.core.common.log.LoggerUtils;
import com.jule.domino.dispacher.network.protocol.Ack;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.service.UserService;
import com.jule.domino.dispacher.network.DispacherFunctionFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;


/**
 * @author lyb 2018-06-21
 */
@Slf4j
public class MailMsgHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private MailConnectPool mailConnectPool;

    public MailMsgHandler(MailConnectPool mailConnectPool) {
        this.mailConnectPool = mailConnectPool;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (this.mailConnectPool.canAdd()) {
            this.mailConnectPool.addChannel(ctx);
        } else {
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.mailConnectPool.delConnect(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        int functionId = msg.readInt();
        if ((functionId & 0x00FFFFFF) == 1) {//心跳
            return;
        }

        int gameId = msg.readInt();
        int gameSvrId = msg.readInt();
        boolean isAsync = msg.readInt() == 1;
        int reqNum = msg.readInt();
        long targetChannelId = msg.readLong();


        byte[] playLoad = new byte[msg.readableBytes()];
        msg.readBytes(playLoad);
        Req.ReqHeader header = new Req.ReqHeader(functionId, gameId, gameSvrId, isAsync, reqNum);


        ChannelHandlerContext channelHandlerContext = UserService.getInstance().getChannel(targetChannelId);

        LoggerUtils.mailLog.info("To Client targetChannelId:{},functionId:{},con is null?{},channelHandlerContext.channel().isActive()?{}"
                , targetChannelId,functionId ,(channelHandlerContext == null ? true : false) , channelHandlerContext.channel().isActive());
        if (channelHandlerContext == null || !channelHandlerContext.channel().isActive()) {
            log.debug("client link closed");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("functionId:" + functionId);
        int funcId = header.functionId & 0x00FFFFFF;
        sb.append("1 funcId:" + funcId);
        funcId = funcId % 600000;
        sb.append("2 funcId:" + funcId);
        funcId = funcId | 0x08000000;
        sb.append("3 funcId:" + funcId);

        LoggerUtils.mailLog.info(sb.toString());
        Ack ack = DispacherFunctionFactory.getInstance().getResponse(funcId, playLoad);
        if (ack != null) {
            ack.setFunctionId(header.functionId);
            ack.send(channelHandlerContext, header);
        }

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE || event.state() == IdleState.WRITER_IDLE || event.state() == IdleState.ALL_IDLE) {
                ByteBuf byteBuf = ctx.alloc().buffer(28);
                byteBuf.writeInt(1);
                byteBuf.writeInt(0);
                byteBuf.writeInt(0);
                byteBuf.writeInt(0);
                byteBuf.writeInt(0);
                byteBuf.writeInt(0);
                byteBuf.writeInt(0);
                ctx.writeAndFlush(byteBuf);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.mailConnectPool.delConnect(ctx);
        ctx.close();
    }
}
