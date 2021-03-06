package com.jule.domino.game.service.holder;

import com.jule.domino.game.model.CardValueModel;

import java.util.HashMap;
import java.util.Map;

/**
 * 牌对应值 的控制类
 */
public class CardValueHolder {
    private final static Map<Integer, CardValueModel> cardValueMap = new HashMap();

    static{
        //黑桃
        cardValueMap.put(1,  new CardValueModel('♠', '8'));
        cardValueMap.put(2,  new CardValueModel('♠', '9'));
        cardValueMap.put(3,  new CardValueModel('♠', 'T'));
        cardValueMap.put(4,  new CardValueModel('♠', 'J'));
        cardValueMap.put(5,  new CardValueModel('♠', 'Q'));
        cardValueMap.put(6,  new CardValueModel('♠', 'K'));
        cardValueMap.put(7,  new CardValueModel('♠', 'A'));

        //红桃
        cardValueMap.put(8,  new CardValueModel('♥', '8'));
        cardValueMap.put(9,  new CardValueModel('♥', '9'));
        cardValueMap.put(10, new CardValueModel('♥', 'T'));
        cardValueMap.put(11, new CardValueModel('♥', 'J'));
        cardValueMap.put(12, new CardValueModel('♥', 'Q'));
        cardValueMap.put(13, new CardValueModel('♥', 'K'));
        cardValueMap.put(14, new CardValueModel('♥', 'A'));

        //梅花
        cardValueMap.put(15, new CardValueModel('♣', '8'));
        cardValueMap.put(16, new CardValueModel('♣', '9'));
        cardValueMap.put(17, new CardValueModel('♣', 'T'));
        cardValueMap.put(18, new CardValueModel('♣', 'J'));
        cardValueMap.put(19, new CardValueModel('♣', 'Q'));
        cardValueMap.put(20, new CardValueModel('♣', 'K'));
        cardValueMap.put(21, new CardValueModel('♣', 'A'));

        //方块
        cardValueMap.put(22, new CardValueModel('♦', '8'));
        cardValueMap.put(23, new CardValueModel('♦', '9'));
        cardValueMap.put(24, new CardValueModel('♦', 'T'));
        cardValueMap.put(25, new CardValueModel('♦', 'J'));
        cardValueMap.put(26, new CardValueModel('♦', 'Q'));
        cardValueMap.put(27, new CardValueModel('♦', 'K'));
        cardValueMap.put(28, new CardValueModel('♦', 'A'));
    }

    public static CardValueModel getCardValueModel(Integer cardIndex){
        CardValueModel model = null;
        if(null != (model = cardValueMap.get(cardIndex))){
            return model;
        }
        return null;
    }

    public static Character getCardValue(Integer cardIndex){
        CardValueModel model = null;
        if(null != (model = cardValueMap.get(cardIndex))){
            return model.getCardValue();
        }
        return null;
    }

    public static String getCardValueString(Integer cardIndex){
        CardValueModel model = null;
        if(null != (model = cardValueMap.get(cardIndex))){
            return model.toString();
        }
        return null;
    }
}
