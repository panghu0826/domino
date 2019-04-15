package com.jule.domino.dispacher.network.process;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.domino.base.service.PlayerRecordService;
import com.jule.domino.dispacher.network.protocol.Req;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * 请求牌局记录
 *
 */
@Slf4j
public class JoloAuth_GameRecordsReq_80007 extends Req {

    private JoloAuth.JoloCommon_PlayRecordsReq req;

    public JoloAuth_GameRecordsReq_80007(int functionId ) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl( ByteBuf buf ) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloCommon_PlayRecordsReq.parseFrom(blob);
    }

    @Override
    public void processImpl() throws Exception {
        log.info("收到消息functionId = {}, 消息体body={}",functionId,req.toString());

        JoloAuth.JoloCommon_PlayRecordsAck.Builder ack = JoloAuth.JoloCommon_PlayRecordsAck.newBuilder();
        String userId = req.getUserId();
        String gameId = req.getGameId();

        try {
            //构建消息
            ack.setUserId(userId)
                    .setResult(1)
                    .setResultMsg("")
                    .addAllRecords(PlayerRecordService.OBJ.getPlayRecordsAuth(userId, Integer.valueOf(gameId)));

            log.debug("send - > {}", ack.toString());

            sendAcqMsg(ack);
        }catch (Exception e){
            log.error("构建消息失败、e = {}",e.getMessage());
        }

    }
}
