package com.jule.domino.game.service.holder;

import com.jule.domino.game.gameUtil.DealCardForTable;

import java.util.HashMap;
import java.util.Map;

/**
 * 牌桌内的手牌数据容器类
 * 描述：存储每个牌局的发牌控制类，保证不同牌局之间不出现发牌冲突的情况
 */
public class CardOfTableHolder {
    /**
     * 键值对：存储牌局对应的手牌数据信息
     */
    private static Map<String, DealCardForTable> cardOperationMap = new HashMap<>();

    public static void PutCardOperationObj(String gameOrderId, DealCardForTable ocObj){
        cardOperationMap.put(gameOrderId, ocObj);
    }

    public static DealCardForTable TakeCardOperationObj(String gameOrderId){
        return cardOperationMap.getOrDefault(gameOrderId, null);
    }

    public static void RemoveCardOperationObj(String gameOrderId){
        cardOperationMap.remove(gameOrderId);
    }
}
