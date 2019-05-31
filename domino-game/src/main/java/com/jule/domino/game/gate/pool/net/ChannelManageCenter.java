package com.jule.domino.game.gate.pool.net;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.network.ChannelHandler;
import com.jule.domino.game.gate.network.GateFunctionFactory;
import com.jule.domino.game.gate.network.protocol.Req;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Slf4j
public class ChannelManageCenter {

    private static ChannelManageCenter cmcIns;

    /**
     * channel and String mapping
     **/
    private DualHashBidiMap<ChannelHandlerContext, Long> tempCache;

    private ConcurrentHashMap<String, ConnectSession> sessionPool;

    private ConcurrentHashMap<Long, String> sessionPoolCache;

    private DualHashBidiMap<Long, ChannelHandlerContext> gateSvrPool;

    private ScheduledThreadPoolExecutor schedule;

    public static final AttributeKey<ChannelAttach> akey = AttributeKey.valueOf("attach");

    private ChannelManageCenter() {

        tempCache = new DualHashBidiMap();

        sessionPool = new ConcurrentHashMap<>();

        sessionPoolCache = new ConcurrentHashMap<>();

        gateSvrPool = new DualHashBidiMap();

        schedule = new ScheduledThreadPoolExecutor(1);

        //checkIllegalChannel();
    }

    /***
     * get channel manager center instance
     *
     * @return
     */
    public static ChannelManageCenter getInstance() {
        if (cmcIns == null) {
            cmcIns = new ChannelManageCenter();
        }
        return cmcIns;
    }


    /***
     * 添加信道到临时缓存
     *
     * @param ch
     */
    public Long addChannel(ChannelHandlerContext ch) {
        long name = ChannelHandler.getSesseionId(ch);

        Attribute<ChannelAttach> att = ch.channel().attr(akey);
        if (att != null && att.get() != null) {
            att.get().setActivityTime(System.currentTimeMillis());
        } else {
            ChannelAttach ca = new ChannelAttach(ch, System.currentTimeMillis());
            att.setIfAbsent(ca);
        }
        gateSvrPool.put(name, ch);
        tempCache.put(ch, name);
        return name;
    }

    /***
     * remove the cached channel
     *
     * @param ch
     */
    public void removeTempSession(ChannelHandlerContext ch) {
        if (tempCache.containsKey(ch)) {
            //System.out.println("removeTempSession - "+ch.remoteAddress());
            tempCache.remove(ch);
        }
        if (gateSvrPool.containsValue(ch)) {
            gateSvrPool.removeValue(ch);
        }

        long sessionId = ChannelHandler.getSesseionId(ch);
        sessionPool.values().forEach(
                (session)->{
                    if(ChannelHandler.getSesseionId(session.getChannel())== sessionId){
                        sessionPool.remove(session);
                        log.info("removeTempSession() success useId:"+session.getUserId());
                    }
                }
        );
        if (sessionPoolCache.containsKey(sessionId)) {
            sessionPoolCache.remove(sessionId);
        }
    }

    public boolean bind(Long channelUid, String userId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("bind");
            if (tempCache.containsValue(channelUid)) {
                sb.append("exist channelUid:" + channelUid);
                ChannelHandlerContext ch = tempCache.removeValue(channelUid);

                if (sessionPool.containsKey(userId)) {
                    sb.append("exist userId:" + userId);
                    // 改玩家session 已存在。挤用户下线
                    ConnectSession session = sessionPool.get(userId);
                    ChannelHandlerContext last = session.getChannel();

                    if (last != null) {
                        //TODO Tips 别处登陆
                        Req.ReqHeader reqHeader = new Req.ReqHeader(GateFunctionFactory.__function__id_600002 | 0x08000000, 0, 0, false, 0);
                        GateFunctionFactory.getInstance().getResponse(GateFunctionFactory.__function__id_600002 | 0x08000000, JoloAuth.JoloCommon_LoginElsewhereAck.newBuilder().setResult(1).build().toByteArray()).send(last, reqHeader, true);

                        session.setChannel(ch);
                        session.setSesseionId(channelUid);
                        gateSvrPool.removeValue(last);

                        last.disconnect();
                    }
                    log.info(sb.toString());
                    return true;

                } else {
                    sb.append("add channelUid:" + channelUid + "userID:" + userId);

                    ConnectSession session = new ConnectSession(channelUid, userId).setChannel(ch);

                    sessionPool.put(userId, session);
                    log.info(sb.toString());
                    return true;
                }
            } else {
                log.info(sb.toString());
                return false;
            }
        }catch (Exception ex){
            log.error("",ex);
        }finally {
            ConnectSession session = sessionPool.get(userId);
            ChannelHandlerContext last = session.getChannel();
            sessionPoolCache.put(ChannelHandler.getSesseionId(last), userId);
        }
        return false;
    }

    public void sub(String userId) {
        log.info("remove link userId:" + userId);
        if (sessionPool.containsKey(userId)) {
            // 改玩家session 已存在。挤用户下线
            ConnectSession session = sessionPool.get(userId);
            ChannelHandlerContext last = session.getChannel();

            if (last != null) {
                //TODO Tips 别处登陆
                Req.ReqHeader reqHeader = new Req.ReqHeader(60002 | 0x08000000, 0, 0, false, 0);
                GateFunctionFactory.getInstance().getResponse(60002 | 0x08000000, JoloAuth.JoloCommon_LoginElsewhereAck.newBuilder()
                        .setResult(1)
                        .build().toByteArray()).send(last, reqHeader, true);

                gateSvrPool.removeValue(last);
                tempCache.remove(last);
                last.disconnect();
            }
        }
    }


    public ChannelHandlerContext getChannel(Long sessionId) {
        ChannelHandlerContext channelHandlerContext = null;
        if (gateSvrPool.containsKey(sessionId)) {
            channelHandlerContext = gateSvrPool.get(sessionId);
        }else {
            log.error("找不到该session -> sessionId：{}",sessionId);
        }
        return channelHandlerContext;
    }

    public ConnectSession getSession(long uid) {
        return cmcIns.sessionPool.get(uid);
    }

    public String getSessionUID(Long sessionId){
        return this.sessionPoolCache.get(sessionId);
    }
}
