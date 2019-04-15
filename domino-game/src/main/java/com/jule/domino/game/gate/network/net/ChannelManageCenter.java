package com.jule.domino.game.gate.network.net;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.network.ChannelHandler;
import com.jule.domino.game.gate.network.GateFunctionFactory;
import com.jule.domino.game.gate.network.protocol.Req;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Slf4j
public class ChannelManageCenter {

    private static ChannelManageCenter cmcIns;

    /**
     * channel and connectSession mapping
     **/
    private DualHashBidiMap<ChannelHandlerContext, ConnectSession> connectSessionMap;

    /**
     * channel and String mapping
     **/
    private DualHashBidiMap<ChannelHandlerContext, Long> tempCache;

    private ChannelGroup cg;

    private ConcurrentHashMap<String, ConnectSession> sessionPool;

    private DualHashBidiMap<Long, ChannelHandlerContext> gateSvrPool;

    private ScheduledThreadPoolExecutor schedule;

    public static final AttributeKey<ChannelAttach> akey = AttributeKey.valueOf("attach");

    private ChannelManageCenter() {

        connectSessionMap = new DualHashBidiMap();

        tempCache = new DualHashBidiMap();

        cg = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        sessionPool = new ConcurrentHashMap<>();

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
        //System.out.println("addChannel =  ch "+ch.remoteAddress()+"  ,"+name);
        Attribute<ChannelAttach> att = ch.channel().attr(akey);
        if (att != null && att.get() != null) {
            att.get().setActivityTime(System.currentTimeMillis());
        } else {
            ChannelAttach ca = new ChannelAttach(ch, System.currentTimeMillis());
            att.setIfAbsent(ca);
        }
        gateSvrPool.put(name,ch);
        tempCache.put(ch, name);
        return name;
    }

    /***
     * get connectSession by channel
     *
     * @param ch
     * @return
     */
    public ConnectSession getSession(ChannelHandlerContext ch) {
        if (connectSessionMap.containsKey(ch)) {
            ConnectSession session = connectSessionMap.get(ch);
            return session;
        }
        return null;
    }

    /***
     * get connectSession by channel
     *
     * @param ch
     * @return
     */
    public Long getTempID(ChannelHandlerContext ch) {
        if (tempCache.containsKey(ch)) {
            long session = tempCache.get(ch);
            return session;
        }
        return null;
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
    }

    public boolean bind(Long channelUid, String userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("bind");
        if (tempCache.containsValue(channelUid)) {
            sb.append("exist channelUid:"+channelUid);
            ChannelHandlerContext ch = tempCache.removeValue(channelUid);

            if (sessionPool.containsKey(userId)) {
                sb.append("exist userId:"+userId);
                // 改玩家session 已存在。挤用户下线
                ConnectSession session = sessionPool.get(userId);
                ChannelHandlerContext last = session.getChannel();

                if (last != null) {
                    //TODO Tips 别处登陆
                    Req.ReqHeader reqHeader = new Req.ReqHeader(GateFunctionFactory.__function__id_600002 | 0x08000000, 0, 0, false, 0);
                    GateFunctionFactory.getInstance().getResponse(GateFunctionFactory.__function__id_600002 | 0x08000000, JoloAuth.JoloCommon_LoginElsewhereAck.newBuilder()
                            .setResult(1)
                            .build().toByteArray()).send(last, reqHeader, true);

                    session.setChannel(ch);
                    session.setSesseionId(channelUid);
                    cg.remove(last);
                    cg.add(ch.channel());
                    gateSvrPool.removeValue(last);
                    connectSessionMap.remove(last);
                    connectSessionMap.put(ch, session);

                    last.disconnect();
                }
                log.info(sb.toString());
                return true;

            } else {
                sb.append("add channelUid:"+channelUid+"userID:"+ userId);
                cg.add(ch.channel());

                ConnectSession session = new ConnectSession(channelUid, userId).setChannel(ch);

                connectSessionMap.put(ch, session);

                sessionPool.put(userId, session);
                log.info(sb.toString());
                return true;
            }
        } else {
            log.info(sb.toString());
            return false;
        }
    }

    public void sub(String userId) {
        log.info("remove link userId:"+userId);
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

                cg.remove(last);
                gateSvrPool.removeValue(last);
                connectSessionMap.remove(last);
                last.disconnect();
            }
        }
    }

    public void clearSession(long userId) {
        ConnectSession session = cmcIns.sessionPool.remove(userId);
        cmcIns.connectSessionMap.removeValue(session);
    }

    /*
    public ChannelHandlerContext getChannel(Long sessionId) {
        StringBuilder sb = new StringBuilder();
        ChannelHandlerContext channelHandlerContext = null;
        sb.append("getChannel");
        if (tempCache.containsValue(sessionId)) {
            Object Object = tempCache.getKey(sessionId);
            sb.append("success");
            channelHandlerContext = (ChannelHandlerContext) Object;
        }
        log.info(sb.toString());
        return channelHandlerContext;
    }*/

    public ChannelHandlerContext getChannel(Long sessionId) {
        StringBuilder sb = new StringBuilder();
        ChannelHandlerContext channelHandlerContext = null;
        sb.append("getChannel");
        if (gateSvrPool.containsKey(sessionId)) {
            Object Object = gateSvrPool.get(sessionId);
            sb.append("success");
            channelHandlerContext = (ChannelHandlerContext) Object;
        }
        log.info(sb.toString());
        return channelHandlerContext;
    }

    public ConnectSession getSession(long uid) {
        return cmcIns.sessionPool.get(uid);
    }

    public ChannelGroup getAllChannels() {
        return cg;

    }
}
