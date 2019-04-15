package com.jule.domino.notice.service.thread;

import JoloProtobuf.NoticeSvr.JoloNotice;
import com.google.protobuf.ByteString;
import com.jule.domino.notice.model.NormalMsgInfo;
import com.jule.domino.notice.service.JedisService;
import com.jule.domino.notice.valve.gate.GateServerGroup;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendNormalMsgThread implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(SendNormalMsgThread.class);
    private NormalMsgInfo msgInfo = null;

    public SendNormalMsgThread(NormalMsgInfo _msgInfo){
        msgInfo = _msgInfo;
    }

    public void run() {
        try{
            //TODO：获取到用户连接的GateSvr（hset key= USER_LINK_INFO， 二级key 是玩家id， 值是  ip：port）
            String gateSvr = JedisService.getInstance().getUserInWhichGateSvr(msgInfo.getGameId()+"", msgInfo.getUserId());
            JoloNotice.JoloNotice2Gate_MsgReq.Builder gateReq = JoloNotice.JoloNotice2Gate_MsgReq.newBuilder();
            gateReq.setUserId(msgInfo.getUserId());
            gateReq.setContent(ByteString.copyFrom(msgInfo.getMsgContent()));
            logger.debug("reqNum-> " + 0 + ", gameId->"+msgInfo.getGameId()+", userId->"+msgInfo.getUserId() +", gateSvr -> "+ gateSvr);

            ChannelHandlerContext chc = GateServerGroup.getInstance().getConnect(gateSvr);
            int gateReqLength = gateReq.build().toByteArray().length;
            int totalLength = 28 + gateReqLength;

            if(null != chc){
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

                logger.debug("reqNum-> " + msgInfo.getReqNum() +", userId->"+ msgInfo.getUserId() +", functionId->"+70000+", totalLength->"+ totalLength + ", gateReqLength->"+ gateReqLength +", send data to GateSvr over.");
            }else{
                logger.error("ERROR:::Get connect error, gateSvrId -> "+gateSvr);
            }
        }catch (Exception ex){
            logger.error("SendNormalMsgThread ERROR, msg = "+ex.getMessage(), ex);
        }
    }
}
