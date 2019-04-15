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
        cardValueMap.put(1, new CardValueModel('♠', 'A'));
        cardValueMap.put(2, new CardValueModel('♠', '2'));
        cardValueMap.put(3, new CardValueModel('♠', '3'));
        cardValueMap.put(4, new CardValueModel('♠', '4'));
        cardValueMap.put(5, new CardValueModel('♠', '5'));
        cardValueMap.put(6, new CardValueModel('♠', '6'));
        cardValueMap.put(7, new CardValueModel('♠', '7'));
        cardValueMap.put(8, new CardValueModel('♠', '8'));
        cardValueMap.put(9, new CardValueModel('♠', '9'));
        cardValueMap.put(10, new CardValueModel('♠', 'T'));
        cardValueMap.put(11, new CardValueModel('♠', 'J'));
        cardValueMap.put(12, new CardValueModel('♠', 'Q'));
        cardValueMap.put(13, new CardValueModel('♠', 'K'));

        //红桃
        cardValueMap.put(14, new CardValueModel('♥', 'A'));
        cardValueMap.put(15, new CardValueModel('♥', '2'));
        cardValueMap.put(16, new CardValueModel('♥', '3'));
        cardValueMap.put(17, new CardValueModel('♥', '4'));
        cardValueMap.put(18, new CardValueModel('♥', '5'));
        cardValueMap.put(19, new CardValueModel('♥', '6'));
        cardValueMap.put(20, new CardValueModel('♥', '7'));
        cardValueMap.put(21, new CardValueModel('♥', '8'));
        cardValueMap.put(22, new CardValueModel('♥', '9'));
        cardValueMap.put(23, new CardValueModel('♥', 'T'));
        cardValueMap.put(24, new CardValueModel('♥', 'J'));
        cardValueMap.put(25, new CardValueModel('♥', 'Q'));
        cardValueMap.put(26, new CardValueModel('♥', 'K'));

        //草花
        cardValueMap.put(27, new CardValueModel('♣', 'A'));
        cardValueMap.put(28, new CardValueModel('♣', '2'));
        cardValueMap.put(29, new CardValueModel('♣', '3'));
        cardValueMap.put(30, new CardValueModel('♣', '4'));
        cardValueMap.put(31, new CardValueModel('♣', '5'));
        cardValueMap.put(32, new CardValueModel('♣', '6'));
        cardValueMap.put(33, new CardValueModel('♣', '7'));
        cardValueMap.put(34, new CardValueModel('♣', '8'));
        cardValueMap.put(35, new CardValueModel('♣', '9'));
        cardValueMap.put(36, new CardValueModel('♣', 'T'));
        cardValueMap.put(37, new CardValueModel('♣', 'J'));
        cardValueMap.put(38, new CardValueModel('♣', 'Q'));
        cardValueMap.put(39, new CardValueModel('♣', 'K'));

        //方块
        cardValueMap.put(40, new CardValueModel('♦', 'A'));
        cardValueMap.put(41, new CardValueModel('♦', '2'));
        cardValueMap.put(42, new CardValueModel('♦', '3'));
        cardValueMap.put(43, new CardValueModel('♦', '4'));
        cardValueMap.put(44, new CardValueModel('♦', '5'));
        cardValueMap.put(45, new CardValueModel('♦', '6'));
        cardValueMap.put(46, new CardValueModel('♦', '7'));
        cardValueMap.put(47, new CardValueModel('♦', '8'));
        cardValueMap.put(48, new CardValueModel('♦', '9'));
        cardValueMap.put(49, new CardValueModel('♦', 'T'));
        cardValueMap.put(50, new CardValueModel('♦', 'J'));
        cardValueMap.put(51, new CardValueModel('♦', 'Q'));
        cardValueMap.put(52, new CardValueModel('♦', 'K'));
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
