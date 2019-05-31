package com.jule.domino.game.gate.network.handler;

import JoloProtobuf.NoticeSvr.JoloNotice;
import com.jule.domino.game.gate.service.UserService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoticeIoHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final static Logger logger = LoggerFactory.getLogger(NoticeIoHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("received notice link->" + ctx.toString());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                logger.debug(ctx.toString() + " notice server read idle close connection");
                ctx.close();
            } else if (event.state() == IdleState.WRITER_IDLE) {
                logger.debug(ctx.toString() + " notice server write idle");
                ctx.close();
            } else if (event.state() == IdleState.ALL_IDLE) {
                logger.debug(ctx.toString() + " notice server all idle");
                ctx.close();
            }
        }
    }

    /**
     * 接收消息只有一个消息
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        int functionId = msg.readInt();
        msg.readInt();
        msg.readInt();
        msg.readInt();
        int reqNum = msg.readInt();
        msg.readInt();
        msg.readInt();
        if(functionId == 1){
            ByteBuf byteBuf = ctx.alloc().buffer(28);
            byteBuf.writeInt(1);
            byteBuf.writeInt(0);
            byteBuf.writeInt(0);
            byteBuf.writeInt(0);
            byteBuf.writeInt(0);
            byteBuf.writeInt(0);
            byteBuf.writeInt(0);
            ctx.writeAndFlush(byteBuf);
            //心跳忽略
            return;
        }
        if (functionId == 70000) {

            byte[] playLoad = new byte[msg.readableBytes()];
            msg.readBytes(playLoad);

            JoloNotice.JoloNotice2Gate_MsgReq joloNotice2Gate_msgReq = JoloNotice.JoloNotice2Gate_MsgReq.parseFrom(playLoad);

            String userId = joloNotice2Gate_msgReq.getUserId();

            JoloNotice.JoloNotice_PayLoad payLoad = JoloNotice.JoloNotice_PayLoad.parseFrom(joloNotice2Gate_msgReq.getContent());
//            logger.info("received notice msg functionId-> " + functionId + " reqNum->" + reqNum + " 外壳payLoad->" + joloNotice2Gate_msgReq.toString() + " 内容payLoad-> " + payLoad);
            logger.info("游戏广播 -> functionId：{}，userId：{}，gameId：{}，gameSvrId：{}",payLoad.getFunctionId(),joloNotice2Gate_msgReq.getUserId(),payLoad.getGameId(),payLoad.getGameSvrId());

            UserService.getInstance().sendNoticeMsg(userId, payLoad);
        } else {
            logger.info("received error functionId->" + functionId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
