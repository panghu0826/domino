package com.jule.domino.notice.service.thread;

import JoloProtobuf.NoticeSvr.JoloNotice;
import com.jule.domino.notice.model.GamePlayMsgInfo;
import com.jule.domino.notice.service.JedisService;
import com.jule.domino.notice.valve.gate.GateServerGroup;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SendGamePlayMsgThread implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(SendGamePlayMsgThread.class);
    private GamePlayMsgInfo msgInfo = null;

    public SendGamePlayMsgThread(GamePlayMsgInfo _msgInfo) {
        msgInfo = _msgInfo;
    }

    public void run() {
        try {
            //解析reqObj，循环要发送的信息列表
            JoloNotice.JoloNotice_SendGamePlayMsgReq req = msgInfo.getReqObj();

            //获得用户列表

            List<String> uids = new ArrayList<>();
            req.getUserIdsList().forEach(s ->{
                    uids.add(s);
             });

            //TODO：获取到用户连接的GateSvr（hset key= USER_LINK_INFO， 二级key 是玩家id， 值是  ip：port）
            List<String> gateSvrs = JedisService.getInstance().getAllUserInWhichGateSvr2(msgInfo.getGameId() + "", uids);

            if (gateSvrs != null && gateSvrs.size() > 0) {
                for (int i = 0; i < gateSvrs.size(); i++) {
                    String gateSvr = gateSvrs.get(i);
                    if (gateSvr != null) {
                        String uid = uids.get(i);
                        JoloNotice.JoloNotice2Gate_MsgReq.Builder gateReq = JoloNotice.JoloNotice2Gate_MsgReq.newBuilder();
                        gateReq.setUserId(uid);
                        gateReq.setContent(req.getContent());
                        ChannelHandlerContext chc = GateServerGroup.getInstance().getConnect(gateSvr);

                        if (null != chc) {
                            int gateReqLength = gateReq.build().toByteArray().length;
                            int totalLength = 28 + gateReqLength;
                            ByteBuf buffer = chc.alloc().buffer(totalLength);
                            buffer.writeInt(70000);
                            buffer.writeInt(msgInfo.getGameId());
                            buffer.writeInt(0);
                            buffer.writeInt(0);
                            buffer.writeInt(msgInfo.getReqNum());
                            buffer.writeInt(0);
                            buffer.writeInt(0);
                            buffer.writeBytes(gateReq.build().toByteArray());

                            //找到gateSvr对应的连接池并发送消息
                            chc.writeAndFlush(buffer);
                            logger.debug("reqNum-> " + msgInfo.getReqNum() + ", userId->" + uid + ", functionId->" + 70000 + ", totalLength->" + totalLength + ", gateReqLength->" + gateReqLength + ", send data to GateSvr over.");
                        } else {
                            logger.error("ERROR:::Get connect error, gateSvrId -> " + gateSvr);
                        }
                    }

                }
            }
        } catch (Exception ex) {
            logger.error("UpdateGateSvrListThread ERROR, msg = " + ex.getMessage(), ex);
        }
    }
}
