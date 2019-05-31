package com.jule.domino.game.gate.network.process.reqs;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.game.gate.login.LoginService;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.log.producer.RabbitMqSender;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 可以理解为游戏内的登陆采用本地自制策略
 */
@Slf4j
public class JoloCommon_LoginReq_600001 extends Req {

    private final static ExecutorService executor = Executors.newFixedThreadPool(10);

    private byte[] blob;

    public JoloCommon_LoginReq_600001(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
    }

    @Override
    public void processImpl() throws Exception {
        JoloAuth.JoloCommon_LoginReq req = JoloAuth.JoloCommon_LoginReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId,req.toString());
        log.info("gate收到600001,req={}",req.toString());

        JoloAuth.JoloCommon_LoginAck.Builder builder = JoloAuth.JoloCommon_LoginAck.newBuilder();
        executor.submit(()->{
            try {
                LoginService.OBJ.processLogin(req, ctx, reqHeader);

            } catch (Exception e) {
                log.error("GateLogin ERROR, ex = {}", e.getMessage(), e);
                builder.setResult(0).setVerify(req.getVerify()).setResultMsg(ErrorCodeEnum.GAME_50002_2.getCode());
                sendResponse(functionId | 0x08000000, builder.build().toByteArray());
            }
        });
    }

}
