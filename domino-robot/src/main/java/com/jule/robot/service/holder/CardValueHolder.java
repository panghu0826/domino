package com.jule.robot.service.holder;

import com.jule.robot.model.CardValueModel;

import java.util.HashMap;
import java.util.Map;

/**
 * 牌对应值 的控制类
 */
public class CardValueHolder {
    private final static Map<Integer, CardValueModel> cardValueMap = new HashMap();
    private final static Map<String, Integer> cardModelMap = new HashMap();

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


        /**
         * 通过花色牌型，反推出数字ID
         */
        //黑桃
        cardModelMap.put(new CardValueModel('♠', 'A').toString(), 1);
        cardModelMap.put(new CardValueModel('♠', '2').toString(), 2);
        cardModelMap.put(new CardValueModel('♠', '3').toString(), 3);
        cardModelMap.put(new CardValueModel('♠', '4').toString(), 4);
        cardModelMap.put(new CardValueModel('♠', '5').toString(), 5);
        cardModelMap.put(new CardValueModel('♠', '6').toString(), 6);
        cardModelMap.put(new CardValueModel('♠', '7').toString(), 7);
        cardModelMap.put(new CardValueModel('♠', '8').toString(), 8);
        cardModelMap.put(new CardValueModel('♠', '9').toString(), 9);
        cardModelMap.put(new CardValueModel('♠', 'T').toString(), 10);
        cardModelMap.put(new CardValueModel('♠', 'J').toString(), 11);
        cardModelMap.put(new CardValueModel('♠', 'Q').toString(), 12);
        cardModelMap.put(new CardValueModel('♠', 'K').toString(), 13);

        //红桃
        cardModelMap.put(new CardValueModel('♥', 'A').toString(), 14);
        cardModelMap.put(new CardValueModel('♥', '2').toString(), 15);
        cardModelMap.put(new CardValueModel('♥', '3').toString(), 16);
        cardModelMap.put(new CardValueModel('♥', '4').toString(), 17);
        cardModelMap.put(new CardValueModel('♥', '5').toString(), 18);
        cardModelMap.put(new CardValueModel('♥', '6').toString(), 19);
        cardModelMap.put(new CardValueModel('♥', '7').toString(), 20);
        cardModelMap.put(new CardValueModel('♥', '8').toString(), 21);
        cardModelMap.put(new CardValueModel('♥', '9').toString(), 22);
        cardModelMap.put(new CardValueModel('♥', 'T').toString(), 23);
        cardModelMap.put(new CardValueModel('♥', 'J').toString(), 24);
        cardModelMap.put(new CardValueModel('♥', 'Q').toString(), 25);
        cardModelMap.put(new CardValueModel('♥', 'K').toString(), 26);

        //草花
        cardModelMap.put(new CardValueModel('♣', 'A').toString(), 27);
        cardModelMap.put(new CardValueModel('♣', '2').toString(), 28);
        cardModelMap.put(new CardValueModel('♣', '3').toString(), 29);
        cardModelMap.put(new CardValueModel('♣', '4').toString(), 30);
        cardModelMap.put(new CardValueModel('♣', '5').toString(), 31);
        cardModelMap.put(new CardValueModel('♣', '6').toString(), 32);
        cardModelMap.put(new CardValueModel('♣', '7').toString(), 33);
        cardModelMap.put(new CardValueModel('♣', '8').toString(), 34);
        cardModelMap.put(new CardValueModel('♣', '9').toString(), 35);
        cardModelMap.put(new CardValueModel('♣', 'T').toString(), 36);
        cardModelMap.put(new CardValueModel('♣', 'J').toString(), 37);
        cardModelMap.put(new CardValueModel('♣', 'Q').toString(), 38);
        cardModelMap.put(new CardValueModel('♣', 'K').toString(), 39);

        //方块
        cardModelMap.put(new CardValueModel('♦', 'A').toString(), 40);
        cardModelMap.put(new CardValueModel('♦', '2').toString(), 41);
        cardModelMap.put(new CardValueModel('♦', '3').toString(), 42);
        cardModelMap.put(new CardValueModel('♦', '4').toString(), 43);
        cardModelMap.put(new CardValueModel('♦', '5').toString(), 44);
        cardModelMap.put(new CardValueModel('♦', '6').toString(), 45);
        cardModelMap.put(new CardValueModel('♦', '7').toString(), 46);
        cardModelMap.put(new CardValueModel('♦', '8').toString(), 47);
        cardModelMap.put(new CardValueModel('♦', '9').toString(), 48);
        cardModelMap.put(new CardValueModel('♦', 'T').toString(), 49);
        cardModelMap.put(new CardValueModel('♦', 'J').toString(), 50);
        cardModelMap.put(new CardValueModel('♦', 'Q').toString(), 51);
        cardModelMap.put(new CardValueModel('♦', 'K').toString(), 52);
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

    /**
     * 获得
     */
    public static int getCardIndex(CardValueModel model){
        int index = 0;
        if(cardModelMap.containsKey(model.toString())){
            index = cardModelMap.get(model.toString());
        }
        return index;
    }
}
