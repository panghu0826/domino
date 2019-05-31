package com.jule.domino.game.gate.network.process.reqs;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.gate.service.UserService;
import com.jule.domino.game.log.producer.RabbitMqSender;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;


/**
 * 通知游戏关闭socket
 */
@Slf4j
public class JoloCommon_NoticeCloseReq_600026 extends Req {

    private byte[] blob;

    public JoloCommon_NoticeCloseReq_600026(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
    }

    @Override
    public void processImpl() throws Exception {
        JoloAuth.JoLoCommon_CloseNoticeReq req = JoloAuth.JoLoCommon_CloseNoticeReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId,req.toString());
        log.info("gate收到600026,req={}",req.toString());

        JoloAuth.JoLoCommon_CloseNoticeAck.Builder builder = JoloAuth.JoLoCommon_CloseNoticeAck.newBuilder();
        try {
            //断game
            UserService.getInstance().onUserBreak(ctx, false);
        } catch (Exception e) {
            log.error("Close gate error, ex = {}", e.getMessage(), e);
        } finally {
            //返回消息
            builder.setResult(1).setResultMsg("");
            sendResponse(functionId | 0x08000000, builder.build().toByteArray());

            //断gate连接
            ctx.close();
        }
    }

}
