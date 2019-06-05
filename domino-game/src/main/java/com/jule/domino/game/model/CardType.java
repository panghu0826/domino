package com.jule.domino.game.model;

import com.google.common.primitives.Ints;
import com.jule.domino.game.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public enum CardType {
    shunjin("顺金", 18),//顺子 and 同花

    wuxiao("五小", 17),

    zhadan("炸弹", 16),

    hulu("葫芦", 15),//三带二

    tonghua("同花", 14),//同花色

    wuhua("五花", 13),

    shunzi("顺子", 12),//顺子 1~5 10~A
    //11往上的特殊牌型都不需要成为牛
    duiziniu("对子牛", 11),//牛牛余下两张牌为对子

    niuniu("牛牛", 10),

    niujiu("牛九", 9),

    niuba("牛八", 8),

    niuqi("牛七", 7),

    niuliu("牛六", 6),

    niuwu("牛五", 5),

    niusi("牛四", 4),

    niusan("牛三", 3),

    niuer("牛二", 2),

    niuyi("牛一", 1),

    wuniu("无牛", 0);

    private String name;

    private int type;

    private boolean haveSpecialCard;

    private List<Integer> handCard;//计算完癞子的牌

    private List<Integer> cards;//去除掉癞子牌的手牌

    private CardType(String name, int type) {
        this.name = name;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

//    /***
//     * 比较牌型大小
//     * @param cardType
//     * @return
//     */
//    public int compare(CardType cardType) {
//        if (this.type > cardType.getType()) {
//            return 1;
//        } else if (this.type < cardType.getType()) {
//            return -1;
//        } else {
//            if (!this.haveSpecialCard && cardType.haveSpecialCard) {
//                return 1;
//            } else if (this.haveSpecialCard && !cardType.haveSpecialCard) {
//                return -1;
//            } else {
//                if (this.type == 16 || this.type == 15) { //炸弹或者葫芦取第三张牌对比大小 11112 11122
//                    int compareA = this.handCard.get(2);
//                    int compareB = cardType.handCard.get(2);
//                    if (compareA > compareB) {
//                        return 1;
//                    } else if (compareA < compareB) {
//                        return -1;
//                    } else {
//                        System.out.println("----------" + this.handCard.toString());
//                        System.out.println("----------" + cardType.handCard.toString());
//                        System.out.println("炸弹或者葫芦：一样牌型，一样的最大牌");
//                        return 0;
//                    }
//                } else if (this.type == 11) { //对子
//                    int pairA = getPairMaxCard(this);
//                    int pairB = getPairMaxCard(cardType);
//                    if (getCardValue(pairA) > getCardValue(pairB)) {
//                        return 1;
//                    } else if (getCardValue(pairA) < getCardValue(pairB)) {
//                        return -1;
//                    } else {
//                        if (getCardColor(pairA) < getCardColor(pairB)) { //值越小花色越大
//                            return 1;
//                        } else if (getCardColor(pairA) < getCardColor(pairB)) {
//                            return -1;
//                        } else {
//                            System.out.println("----------" + this.handCard.toString());
//                            System.out.println("----------" + cardType.handCard.toString());
//                            System.out.println("对子：一样牌型，一样的最大牌");
//                        }
//                    }
//                } else {
//                    List<Integer> cardA = getSortCard(this);
//                    List<Integer> cardB = getSortCard(cardType);
//                    for (int i = 0; i < cardA.size(); i++) {
//                        int a = getCardValue(cardA.get(i));
//                        int b = getCardValue(cardB.get(i));
//                        if (a > b) {
//                            return 1;
//                        } else if (a < b) {
//                            return -1;
//                        }else {
//                            int c = getCardColor(cardA.get(i));
//                            int d = getCardColor(cardB.get(i));
//                            if (c < d) {
//                                return 1;
//                            } else if (c > d) {
//                                return -1;
//                            }
//                        }
//                    }
//                    System.out.println("----------" + this.handCard.toString());
//                    System.out.println("----------" + cardType.handCard.toString());
//                    System.out.println("对比完所有牌：一样牌型，一样的最大牌");
//                }
//            }
//        }
//        return 0;
//    }
//
//    //对手牌按牌值大小和花色大小排序
//    private static List<Integer> getSortCard(CardType cardType) {
//        List<Integer> array = new ArrayList<>(cardType.handCard);
//        Collections.sort(array, new Comparator<Integer>() {
//            @Override
//            public int compare(Integer o1, Integer o2) {
//                int a = getCardValue(o1);
//                int b = getCardValue(o2);
//                if (a > b) {
//                    return 1;
//                } else if (a < b) {
//                    return -1;
//                } else {
//                    int c = getCardColor(o1);
//                    int d = getCardColor(o2);
//                    if (c < d) {
//                        return 1;
//                    } else {
//                        return -1;
//                    }
//                }
//            }
//        });
//        return array;
//    }
//
//    //找出对子牌中花色较大的那张牌
//    private static int getPairMaxCard(CardType cardType) {
//        List<Integer> array = cardType.handCard;
//        List<Integer> list = array.stream() // list 对应的 Stream
//                .collect(Collectors.toMap(e -> e, e -> 1, (a, b) -> a + b)) // 获得元素出现频率的 Map，键为元素，值为元素出现的次数
//                .entrySet().stream() // 所有 entry 对应的 Stream
//                .filter(entry -> entry.getValue() > 1) // 过滤出元素出现次数大于 1 的 entry
//                .map(entry -> entry.getKey()) // 获得 entry 的键（重复元素）对应的 Stream
//                .collect(Collectors.toList());
//        int maxCard = -1;
//        for (int i = 0; i < array.size(); i++) {
//            if (list.get(list.size() - 1) == array.get(i)) {
//                if (maxCard != -1) {
//                    if (getCardColor(array.get(i)) < getCardColor(maxCard)) { //值越小花色越大
//                        maxCard = array.get(i);
//                    }
//                } else {
//                    maxCard = array.get(i);
//                }
//            }
//        }
//        return maxCard;
//    }
//
//    private static int getCardValue(int card) {
//        return card % 13 == 0 ? 13 : card % 13;
//    }
//
//    private static int getCardColor(int card) {
//        return card / 13;
//    }
//    /***
//     * 获取牌型倍数
//     * @param difen
//     * @param bank
//     * @param nomal
//     * @return
//     */
//    public double getScore(long difen, PlayerInfo bank,PlayerInfo nomal) {
//        double score = 0;
//        int multiple = 1;
//        if (bank.getMultiple() > 1) {
//            multiple = bank.getMultiple();
//        }
//        int carTypeMultiple = CardTypeMultipleService.getInstance().getMultipleByCardType(this.type);
//        score = difen * carTypeMultiple * multiple * nomal.getBetMultiple();
//        return score;
//    }

//    //前面的list为计算癞子之后的牌  后面的为去除完癞子玩家的手牌(可能不足五张)
//    private static List<Integer> setCardColor(List<Integer> list, List<Integer> cards) {
//        List<Integer> array = new ArrayList<>();
////        List<Integer> list = new ArrayList<>();
////        handCards.forEach(e -> list.add((int) e));
//        for (int card : list) {
//            List<Integer> color = new ArrayList<>(Ints.asList(card + 13, card + 26, card + 39));
//            if (!cards.contains(color.get(0)) && !array.contains(color.get(0))) {
//                array.add(color.get(0));
//            } else if (!cards.contains(color.get(1)) && !array.contains(color.get(1))) {
//                array.add(color.get(1));
//            } else if (!cards.contains(color.get(2)) && !array.contains(color.get(2))) {
//                array.add(color.get(2));
//            } else {
//                array.add(card);
//            }
//        }
//        return array;
//    }

    public List<Integer> getHandCard() {
        return handCard == null ? cards : handCard;
    }

    public CardType setHandCard(List<Integer> handCard) {
        Collections.sort(handCard);
        this.handCard = handCard;
        return this;
    }

    public boolean isHaveSpecialCard() {
        return haveSpecialCard;
    }

    public void setHaveSpecialCard(boolean haveSpecialCard) {
        this.haveSpecialCard = haveSpecialCard;
    }

    public List<Integer> getCards() {
        return cards;
    }

    public void setCards(List<Integer> cards) {
        Collections.sort(cards);
        this.cards = cards;
    }

    @Override
    public String toString() {
        return "CardType{" +
                "name='" + name + '\'' +
//                ", type=" + type +
                ", handCard=" + handCard +
                '}';
    }

    class cardData{

    }
}
