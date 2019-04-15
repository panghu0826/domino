package com.jule.domino.game.utils;

import com.jule.domino.game.model.PlayerInfo;

import java.util.*;

/**
 * 比牌器
 * @author
 * @since 2018/10/18 16:16
 */
public class CardComparator {

    public static final CardComparator OBJ = new CardComparator();

    private static Map<Integer ,Integer> _map = new HashMap<>();

    /**
     * 输赢
     */
    private static final int win = 1;
    private static final int equal = 0;
    private static final int lose = -1;

    static {
        //牌型对应点数
        _map.put(1,3);
        _map.put(2,6);
        _map.put(3,5);
        _map.put(4,5);
        _map.put(5,7);
        _map.put(6,7);
        _map.put(7,8);
        _map.put(8,8);
        _map.put(9,9);
        _map.put(10,9);
        _map.put(11,6);
        _map.put(12,6);
        _map.put(13,7);
        _map.put(14,7);
        _map.put(15,10);
        _map.put(16,10);
        _map.put(17,11);
        _map.put(18,11);
        _map.put(19,4);
        _map.put(20,4);
        _map.put(21,6);
        _map.put(22,6);
        _map.put(23,10);
        _map.put(24,10);
        _map.put(25,4);
        _map.put(26,4);
        _map.put(27,8);
        _map.put(28,8);
        _map.put(29,2);
        _map.put(30,2);
        _map.put(31,12);
        _map.put(32,12);
    }

    //至尊
    private static final int[] zhizun = {2, 1};
    //双天
    private static final int[] shuangtian = {32, 31};
    //双地
    private static final int[] shuangdi = {30, 29};
    //双人
    private static final int[] shuangren = {28, 27};
    //双鹅
    private static final int[] shuange = {26, 25};
    //双梅
    private static final int[] shuangmei = {24, 23};
    //双长三
    private static final int[] shuangchangsan = {22, 21};
    //双板凳
    private static final int[] shuangbandeng = {20, 19};
    //双斧头
    private static final int[] shuangfutou = {18, 17};
    //双红头
    private static final int[] shuanghotou = {16, 15};
    //双高脚
    private static final int[] shuanggj = {14, 13};
    //双零零
    private static final int[] shuangll = {12, 11};
    //杂九
    private static final int[] zha9 = {10, 9};
    //杂八
    private static final int[] zha8 = {8, 7};
    //杂七
    private static final int[] zha7 = {6, 5};
    //杂五
    private static final int[] zha5 = {4, 3};
    //天王
    private static final int[] tianwang_1 = {32,9 };
    private static final int[] tianwang_2 = {31,9 };
    private static final int[] tianwang_3 = {32,10 };
    private static final int[] tianwang_4 = {31,10 };
    //地王
    private static final int[] diwang_1 = {29, 9};
    private static final int[] diwang_2 = {30, 9};
    private static final int[] diwang_3 = {29, 10};
    private static final int[] diwang_4 = {30, 10};
    //天杠
    private static final int[] tiangang_1 = {32, 8};
    private static final int[] tiangang_2 = {31, 8};
    private static final int[] tiangang_3 = {32, 7};
    private static final int[] tiangang_4 = {31, 7};
    private static final int[] tiangang_5 = {32, 28};
    private static final int[] tiangang_6 = {31, 28};
    private static final int[] tiangang_7 = {32, 27};
    private static final int[] tiangang_8 = {31, 27};
    //地杠
    private static final int[] digang_1 = {29, 8};
    private static final int[] digang_2 = {30, 8};
    private static final int[] digang_3 = {29, 7};
    private static final int[] digang_4 = {30, 7};
    private static final int[] digang_5 = {29, 28};
    private static final int[] digang_6 = {30, 28};
    private static final int[] digang_7 = {29, 27};
    private static final int[] digang_8 = {30, 27};
    //天高九
    private static final int[] tgj_1 = {32, 5};
    private static final int[] tgj_2 = {31, 5};
    //地高九
    private static final int[] dgj_1 = {30, 13};
    private static final int[] dgj_2 = {29, 13};
    private static final int[] dgj_3 = {30, 14};
    private static final int[] dgj_4 = {29, 14};

    /**
     * 获胜玩家
     * @param inGamePlayers
     * @return
     */
    public Map<String,Integer> getWinner(Map<Integer, PlayerInfo> inGamePlayers){
        Map<String,Integer> map = new HashMap<>();
        if (inGamePlayers == null){
            return map;
        }

        Map<Integer , Integer> equalMap = new HashMap<>();
        //获得最大牌型玩家
        PlayerInfo winner = null;
        for (Integer seatNum : inGamePlayers.keySet()){
            if (winner == null){
                winner = inGamePlayers.get(seatNum);
                continue;
            }

            //比较玩家
            PlayerInfo curPlay = inGamePlayers.get(seatNum);
            int ret = compare(winner.getCards(), curPlay.getCards());
            if ( ret == -1){
                //更换赢家
                winner = curPlay;
            }else if (ret == 0 ){
                //平牌
                equalMap.put(curPlay.getSeatNum(), winner.getSeatNum());
            }
        }

        if (winner == null){
            return map;
        }

        map.put(winner.getPlayerId(),winner.getBetMultiple());
        for (Integer k: equalMap.keySet()){
            Integer v = equalMap.get(k);
            if (v == winner.getSeatNum()){
                PlayerInfo player = inGamePlayers.get(k);
                map.put(player.getPlayerId(), player.getBetMultiple());
            }
        }

        return map;
    }

    /**
     * 比较手牌大小
     * @param comparor 发起者
     * @param target   比牌目标
     * @return
     *                  发起者 > 目标 返回 1
     *                  发起者 = 目标 返回 0
     *                  发起者 < 目标 返回 -1
     */
    public int compare(List<Byte> comparor, List<Byte> target){
        if (comparor == null || target == null){
            return -1;
        }

        int[] cardArrA = {NumUtils.byteToInt(comparor.get(0)), NumUtils.byteToInt(comparor.get(1))};
        int[] cardArrB = {NumUtils.byteToInt(target.get(0)), NumUtils.byteToInt(target.get(1))};
        return compare(cardArrA,cardArrB);
    }

    /**
     * 比较手牌大小
     * @param comparor 发起者
     * @param target   比牌目标
     * @return
     *                  发起者 > 目标 返回 1
     *                  发起者 = 目标 返回 0
     *                  发起者 < 目标 返回 -1
     */
    public int compare(int[] comparor, int[] target){
        //参数验证
        if (comparor == null || comparor.length != 2){
            return lose;
        }
        if (target == null || target.length != 2){
            return win;
        }

        int _cspecial = isSpecialCard(comparor);
        int _tspecial = isSpecialCard(target);

        //有特殊牌型
        if (_cspecial > _tspecial){
            return win;
        }
        if (_cspecial < _tspecial){
            return lose;
        }
        if (_cspecial == _tspecial && _cspecial != 0){
            return equal;
        }

        //普通牌型、比牌点数
        int _cpoint = getCommomCardPoint(comparor);
        int _tpoint = getCommomCardPoint(target);
        if (_cpoint > _tpoint){
            return win;
        }
        if (_cpoint < _tpoint){
            return lose;
        }

        //比单牌最大牌
        int[] _ccard = sort(comparor);
        int[] _tcard = sort(target);
        if (_ccard[0] > _tcard[0]){
            return win;
        }
        if (_ccard[0] < _tcard[0]){
            return lose;
        }

        //比单牌最小牌
        if (_ccard[1] > _tcard[1]){
            return win;
        }
        if (_ccard[1] < _tcard[1]){
            return lose;
        }

        // 这个时候我真的没办法了
        // 你们都是大哥
        return equal;
    }

    /**
     * 判断手牌是否是对牌、
     * @param handCards
     * @return  牌力值。  返回 0 非对牌
     */
    public int isSpecialCard(List<Byte> handCards){
        if (handCards == null || handCards.size() != 2){
            return 0;
        }

        int[] cardArr = {NumUtils.byteToInt(handCards.get(0)), NumUtils.byteToInt(handCards.get(1))};
        return isSpecialCard(cardArr);

    }

    /**
     * 判断手牌是否是对牌、
     * @param handCards
     * @return  牌力值。  返回 0 非对牌
     */
    public int isSpecialCard(int[] handCards){
        //先把手牌排序 【大牌，小牌】
        int[] cards = sort(handCards);

        //判断特殊牌型
        if (equal(cards, zhizun)){
            return 100;
        }
        if (equal(cards, shuangtian)){
            return 99;
        }
        if (equal(cards, shuangdi)){
            return 98;
        }
        if (equal(cards, shuangren)){
            return 97;
        }
        if (equal(cards, shuange)){
            return 96;
        }
        if (equal(cards, shuangmei)){
            return 95;
        }
        if (equal(cards, shuangchangsan)){
            return 94;
        }
        if (equal(cards, shuangbandeng)){
            return 93;
        }
        if (equal(cards, shuangfutou)){
            return 92;
        }
        if (equal(cards, shuanghotou)){
            return 91;
        }
        if (equal(cards, shuanggj)){
            return 90;
        }
        if (equal(cards, shuangll)){
            return 89;
        }
        if (equal(cards, zha9)){
            return 88;
        }
        if (equal(cards, zha8)){
            return 87;
        }
        if (equal(cards, zha7)){
            return 86;
        }
        if (equal(cards, zha5)){
            return 85;
        }
        if (equal(cards, tianwang_1) || equal(cards, tianwang_2)|| equal(cards, tianwang_3)|| equal(cards, tianwang_4)){
            return 84;
        }
        if (equal(cards, diwang_1) || equal(cards, diwang_2)|| equal(cards, diwang_3)|| equal(cards, diwang_4)){
            return 83;
        }
        if (equal(cards, tiangang_1) || equal(cards, tiangang_2)|| equal(cards, tiangang_3)|| equal(cards, tiangang_4)
        ||  equal(cards, tiangang_5) || equal(cards, tiangang_6)|| equal(cards, tiangang_7)|| equal(cards, tiangang_8)){
            return 82;
        }
        if (equal(cards, digang_1) || equal(cards, digang_2)|| equal(cards, digang_3)|| equal(cards, digang_4)
          ||equal(cards, digang_5) || equal(cards, digang_6)|| equal(cards, digang_7)|| equal(cards, digang_8)){
            return 81;
        }
        if (equal(cards, tgj_1) || equal(cards, tgj_2)){
            return 80;
        }
        if (equal(cards, dgj_1) || equal(cards, dgj_2) || equal(cards, dgj_3) || equal(cards, dgj_4)){
            return 79;
        }
        return 0;
    }

    /**
     * 取得手牌点数
     * @param cards
     * @return
     */
    private int getCommomCardPoint(int[] cards){
        //参数验证
        if (cards == null || cards.length != 2){
            return 0;
        }

        if (!_map.containsKey(cards[0]) || !_map.containsKey(cards[1])){
            return 0;
        }

        //获取点数
        int cardA = _map.get(cards[0]);
        int cardB = _map.get(cards[1]);

        int points = ((cardA + cardB) % 10);
        return points;
    }

    /**
     * 整理手牌
     * @param cards
     * @return
     */
    private int[] sort(int[] cards){
        //验证手牌
        if (cards == null || cards.length != 2){
            return null;
        }

        if (cards[0] > cards[1]){
            return cards;
        }else {
            int[] curCards = new int[2];
            curCards[0] = cards[1];
            curCards[1] = cards[0];
            return curCards;
        }
    }

    /**
     * 比较两个数组元素相同
     * @param object
     * @param _object
     * @return
     */
    private boolean equal(int[] object , int[] _object){
        if (object == null || _object == null){
            return false;
        }

        if (object.length != _object.length){
            return false;
        }

        for (int index = 0; index < object.length ; index ++){
            if (object[index] != _object[index]){
                return false;
            }
        }

        return true;
    }

    public int[] getSpecialCard(){
        /*List<int[]> list = Arrays.asList(
                zhizun,shuangtian,shuangdi,shuangren,shuange,shuangmei,shuangchangsan,shuangbandeng,shuangfutou,
                shuanghotou,shuanggj,shuangll,zha9,zha8,zha7,zha5,
                tianwang_1,tianwang_2,tianwang_3,tianwang_4,
                diwang_1,diwang_2,diwang_3,diwang_4,
                tiangang_1,tiangang_2,tiangang_3,tiangang_4,tiangang_5,tiangang_6,tiangang_7,tiangang_8,
                digang_1,digang_2,digang_3,digang_4,digang_5,digang_6,digang_7,digang_8,
                tgj_1,tgj_2,dgj_1,dgj_2,dgj_3,dgj_4
        );*/

        List<int[]> list = Arrays.asList(
                zhizun,shuangtian
        );

        Random random = new Random();
        int index = random.nextInt(list.size());

        return list.get(index);
    }

}
