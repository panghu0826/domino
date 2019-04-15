package com.jule.domino.game.notice.service;

/**
 * @Author xujian
 * 转发路由服务
 * NoticeSvr  消息号范围10000 – 19999
 * AuthSvr    消息号范围 20000 – 29999
 * AvataSvr    消息号范围 30000 – 39999
 * RoomSvr    消息号范围 40000 – 49999
 * GameSvr    消息号范围 50000 – 59999
 */
public class RouteService {
    private static class SingletonHolder {
        protected static final RouteService instance = new RouteService();
    }

    public static final RouteService getInstance() {
        return RouteService.SingletonHolder.instance;
    }

    /**
     * 是否是game server协议
     *
     * @param opCode
     * @return
     */
    public boolean isGameMessage(int opCode) {
        return opCode >= 50000 && opCode <= 59999;
    }

    /**
     * 是否是room server协议
     *
     * @param opCode
     * @return
     */
    public boolean isRoomMessage(int opCode) {
        return opCode >= 40000 && opCode <= 49999;
    }
}
