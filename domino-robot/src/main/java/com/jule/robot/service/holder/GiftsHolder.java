package com.jule.robot.service.holder;

import JoloProtobuf.GameSvr.JoloGame;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GiftsHolder {
    /**
     * 礼物列表
     * key：gameId_roomId
     */
    public static ConcurrentMap<String, List<JoloGame.JoloGame_ItemInfo>> giftsMap = new ConcurrentHashMap<>();
}
