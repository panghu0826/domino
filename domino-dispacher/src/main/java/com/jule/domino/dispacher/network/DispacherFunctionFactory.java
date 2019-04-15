package com.jule.domino.dispacher.network;
import com.jule.domino.dispacher.network.process.*;
import com.jule.domino.dispacher.network.protocol.Ack;
import com.jule.domino.dispacher.network.protocol.Req;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xujian on 2017/5/16 0016.
 */
public class DispacherFunctionFactory {
    private final static Logger logger = LoggerFactory.getLogger(DispacherFunctionFactory.class);
    public static final int __function__id_60000 = 60000;//心跳
    public static final int __function__id_600001 = 600001;//登陆
    public static final int __function__id_600002 = 600002;//商城列表
    public static final int __function__id_600003 = 600003;//商城下单
    public static final int __function__id_600004 = 600004;//google支付履行下单
    public static final int __function__id_600005 = 600005;//更换玩家ico
    public static final int __function__id_600006 = 600006;//是否需要版本更新
    public static final int __function__id_600007 = 600007;//返回购买礼物、荷官、头像状态
    public static final int __function__id_600008 = 600008;//返回头像列表
    public static final int __function__id_600009 = 600009;//返回头像状态
    public static final int __function__id_600010 = 600010;//在别处登录
    public static final int __function__id_600011 = 600011;//是否在桌内
    public static final int __function__id_600012 = 600012;//返回解锁头像状态

    public static final int __function__id_600020 = 20;//请求邮件红点
    public static final int __function__id_600021 = 21;//邮件列表
    public static final int __function__id_600022 = 22;//邮件查看
    public static final int __function__id_600023 = 23;//邮件领取附件
    public static final int __function__id_600024 = 24;//邮件删除

    public static final int __function__id_90001 = 90001;//google支付履行下单
    public static final int __function__id_90002 = 90002;//google支付履行下单
    public static final int __function__id_90003 = 90003;//google支付履行下单

    public static final int __function__id_80000 = 80000;//在线参数返回
    public static final int __function__id_80003 = 80003;//广告
    public static final int __function__id_80004 = 80004;//广告

    public static final int __function__id_80005 = 80005;//房间列表
    public static final int __function__id_80007 = 80007;//记录

    private static final Map<Integer, Req> packetsPrototypesC = new HashMap<>();
    private static final Map<Integer, Ack> packetsPrototypesS = new HashMap<>();

    public DispacherFunctionFactory() {
        packetsPrototypesC.put(__function__id_60000, new PingReq_60000(__function__id_60000));
        packetsPrototypesS.put(__function__id_60000 | 0x08000000, new PongAck_60000(__function__id_60000 | 0x08000000));
        packetsPrototypesC.put(__function__id_600001, new JoloCommon_LoginReq_600001(__function__id_600001));
        packetsPrototypesS.put(__function__id_600001 | 0x08000000, new JoloCommon_LoginAck_600001(__function__id_600001 | 0x08000000));
        packetsPrototypesC.put(__function__id_600002, new JoloCommon_ShopReq_600002(__function__id_600002));
        packetsPrototypesS.put(__function__id_600002 | 0x08000000, new JoloCommon_ShopAck_600002(__function__id_600002 | 0x08000000));
        packetsPrototypesC.put(__function__id_600003, new JoloCommon_OrderReq_600003(__function__id_600003));
        packetsPrototypesS.put(__function__id_600003 | 0x08000000, new JoloCommon_OrderAck_600003(__function__id_600003 | 0x08000000));
        packetsPrototypesC.put(__function__id_600004, new JoloCommon_GoogleVerifyReq_600004(__function__id_600004));
        packetsPrototypesS.put(__function__id_600004 | 0x08000000, new JoloCommon_GoogleVerifyAck_600004(__function__id_600004 | 0x08000000));
        packetsPrototypesC.put(__function__id_600005, new JoloAuth_ChangeIcoReq_600005(__function__id_600005));
        packetsPrototypesS.put(__function__id_600005 | 0x08000000, new JoloAuth_ChangeIcoAck_600005(__function__id_600005 | 0x08000000));
        packetsPrototypesS.put(__function__id_600006 | 0x08000000, new JoloAuth_VersionAck_600006(__function__id_600006 | 0x08000000));
        packetsPrototypesS.put(__function__id_600007 | 0x08000000, new JoloAuth_ItemStatusAck_600007(__function__id_600007 | 0x08000000));
        packetsPrototypesC.put(__function__id_600008, new JoloAuth_HeadListReq_600008(__function__id_600008));
        packetsPrototypesS.put(__function__id_600008 | 0x08000000, new JoloAuth_HeadListAck_600008(__function__id_600008 | 0x08000000));
        packetsPrototypesC.put(__function__id_600009, new JoloAuth_ItemsStatusReq_600009(__function__id_600009));
        packetsPrototypesS.put(__function__id_600009 | 0x08000000, new JoloAuth_ItemsStatusAck_600009(__function__id_600009 | 0x08000000));
        packetsPrototypesS.put(__function__id_600010 | 0x08000000, new JoloCommon_LoginElsewhereAck_600010(__function__id_600010 | 0x08000000));
        packetsPrototypesS.put(__function__id_600011 | 0x08000000, new JoloCommon_InTableAck_600011(__function__id_600011 | 0x08000000));
        packetsPrototypesC.put(__function__id_600012, new JoloAuth_UnlockReq_600012(__function__id_600012));
        packetsPrototypesS.put(__function__id_600012 | 0x08000000, new JoloAuth_UnlockAck_600012(__function__id_600012 | 0x08000000));

        packetsPrototypesC.put(__function__id_600020, new MailProxyReq(__function__id_600020));
        packetsPrototypesS.put(__function__id_600020 | 0x08000000, new MailProxyAck(__function__id_600020 | 0x08000000));
        packetsPrototypesC.put(__function__id_600021, new MailProxyReq(__function__id_600021));
        packetsPrototypesS.put(__function__id_600021 | 0x08000000, new MailProxyAck(__function__id_600021 | 0x08000000));
        packetsPrototypesC.put(__function__id_600022, new MailProxyReq(__function__id_600022));
        packetsPrototypesS.put(__function__id_600022 | 0x08000000, new MailProxyAck(__function__id_600022 | 0x08000000));
        packetsPrototypesC.put(__function__id_600023, new MailProxyReq(__function__id_600023));
        packetsPrototypesS.put(__function__id_600023 | 0x08000000, new MailProxyAck(__function__id_600023 | 0x08000000));
        packetsPrototypesC.put(__function__id_600024, new MailProxyReq(__function__id_600024));
        packetsPrototypesS.put(__function__id_600024 | 0x08000000, new MailProxyAck(__function__id_600024 | 0x08000000));

        packetsPrototypesC.put(__function__id_90001, new JoloAuth_PlayerCheckInReq_90001(__function__id_90001));
        packetsPrototypesS.put(__function__id_90001 | 0x08000000, new JoloAuth_PlayerCheckInAck_90001(__function__id_90001 | 0x08000000));
        packetsPrototypesC.put(__function__id_90002, new JoloAuth_PlayerSeeCheckInReq_90002(__function__id_90002));
        packetsPrototypesS.put(__function__id_90002 | 0x08000000, new JoloAuth_PlayerSeeCheckInAck_90002(__function__id_90002 | 0x08000000));
        packetsPrototypesC.put(__function__id_90003, new JoloAuth_SignReachReq_90003(__function__id_90003));
        packetsPrototypesS.put(__function__id_90003 | 0x08000000, new JoloAuth_SignReachAck_90003(__function__id_90003 | 0x08000000));

        packetsPrototypesC.put(__function__id_80000, new JoloCommon_onlineParamsReq_80000(__function__id_80000));
        packetsPrototypesS.put(__function__id_80000 | 0x08000000, new JoloCommon_onlineParamsAck_80000(__function__id_80000 | 0x08000000));

        packetsPrototypesC.put(__function__id_80003, new JoloCommon_AdInfoReq_80001(__function__id_80003));
        packetsPrototypesS.put(__function__id_80003 | 0x08000000, new JoloCommon_AdInfoAck_80001(__function__id_80003 | 0x08000000));
        packetsPrototypesC.put(__function__id_80004, new JoloCommon_AdCountReq_80004(__function__id_80004));
        packetsPrototypesS.put(__function__id_80004 | 0x08000000, new JoloCommon_AdCountAck_80004(__function__id_80004 | 0x08000000));

        packetsPrototypesC.put(__function__id_80005, new JoloRoom_RoomListReq_80005(__function__id_80005));
        packetsPrototypesS.put(__function__id_80005 | 0x08000000, new JoloRoom_RoomListAck_80005(__function__id_80005 | 0x08000000));

        packetsPrototypesC.put(__function__id_80007, new JoloAuth_GameRecordsReq_80007(__function__id_80007));
        packetsPrototypesS.put(__function__id_80007 | 0x08000000, new JoloAuth_GameRecordsAck_80007(__function__id_80007 | 0x08000000));

    }

    @SuppressWarnings("synthetic-access")
    private static class SingletonHolder {
        protected static final DispacherFunctionFactory instance = new DispacherFunctionFactory();
    }

    public static final DispacherFunctionFactory getInstance() {
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

        logger.debug("是否是mail的协议：" + header.functionId);
        if (RouteService.getInstance().isMailMessage(header.functionId)) {
            Req requestProxy = packetsPrototypesC.get(header.functionId % 600000);
            if (requestProxy != null) {
                logger.debug("准备转发到mail");
                Req _request2 = requestProxy.clone();
                _request2.setCtx(ctx);
                _request2.setReqHeader(header);
                _request2.setFunctionId(header.functionId);
                return _request2;
            }
        }

        logger.warn(String.format("received unknow function_id -> 0x%02X", header.functionId));
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
        return null;
    }
}
