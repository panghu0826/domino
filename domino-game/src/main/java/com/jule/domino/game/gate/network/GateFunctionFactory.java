package com.jule.domino.game.gate.network;


import com.jule.domino.game.gate.network.process.acks.*;
import com.jule.domino.game.gate.network.process.reqs.*;
import com.jule.domino.game.gate.network.protocol.Ack;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.gate.service.RouteService;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xujian on 2017/5/16 0016.
 */
public class GateFunctionFactory {
    private final static Logger logger = LoggerFactory.getLogger(GateFunctionFactory.class);
    public static final int __function__id_60000 = 60000;//心跳
    public static final int __function__id_4 = 4;//房间消息代理通用类
    public static final int __function__id_5 = 5;//游戏消息代理通用类
    public static final int __function__id_8 = 8;//聊天消息代理通用类

    public static final int __function__id_600001 = 600001;//登陆
    public static final int __function__id_600005 = 600005;//更换头像
    public static final int __function__id_600002 = 600010;//登陆重复
    public static final int __function__id_600011 = 600011;//reconnect登陆
    public static final int __function__id_600026 = 600026;//通知socket关闭

    public static final int __function__id_80003 = 80003;//发送/领取房卡
    public static final int __function__id_80007 = 80007;//牌局记录
    public static final int __function__id_80005 = 80005;//房卡发出/领取记录
    public static final int __function__id_80008 = 80008;//好友请求
    public static final int __function__id_80009 = 80009;//创建桌子记录
    public static final int __function__id_80010 = 80010;//购买、发送和领取道具
    public static final int __function__id_80011 = 80011;//道具发送和领取记录


    private static final Map<Integer, Req> packetsPrototypesC = new HashMap<>();
    private static final Map<Integer, Ack> packetsPrototypesS = new HashMap<>();

    public GateFunctionFactory() {
        packetsPrototypesC.put(__function__id_60000, new PingReq_60000(__function__id_60000));
        packetsPrototypesS.put(__function__id_60000 | 0x08000000, new PongAck_60000(__function__id_60000 | 0x08000000));

        packetsPrototypesC.put(__function__id_4, new RoomProxyReq(__function__id_4));
        packetsPrototypesS.put(__function__id_4 | 0x08000000, new RoomProxyAck(__function__id_4 | 0x08000000));

        packetsPrototypesC.put(__function__id_5, new GameProxyReq(__function__id_5));
        packetsPrototypesS.put(__function__id_5 | 0x08000000, new GameProxyAck(__function__id_5 | 0x08000000));

        packetsPrototypesC.put(__function__id_8, new GameProxyReq(__function__id_8));
        packetsPrototypesS.put(__function__id_8 | 0x08000000, new GameProxyAck(__function__id_8 | 0x08000000));

        packetsPrototypesC.put(__function__id_600001, new JoloCommon_LoginReq_600001(__function__id_600001));
        packetsPrototypesS.put(__function__id_600001 | 0x08000000, new JoloCommon_LoginAck_600001(__function__id_600001 | 0x08000000));
        packetsPrototypesS.put(__function__id_600002 | 0x08000000, new JoloCommon_LoginElsewhereAck_600010(__function__id_600002 | 0x08000000));
        packetsPrototypesS.put(__function__id_600011 | 0x08000000, new JoloCommon_InTableAck_600011(__function__id_600011 | 0x08000000));

        packetsPrototypesC.put(__function__id_600026, new JoloCommon_NoticeCloseReq_600026(__function__id_600026));
        packetsPrototypesS.put(__function__id_600026 | 0x08000000, new JoloCommon_NoticeCloseAck_600026(__function__id_600026 | 0x08000000));

        packetsPrototypesC.put(__function__id_600005, new JoloAuth_ChangeIcoReq_600005(__function__id_600005));
        packetsPrototypesS.put(__function__id_600005 | 0x08000000, new JoloAuth_ChangeIcoAck_600005(__function__id_600005 | 0x08000000));

        packetsPrototypesC.put(__function__id_80007, new JoloCommon_PlayRecordsReq_80007(__function__id_80007));
        packetsPrototypesS.put(__function__id_80007 | 0x08000000, new JoloCommon_PlayRecordsAck_80007(__function__id_80007 | 0x08000000));

        packetsPrototypesC.put(__function__id_80003, new JoloCommon_AdInfoReq_80003(__function__id_80003));
        packetsPrototypesS.put(__function__id_80003 | 0x08000000, new JoloCommon_AdInfoAck_80003(__function__id_80003 | 0x08000000));

        packetsPrototypesC.put(__function__id_80005, new JoloCommon_RoomCardRecordsReq_80005(__function__id_80005));
        packetsPrototypesS.put(__function__id_80005 | 0x08000000, new JoloCommon_RoomCardRecordsAck_80005(__function__id_80005 | 0x08000000));

        packetsPrototypesC.put(__function__id_80008, new JoloCommon_FriendReq_80008(__function__id_80008));
        packetsPrototypesS.put(__function__id_80008 | 0x08000000, new JoloCommon_FriendAck_80008(__function__id_80008 | 0x08000000));

        packetsPrototypesC.put(__function__id_80009, new JoloAuth_CreateTableRecordsReq_80009(__function__id_80009));
        packetsPrototypesS.put(__function__id_80009 | 0x08000000, new JoloAuth_CreateTableRecordsAck_80009(__function__id_80009 | 0x08000000));

        packetsPrototypesC.put(__function__id_80010, new JoloCommon_SendAndReceiveItemReq_80010(__function__id_80010));
        packetsPrototypesS.put(__function__id_80010 | 0x08000000, new JoloCommon_SendAndReceiveItemAck_80010(__function__id_80010 | 0x08000000));

        packetsPrototypesC.put(__function__id_80011, new JoloCommon_ItemRecordsReq_80011(__function__id_80011));
        packetsPrototypesS.put(__function__id_80011 | 0x08000000, new JoloCommon_ItemRecordsAck_80011(__function__id_80011 | 0x08000000));

    }

    @SuppressWarnings("synthetic-access")
    private static class SingletonHolder {
        protected static final GateFunctionFactory instance = new GateFunctionFactory();
    }

    public static final GateFunctionFactory getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * @param header
     * @param ctx
     * @return
     */
    public Req getRequest(final Req.ReqHeader header, final ChannelHandlerContext ctx) {
        Req request = packetsPrototypesC.get(header.functionId);
        if (request != null) {
            Req _request = request.clone();
            _request.setCtx(ctx);
            _request.setReqHeader(header);
            return _request;
        }

        logger.debug("是否是room的协议："+header.functionId);
        if (RouteService.getInstance().isRoomMessage(header.functionId)) {
            Req requestProxy = packetsPrototypesC.get(header.functionId / 10000);
            if (requestProxy != null) {
                logger.debug("准备转发到room");
                Req _request2 = requestProxy.clone();
                _request2.setCtx(ctx);
                _request2.setReqHeader(header);
                _request2.setFunctionId(header.functionId);
                return _request2;
            }
        }

        logger.debug("是否是game的协议："+header.functionId);
        if (RouteService.getInstance().isGameMessage(header.functionId)) {
            Req requestProxy = packetsPrototypesC.get(header.functionId / 10000);
            if (requestProxy != null) {
                Req _request2 = requestProxy.clone();
                _request2.setCtx(ctx);
                _request2.setReqHeader(header);
                _request2.setFunctionId(header.functionId);
                return _request2;
            }
        }
        //底层实际丢到game服务器
        if (RouteService.getInstance().isChatMessage(header.functionId)) {
            Req requestProxy = packetsPrototypesC.get(header.functionId / 10000);
            if (requestProxy != null) {
                Req _request2 = requestProxy.clone();
                _request2.setCtx(ctx);
                _request2.setReqHeader(header);
                _request2.setFunctionId(header.functionId);
                return _request2;
            }
        }

        logger.warn(String.format("received unknow msg function_id -> %02X %d", header.functionId, header.functionId));
        return null;
    }

    /**
     * @param functionId
     * @param payLoad
     * @return
     */
    public Ack getResponse(final int functionId, byte[] payLoad) {
        Ack response = packetsPrototypesS.get(functionId);
        if (response != null) {
            Ack _response1 = response.clone();
            _response1.setBuf(payLoad);
            return _response1;
        }
        logger.warn(String.format("received ack msg function_id -> %02X %d",functionId, functionId));
        return null;
    }
}
