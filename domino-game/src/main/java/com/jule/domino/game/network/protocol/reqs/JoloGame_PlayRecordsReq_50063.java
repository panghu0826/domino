package com.jule.domino.game.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.acks.JoloGame_PlayRecordsAck_50063;
import com.jule.domino.game.service.PlayerRecordSerivce;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

/**
 * 牌局历史记录
 *
 * @author
 *
 * @since 2018/9/11 18:55
 */
@Slf4j
public class JoloGame_PlayRecordsReq_50063 extends ClientReq{

    private JoloGame.JoloGame_PlayRecordsReq req;

    public JoloGame_PlayRecordsReq_50063(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf byteBuf) throws Exception {
        byte[] blob = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(blob);
        req = JoloGame.JoloGame_PlayRecordsReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId,req.toString());
    }

    @Override
    public void processImpl() throws Exception {

        log.info("收到消息functionId = {}, 消息体body={}",functionId,req.toString());
        String userId = req.getUserId();

        //返回消息体
        JoloGame.JoloGame_PlayRecordsAck.Builder ack = JoloGame.JoloGame_PlayRecordsAck.newBuilder();

        try {
            //构建消息
            ack.setUserId(userId)
                    .setResult(1)
                    .setResultMsg("")
                    .addAllRecords(PlayerRecordSerivce.OBJ.getPlayRecords(userId));

            log.debug("send - > {}", ack.toString());
            ctx.writeAndFlush(new JoloGame_PlayRecordsAck_50063(ack.build(), header));
        }catch (Exception e){
            log.error("functionId = {},消息处理失败 exception = {}", functionId, e.getMessage());
            //构建失败消息
            ack.setUserId(userId)
                    .setResult(-1)
                    .setResultMsg(ErrorCodeEnum.GAME_50063_1.getCode());
            ctx.writeAndFlush(new JoloGame_PlayRecordsAck_50063(ack.build(), header));
        }

    }
}
