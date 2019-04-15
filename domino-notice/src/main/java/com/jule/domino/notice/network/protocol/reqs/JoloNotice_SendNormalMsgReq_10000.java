package com.jule.domino.notice.network.protocol.reqs;

import com.jule.domino.notice.network.protocol.acks.JoloNotice_SendNormalMsgAck_10000;
import com.jule.domino.notice.model.NormalMsgInfo;
import com.jule.domino.notice.network.protocol.ClientReq;
import com.jule.domino.notice.service.thread.SendNormalMsgThread;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import JoloProtobuf.NoticeSvr.*;

public class JoloNotice_SendNormalMsgReq_10000 extends ClientReq {
    private final static Logger logger = LoggerFactory.getLogger(JoloNotice_SendNormalMsgReq_10000.class);
    private final static String NORMAL_NOTICE_MSG_REDIS_KEY = "Notice_Normal_Msg";

    private JoloNotice.JoloNotice_SendNormalMsgReq req;

    public JoloNotice_SendNormalMsgReq_10000(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf byteBuf) throws Exception {
        byte[] msg = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(msg);
        req = JoloNotice.JoloNotice_SendNormalMsgReq.parseFrom(msg);
    }

    @Override
    public void processImpl() throws Exception {
        logger.debug("收到消息-> " + functionId + " reqNum-> " + header.reqNum + "req->" + req.toString());
        String userId = req.getUserId();
        int gameId = header.gameId;

        //启动线程：发送消息到GateSvr
        NormalMsgInfo msgInfo = new NormalMsgInfo();
        msgInfo.setUserId(userId);
        msgInfo.setGameId(gameId);
        msgInfo.setMsgContent(req.getContent().toByteArray());
        msgInfo.setReqNum(header.reqNum);
        logger.debug("reqNum-> " + header.reqNum + ", gameId->"+gameId+", userId->"+userId);

        SendNormalMsgThread smThread = new SendNormalMsgThread(msgInfo);
        Thread thread = new Thread(smThread);
        thread.run();
        //直接移到队列里了 不需要起线程了
        //thread.start();

        //返回结果给TCP客户端
        JoloNotice.JoloNotice_SendNormalMsgAck.Builder builder = JoloNotice.JoloNotice_SendNormalMsgAck.newBuilder();
        builder.setUserId(userId);
        builder.setResult(1);
        ctx.writeAndFlush(new JoloNotice_SendNormalMsgAck_10000(builder.build(), header));
    }
}
