package com.jule.domino.game.notice.network.protocol.reqs;

import JoloProtobuf.NoticeSvr.JoloNotice;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.notice.model.GamePlayMsgInfo;
import com.jule.domino.game.notice.service.thread.SendGamePlayMsgThread;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoloNotice_SendGamePlayMsgReq_10001 extends ClientReq {
    private final static Logger logger = LoggerFactory.getLogger(JoloNotice_SendGamePlayMsgReq_10001.class);
    private final static String GAME_NOTICE_MSG_REDIS_KEY = "Game_Normal_Msg";

    private JoloNotice.JoloNotice_SendGamePlayMsgReq req;

    public JoloNotice_SendGamePlayMsgReq_10001(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf byteBuf) throws Exception {
        byte[] msg = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(msg);
        req = JoloNotice.JoloNotice_SendGamePlayMsgReq.parseFrom(msg);
    }

    @Override
    public void processImpl() throws Exception {
        logger.debug("收到消息-> " + functionId + " reqNum-> " + header.reqNum + "req->" + req.toString());
        logger.debug("收到通知-> {},当前时间-> {}", functionId, String.valueOf(System.currentTimeMillis()));
        int gameId = header.gameId;

        //启动线程：发送消息到GateSvr
        GamePlayMsgInfo msgInfo = new GamePlayMsgInfo();
        msgInfo.setGameId(gameId);
        msgInfo.setTableId(req.getTableId());
        msgInfo.setReqObj(req);
        msgInfo.setReqNum(header.reqNum);
        msgInfo.setUserIdList(req.getUserIdsList());
        logger.debug("reqNum-> " + header.reqNum + ", gameId->"+gameId+", tableId->"+req.getTableId());

        SendGamePlayMsgThread smThread = new SendGamePlayMsgThread(msgInfo);
        Thread thread = new Thread(smThread);
        //直接移到队列里了 不需要起线程了
        //thread.start();
        thread.run();
        //返回结果给TCP客户端
//        JoloNotice.JoloNotice_SendGamePlayMsgAck.Builder builder = JoloNotice.JoloNotice_SendGamePlayMsgAck.newBuilder();
//        builder.setTableId(req.getTableId());
//        builder.setResult(1);
//        ctx.writeAndFlush(new JoloNotice_SendGamePlayMsgAck_10001(builder.build(), header));
    }
}
