package com.jule.domino.dispacher.service;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.alibaba.fastjson.JSONObject;
import com.jule.core.common.log.LoggerUtils;
import com.jule.core.jedis.StoredObjManager;
import com.jule.core.network.ChannelHandler;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisChannel;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.config.Config;
import com.jule.domino.dispacher.dao.DBUtil;
import com.jule.domino.dispacher.network.DispacherFunctionFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.internal.StringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理所有链接的玩家链接映射
 */
@Slf4j
public class UserService {
    @Getter
    @Setter
    private static final Map<String, String> PLAYER_MAP = new ConcurrentHashMap();

    private static final Map<String, ChannelHandlerContext> CACHE = new ConcurrentHashMap<>();
    public static final AttributeKey<String> ATTACHED_USER = AttributeKey.valueOf(String.class, "ATTACHED_USER");
    private DualHashBidiMap<Long, ChannelHandlerContext> gateSvrPool;

    public UserService() {
        gateSvrPool = new DualHashBidiMap();
    }

    private static class SingletonHolder {
        protected static final UserService instance = new UserService();
    }

    public static final UserService getInstance() {
        return UserService.SingletonHolder.instance;
    }

    public void init(){
        if (StoredObjManager.setnx(RedisConst.GAME_LOAD_USERS_SWITCH.getProfix(), "1") == 1) {
            List<User> users = DBUtil.selectAllUser();
            users.forEach(user -> {
                StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
            });
            StoredObjManager.deleteExistsObj(RedisConst.GAME_LOAD_USERS_SWITCH.getProfix());
            log.info("all user is loaded");
        }

    }
    /**
     * 登陆
     *
     * @param user
     */
    public boolean onUserLogin(String user, ChannelHandlerContext channelHandlerContext) throws Exception {
        boolean result = true;
        StringBuilder sb = new StringBuilder();
        //sb.delete(0,sb.toString().length());
        if (StringUtil.isNullOrEmpty(user)) {
            log.info("new guest login->" + user);
            return true;
        }
        String tmpUserId = channelHandlerContext.channel().attr(ATTACHED_USER).get();
        if (StringUtils.isNotEmpty(tmpUserId)) {
            log.error("two accounts in one connects");
        }
        channelHandlerContext.channel().attr(ATTACHED_USER).set(user);
        ChannelHandlerContext channelHandlerContext1 = CACHE.get(user);
        sb.append("CACHE.get(" + user + ") is null " + (channelHandlerContext1 == null));
        channelHandlerContext1 = CACHE.put(user, channelHandlerContext);

        long sessionId = ChannelHandler.getSesseionId(channelHandlerContext);
        gateSvrPool.put(sessionId, channelHandlerContext);
        //long sessionId = ChannelHandler.getSesseionId(channelHandlerContext);
        StoredObjManager.hset(RedisConst.LINK_DISPACHER_STAT.getProfix(), RedisConst.LINK_DISPACHER_STAT.getField() + user, "" + sessionId);
        sb.append(",channelHandlerContext1 is null?" + (channelHandlerContext1 == null));
        if (channelHandlerContext1 != null) {//返回指定协议
            gateSvrPool.removeValue(channelHandlerContext);
            sb.append("user twice login and should close this link!");
            log.warn("user twice login and should close this link!");

            String con = "";
            JSONObject js = new JSONObject();
            js.put("userId", user);
            js.put("dispacherSvr", Config.BIND_IP + Config.BIND_PORT);
            con = js.toString();
            StoredObjManager.publish(con,
                    RedisChannel.REPEAT_LOGIN_CHANNEL.getChannelName());

            Req.ReqHeader reqHeader = new Req.ReqHeader(DispacherFunctionFactory.__function__id_600010 | 0x08000000, 0, 0, false, 0);
            DispacherFunctionFactory.getInstance().getResponse(DispacherFunctionFactory.__function__id_600010 | 0x08000000,
                    JoloAuth.JoloCommon_LoginElsewhereAck.newBuilder().setResult(1).build().toByteArray()).send(channelHandlerContext1, reqHeader);
            channelHandlerContext1.disconnect();
            result = false;
        }
        LoggerUtils.linkLog.info(sb.toString());
        log.info("user login->" + user);
        return result;
    }

    public ChannelHandlerContext getChannel(Long sessionId) {
        ChannelHandlerContext channelHandlerContext = null;
        if (gateSvrPool.containsKey(sessionId)) {
            channelHandlerContext = gateSvrPool.get(sessionId);
        }
        return channelHandlerContext;
    }

    public ChannelHandlerContext getChannel(String userId) {
        ChannelHandlerContext channelHandlerContext1 = CACHE.get(userId);
        return channelHandlerContext1;
    }

    /**
     * 登出
     *
     * @param userId
     */
    public boolean onUserBreak(String userId) {
        if (StringUtil.isNullOrEmpty(userId)) {
            log.info("onUserBreak userId -> " + userId);
            return true;
        }
        onPlayerLoutOut(userId);
        //发送登出日志
        sendLog(userId);

        ChannelHandlerContext channelHandlerContext1 = CACHE.get(userId);
        if (channelHandlerContext1 != null) {//返回指定协议
            log.warn("user twice login and should close this link!");
            String sessionId = StoredObjManager.hget(RedisConst.LINK_DISPACHER_STAT.getProfix(), RedisConst.LINK_DISPACHER_STAT.getField() + userId);
            if (!StringUtil.isNullOrEmpty(sessionId)) {
                long sessId = Long.parseLong(sessionId);
                if (sessId == ChannelHandler.getSesseionId(channelHandlerContext1)) {
                    return true;
                }
            }

            Req.ReqHeader reqHeader = new Req.ReqHeader(DispacherFunctionFactory.__function__id_600010 | 0x08000000, 0, 0, false, 0);
            DispacherFunctionFactory.getInstance().getResponse(DispacherFunctionFactory.__function__id_600010 | 0x08000000,
                    JoloAuth.JoloCommon_LoginElsewhereAck.newBuilder().setResult(1).build().toByteArray()).send(channelHandlerContext1, reqHeader);
            channelHandlerContext1.disconnect();
            CACHE.remove(userId);//modify lyb 2018-07-19 移除连接
            gateSvrPool.removeValue(channelHandlerContext1);
            return false;
        }
        return true;
    }

    /**
     * 断线
     *
     * @param channelHandlerContext
     */
    public void onUserBreak(ChannelHandlerContext channelHandlerContext) {
        String userId = channelHandlerContext.channel().attr(ATTACHED_USER).get();

        //发送登出日志
        sendLog(userId);

        if (userId != null) {
            onPlayerLoutOut(userId);
            String sessionId = StoredObjManager.hget(RedisConst.LINK_DISPACHER_STAT.getProfix(), RedisConst.LINK_DISPACHER_STAT.getField() + userId);
            if (!StringUtil.isNullOrEmpty(sessionId)) {
                long sessId = Long.parseLong(sessionId);
                long currentID = ChannelHandler.getSesseionId(channelHandlerContext);
                log.info("sessId:{},currentSessionID:{}", sessId, currentID);
                if (sessId != currentID) {//不等于现在的就不要移除
                    return;
                }
                CACHE.remove(userId);
                gateSvrPool.remove(currentID);
                LoggerUtils.linkLog.info("user logout userId->" + userId);
                log.info("user logout userId->" + userId);
            }
        }
    }

    /**
     * 给客户端发通知消息
     */
    public void sendNewMailNoticeMsg() {
        CACHE.forEach((k, v) -> {
            String userId = k;
            ChannelHandlerContext channelHandlerContext = CACHE.get(userId);
            if (channelHandlerContext != null && channelHandlerContext.channel().isActive()) {
                log.debug("send new mail notice user link ->" + channelHandlerContext.toString());
                byte[] payload = JoloAuth.JoloAuth_MailAck.newBuilder().setUserId(userId).setForced2Refresh(1).build().toByteArray();
                ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
                byteBuf.writeInt(payload.length + 28);
                byteBuf.writeInt(600025 | 0x08000000);
                byteBuf.writeInt(0);
                byteBuf.writeInt(0);
                byteBuf.writeInt(1);
                byteBuf.writeInt(0);
                byteBuf.writeInt(0);
                byteBuf.writeInt(0);
                byteBuf.writeBytes(payload);
                channelHandlerContext.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
            } else {
                log.debug("dispacher send not exist userId->" + userId);
            }
        });


    }

    /**
     * 给客户端发通知消息
     */
    public void sendPayNoticeMsg(String userId, double money) {
        ChannelHandlerContext channelHandlerContext = CACHE.get(userId);
        if (channelHandlerContext != null && channelHandlerContext.channel().isActive()) {
            log.debug("send pay notice user id->" + userId);
            log.debug("send pay notice user link ->" + channelHandlerContext.toString());
            byte[] payload = JoloAuth.JoloAuth_Notice2Client_PayResultReq.newBuilder().setUserId(userId).setMoney(money).build().toByteArray();
            ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
            byteBuf.writeInt(payload.length + 28);
            byteBuf.writeInt(52001);
            byteBuf.writeInt(0);
            byteBuf.writeInt(0);
            byteBuf.writeInt(1);
            byteBuf.writeInt(0);
            byteBuf.writeInt(0);
            byteBuf.writeInt(0);
            byteBuf.writeBytes(payload);
            channelHandlerContext.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
        } else {
            log.debug("dispacher send not exist userId->" + userId);
        }
    }

    /**
     * 给客户端发通知消息
     */
    public void sendPayAddNoticeMsg(String userId, long addmoney) {
        ChannelHandlerContext channelHandlerContext = CACHE.get(userId);
        if (channelHandlerContext != null && channelHandlerContext.channel().isActive()) {
            log.debug("send pay notice user id->" + userId);
            log.debug("send pay notice user link ->" + channelHandlerContext.toString());
            byte[] payload = JoloAuth.JoloAuth_Notice2Client_PayAddReq.newBuilder().setUserId(userId).setAddmoney(addmoney).build().toByteArray();
            ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
            byteBuf.writeInt(payload.length + 28);
            byteBuf.writeInt(52002);
            byteBuf.writeInt(0);
            byteBuf.writeInt(0);
            byteBuf.writeInt(1);
            byteBuf.writeInt(0);
            byteBuf.writeInt(0);
            byteBuf.writeInt(0);
            byteBuf.writeBytes(payload);
            channelHandlerContext.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
        } else {
            log.debug("dispacher send not exist userId->" + userId);
        }
    }

    /**
     * 发送游戏登出日志
     *
     * @param userId
     */
    public void sendLog(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return;
        }
        User _user = DBUtil.selectByPrimaryKey(userId);
        //更新玩家最后登出时间
        DBUtil.updateLastOffline(userId);
        LogService.OBJ.sendUserLogoutLog(_user);
    }

    /**
     * 玩家登陆
     */
    public void onPlayerLogin(String playerId) {
        if (PLAYER_MAP.put(playerId, playerId) != null) {
            log.warn("玩家 " + playerId + "重复登陆");
        }
    }

    /**
     * 玩家登出
     */
    private void onPlayerLoutOut(String playerId) {
        log.info("onPlayerLoutOut userId:{}", playerId);
        PLAYER_MAP.remove(playerId);
        //StoredObjManager.hdel(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + playerId);
    }
}
