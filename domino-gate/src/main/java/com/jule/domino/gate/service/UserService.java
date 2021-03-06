package com.jule.domino.gate.service;

import JoloProtobuf.NoticeSvr.JoloNotice;
import com.jule.core.jedis.StoredObjManager;
import com.jule.core.network.ChannelHandler;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.gate.vavle.game.GameServerGroup;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理所有链接的玩家链接映射
 */
public class UserService {
    private static final Map<String, ChannelHandlerContext> CACHE = new ConcurrentHashMap<>();
    public static final AttributeKey<String> ATTACHED_USER = AttributeKey.valueOf(String.class, "ATTACHED_USER");
    public static final AttributeKey<Integer> LAST_GAMESVR_ID = AttributeKey.valueOf("LAST_GAMESVR_ID");
    private final static Logger logger = LoggerFactory.getLogger(UserService.class);

    private static class SingletonHolder {
        protected static final UserService instance = new UserService();
    }

    public static final UserService getInstance() {
        return UserService.SingletonHolder.instance;
    }

    /**
     * 登陆
     *
     * @param user
     */
    public boolean onUserLogin(String user, ChannelHandlerContext channelHandlerContext) {
        if (user.isEmpty()) {
            logger.debug("user id is null!");
            return true;
        }
        channelHandlerContext.channel().attr(ATTACHED_USER).set(user);
        if (CACHE.put(user, channelHandlerContext) != null) {
            logger.warn("user twice login");
            return false;
        }
        logger.info("user login id ->" + user);
        return true;
    }

    /**
     * 断线
     *
     * @param channelHandlerContext
     */
    public void onUserBreak(ChannelHandlerContext channelHandlerContext, boolean isExceptionOffline) {
        //如果和缓存里的数据对应不上就不给游戏模块发断线信息
        String user = channelHandlerContext.channel().attr(ATTACHED_USER).get();
        logger.info("onUserBreak(), user->{}, isExceptionOffline->{}", user, isExceptionOffline);
        if (!StringUtil.isNullOrEmpty(user)) {
            String sessionId = StoredObjManager.hget(RedisConst.LINK_GATE_STAT.getProfix(), RedisConst.LINK_GATE_STAT.getField() + user);
            if (!StringUtil.isNullOrEmpty(sessionId)) {
                long sessId = Long.parseLong(sessionId);
                if (sessId != ChannelHandler.getSesseionId(channelHandlerContext)) {
                    logger.info("onUserBreak(),sessIdError function return , user->{}, isExceptionOffline->{},sessId->{},ChannelSessId->{}"
                            , user, isExceptionOffline, sessId, ChannelHandler.getSesseionId(channelHandlerContext));
                    return;
                }
            }

            CACHE.remove(user);
            Integer lastGameSvrId = channelHandlerContext.channel().attr(LAST_GAMESVR_ID).get();
            logger.debug("offline gameSvrId->" + lastGameSvrId + ",userId->" + user + ", isExceptionOffline:" + isExceptionOffline);
            if (lastGameSvrId != null) {// && lastGameSvrId > 0) {
                //通知游戏服务器玩家推出或者说是掉线
                logger.info("user logout usrId->" + user + ", last GameServerId->" + lastGameSvrId + ", isExceptionOffline:" + isExceptionOffline);
                ChannelHandlerContext connect = GameServerGroup.getInstance().getConnect(lastGameSvrId.intValue());
                if (connect != null) {
                    //发一个gate-game之间的通信消息，现在和game转发用一个连接池，todo 专用的单个连接？
                    ByteBuf byteBuf = connect.alloc().buffer(28);
                    byteBuf.writeInt(2);//消息id 1 心跳 2 通知玩家下线
                    byteBuf.writeInt(0);
                    byteBuf.writeInt(0);
                    byteBuf.writeInt(0);
                    byteBuf.writeInt(0);
                    byteBuf.writeLong(Long.parseLong(user));
                    connect.writeAndFlush(byteBuf);
                } else {
                    logger.info("ChannelHandlerContext is null, userId->" + user + ", isExceptionOffline:" + isExceptionOffline);
                }
            }
            logger.info("user logout userId->" + user + ", isExceptionOffline:" + isExceptionOffline);
        }
    }

    /**
     * 给客户端发通知消息
     */
    public void sendNoticeMsg(String userId, JoloNotice.JoloNotice_PayLoad payLoad) {
        try {
            ChannelHandlerContext channelHandlerContext = CACHE.get(userId);
            if (channelHandlerContext != null && channelHandlerContext.channel().isActive()) {
                logger.debug("send user id->" + userId);
                logger.debug("send user link ->" + channelHandlerContext.toString());
                logger.debug("send getFunctionId-> " + payLoad.getFunctionId());
                logger.debug("send getGameId-> " + payLoad.getGameId());
                logger.debug("send getGameSvrId-> " + payLoad.getGameSvrId());
                logger.debug("send getIsAsync-> " + payLoad.getIsAsync());
                logger.debug("send getResver1-> " + payLoad.getResver1());
                logger.debug("send getResver2-> " + payLoad.getResver2());
                ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
                byteBuf.writeInt(payLoad.getPayLopad().toByteArray().length + 28);
                byteBuf.writeInt(payLoad.getFunctionId());
                byteBuf.writeInt(payLoad.getGameId());
                byteBuf.writeInt(payLoad.getGameSvrId());
                byteBuf.writeInt(payLoad.getIsAsync());
                byteBuf.writeInt(payLoad.getReqNum());
                byteBuf.writeInt(payLoad.getResver1());
                byteBuf.writeInt(payLoad.getResver2());
                byteBuf.writeBytes(payLoad.getPayLopad().toByteArray());

                channelHandlerContext.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
            } else {
                logger.debug("gate send not exist userId->" + userId);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
        }
    }
}
