package com.jule.domino.game.service.holder;

import JoloProtobuf.GameSvr.JoloGame;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Deprecated
public class GiftConfigHolder {
    private static Map<String, JoloGame.JoloGame_ItemInfo> GIFT_ITEM_MAP =new HashMap<>(); //所有礼物的配置

    private static class SingletonHolder {
        protected static final GiftConfigHolder instance = new GiftConfigHolder();
    }

    public static final GiftConfigHolder getInstance() {
        return GiftConfigHolder.SingletonHolder.instance;
    }



    /**
     * 获取所有配置
     *
     * @return
     */
    public Collection<JoloGame.JoloGame_ItemInfo> getAllGiftConfig() {
        return GIFT_ITEM_MAP.values();
    }


}
