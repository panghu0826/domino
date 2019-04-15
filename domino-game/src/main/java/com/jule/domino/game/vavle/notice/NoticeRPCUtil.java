package com.jule.domino.game.vavle.notice;

import JoloProtobuf.NoticeSvr.JoloNotice;
import com.google.protobuf.MessageLite;
import com.jule.domino.game.service.RegisterService;
import com.jule.domino.game.vavle.notice.protocol.MuliMsgReq;
import com.jule.domino.game.vavle.notice.protocol.SingleMsgReq;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 统一封装工具类
 */
public class NoticeRPCUtil {
    private final static Logger logger = LoggerFactory.getLogger(NoticeMsgHandler.class);
    /**
     * 推单个消息
     *
     * @param userId
     * @param content
     */
    public static void sendSingMsg(int gameId,String userId, int functionId, MessageLite content) {
        int length = content.toByteString().toByteArray().length;
        JoloNotice.JoloNotice_PayLoad payLoad = JoloNotice.JoloNotice_PayLoad.newBuilder().setLength(28 + length).setFunctionId(functionId).setGameId(gameId).setGameSvrId(RegisterService.GAME_SERVER_ID).setIsAsync(1).setReqNum((int) System.currentTimeMillis() / 1000).setResver1(0).setResver2(0).setPayLopad(content.toByteString()).build();
        NoticeConnectPool.getInstance().getConnection().writeAndFlush(new SingleMsgReq(gameId,JoloNotice.JoloNotice_SendNormalMsgReq.newBuilder().setUserId(userId).setContent(payLoad.toByteString()).build()));
    }

    /**
     * 通知多个人
     *
     * @param tableId
     * @param userId
     * @param message
     */
    public static void senMuliMsg(int gameId,String tableId, List<String> userId, int functionId, MessageLite message) {
        int length = message.toByteString().toByteArray().length;
        JoloNotice.JoloNotice_PayLoad payLoad = JoloNotice.JoloNotice_PayLoad.newBuilder().setLength(28 + length).setFunctionId(functionId).setGameId(gameId).setGameSvrId(RegisterService.GAME_SERVER_ID).setIsAsync(1).setReqNum((int) System.currentTimeMillis() / 1000).setResver1(0).setResver2(0).setPayLopad(message.toByteString()).build();
        ChannelHandlerContext chc =  NoticeConnectPool.getInstance().getConnection();
        logger.info("senMuliMsg get Connection?"+(chc==null?"No":"Yes"));
        if(chc==null){
            logger.error("senMuliMsg() chc is null");
        }else {
            chc.writeAndFlush(new MuliMsgReq(gameId, JoloNotice.JoloNotice_SendGamePlayMsgReq.newBuilder()
                    .setTableId(tableId)
                    .addAllUserIds(userId)
                    .setContent(payLoad.toByteString())
                    .build()));
        }
    }
}
