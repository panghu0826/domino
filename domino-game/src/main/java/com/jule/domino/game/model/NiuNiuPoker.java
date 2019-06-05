package com.jule.domino.game.model;

import com.google.common.primitives.Ints;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.game.gameUtil.DealCardForTable;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.holder.CardOfTableHolder;
import com.jule.domino.game.service.holder.CardValueHolder;
import com.jule.domino.game.utils.CombineUtil;
import com.jule.domino.game.utils.FindNode;
import com.jule.domino.game.utils.NumUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@Setter@Getter
public class NiuNiuPoker {
//    public final static String SHUN_JIN = "顺金";
//    public final static String WU_XIAO = "五小";
//    public final static String ZHA_DAN = "炸弹";
//    public final static String HU_LU = "葫芦";
//    public final static String TONG_HUA = "同花";
//    public final static String WU_HUA = "五花";
//    public final static String SHUN_ZI = "顺子";
//    public final static String DUI_ZI_NIU = "一对";
//    public final static String NIU_NIU = "牛牛";
//    public final static String NIU_JIU = "牛九";
//    public final static String NIU_BA = "牛八";
//    public final static String NIU_QI = "牛七";
//    public final static String NIU_LIU = "牛六";
//    public final static String NIU_WU = "牛五";
//    public final static String NIU_SI = "牛四";
//    public final static String NIU_SAN = "牛三";
//    public final static String NIU_ER = "牛二";
//    public final static String NIU_YI = "牛一";
//    public final static String WU_NIU = "无牛";

    private String typeName; //牌型类型名
    private int typeCompareValue; //牌型比较大小时所用的int值。（值越大代表牌型越大）
    private boolean haveSpecialCard;//玩家有无癞子牌
    private List<Integer> handCard;//去除掉癞子牌的手牌
    private List<Integer> cards;//计算完癞子的牌

    private static byte changeableCard = 8;//demo

//    public static void main(String[] args) {
//        for (int k = 0; k < 1000; k++) {
//            AbstractTable table = new AbstractTable();
//            CardOfTableHolder.PutCardOperationObj("123",new DealCardForTable(2));
//            List<Byte> cards = new ArrayList<>();
//            changeableCard = (byte) (1 + Math.random() * (52));
//            int ina = (int) (1 + Math.random() * (4));
//            int[] c = CardOfTableHolder.TakeCardOperationObj("123").hair_card(5 - ina);
//            for (int j = 0; j < c.length; j++) {
//                cards.add((byte) (1 + Math.random() * (52)));
//            }
//            for (int i = 0; i < ina; i++) {
//                cards.add(changeableCard);
//            }
//            List<Byte> cardB = new ArrayList<>();
//            int ins = (int) (1 + Math.random() * (4));
//            int[] d = CardOfTableHolder.TakeCardOperationObj("123").hair_card(5 - ins);
//            for (int j = 0; j < d.length; j++) {
//                cardB.add((byte) (1 + Math.random() * (52)));
//            }
//            for (int i = 0; i < ins; i++) {
//                cardB.add(changeableCard);
//            }
//            List<String> wanFa = GameConst.wanFa;
//            System.out.println("本局癞子牌为：-------------------------" + CardValueHolder.getCardValue((int)changeableCard));
//            System.out.println("-----------------------------玩家手牌-------------------------");
//            pointln("玩家A：", cards);
//            pointln("玩家B：", cardB);
//            NiuNiuPoker niuA = new NiuNiuPoker(cards, wanFa);
//            NiuNiuPoker niuB = new NiuNiuPoker(cardB, wanFa);
//            System.out.println("-----------------------------计算之后-------------------------");
//            point("玩家 A 牌型 -> " + niuA.typeName + ", 手牌：", niuA.cards);
//            point("玩家 B 牌型 -> " + niuB.typeName + ", 手牌：", niuB.cards);
//            System.out.println("-----------------------------对比结果-------------------------");
//            int in = niuA.compare(niuB);
//            if (in == 1) System.out.println("玩家 A 赢了！");
//            if (in == -1) System.out.println("玩家 B 赢了！");
//            if (in == 0) System.out.println("平局！不可能。(bug)");
//            System.out.println();
//        }
//    }

//    public static void main(String[] args) {
//        List<Byte> cards = new ArrayList<>();
//        changeableCard = (byte) 9;
//        cards.add((byte)3);
//        cards.add((byte)3);
//        cards.add((byte)8);
//        cards.add((byte)9);
//        cards.add((byte)9);
//        List<Byte> cardB = new ArrayList<>();
//        cardB.add((byte)7);
//        cardB.add((byte)5);
//        cardB.add((byte)9);
//        cardB.add((byte)9);
//        cardB.add((byte)9);
//        List<String> wanFa = GameConst.wanFa;
//        System.out.println("本局癞子牌为：-------------------------"+changeableCard);
//        System.out.println("-----------------------------玩家手牌-------------------------");
//        pointln("玩家A：", cards);
//        pointln("玩家B：", cardB);
//        NiuNiuPoker niuA = new NiuNiuPoker(cards, wanFa);
//        NiuNiuPoker niuB = new NiuNiuPoker(cardB, wanFa);
//        System.out.println("-----------------------------计算之后-------------------------");
//        point("玩家 A 牌型 -> " + niuA.typeName + ", 手牌：", niuA.cards);
//        point("玩家 B 牌型 -> " + niuB.typeName + ", 手牌：", niuB.cards);
//        System.out.println("-----------------------------对比结果-------------------------");
//        int in = niuA.compare(niuB);
//        if (in == 1) System.out.println("玩家 A 赢了！");
//        if (in == -1) System.out.println("玩家 B 赢了！");
//        if (in == 0) System.out.println("平局！不可能。(bug)");
//        System.out.println();
//    }
//
//    private static void pointln(String str, List<Byte> cards) {
//        System.out.print(str);
//        for (int card : cards) {
//            System.out.print(CardValueHolder.getCardValueString(card) + ", ");
//        }
//        System.out.println();
//    }
//
//    private static void point(String str, List<Integer> cards) {
//        System.out.print(str);
//        for (int card : cards) {
//            System.out.print(CardValueHolder.getCardValueString(card) + ", ");
//        }
//        System.out.println();
//    }

    public NiuNiuPoker(List<Byte> cards, List<Integer> wanfa) {
        List<Integer> array = new ArrayList<>();
        Iterator<Byte> iter = cards.iterator();
        int specialCards = 0;
        while (iter.hasNext()) {
            byte card = iter.next();
            if (getCardValue(card) == changeableCard || card == 53 || card == 54) {
                iter.remove();
                specialCards++;
            } else {
                array.add((int) card);
            }
        }
        handCard = array;
        haveSpecialCard = (specialCards != 0);
        if (wanfa.contains(8) && shunJin(cards, specialCards)) return;
        if (wanfa.contains(7) && wuXiao(cards, specialCards)) return;
        if (wanfa.contains(6) && zhaDan(cards, specialCards)) return;
        if (wanfa.contains(5) && huLu(cards, specialCards)) return;
        if (wanfa.contains(4) && tongHua(cards, specialCards)) return;
        if (wanfa.contains(3) && wuHua(cards, specialCards)) return;
        if (wanfa.contains(2) && shunZi(cards, specialCards)) return;
        if (wanfa.contains(1) && duiZi(cards, specialCards)) return;
        //计算牛牛牌型
        List<List<Byte>> find = CombineUtil.combine(cards, 3);
        Collections.sort(find, new Comparator<List<Byte>>() {
            @Override
            public int compare(List<Byte> o1, List<Byte> o2) {
                int total2 = 0;
                for (Byte b : o2) {
                    total2 += NumUtils.byteToInt(b);
                }
                int total1 = 0;
                for (Byte b : o1) {
                    total1 += NumUtils.byteToInt(b);
                }
                return total2 - total1;
            }
        });
        setData(getNomalCardType(find, cards).setHandCard(array));
        return;
    }

    @Override
    public String toString() {
        return "NiuNiuPoker: " + cards;
    }

    /***
     * 比较牌型大小
     * @return
     */
    public int compare(NiuNiuPoker niuPoker) {
        if (typeCompareValue > niuPoker.typeCompareValue) {
            return 1;
        } else if (typeCompareValue < niuPoker.typeCompareValue) {
            return -1;
        } else {
            if (!this.haveSpecialCard && niuPoker.haveSpecialCard) {
                return 1;
            } else if (this.haveSpecialCard && !niuPoker.haveSpecialCard) {
                return -1;
            } else {
                if (typeCompareValue == 16 || typeCompareValue == 15) { //炸弹或者葫芦取第三张牌对比大小 11112 11122
                    int compareA = this.cards.get(2);
                    int compareB = niuPoker.cards.get(2);
                    if (compareA > compareB) {
                        return 1;
                    } else if (compareA < compareB) {
                        return -1;
                    } else {
                        System.out.println("----------" + this.cards.toString());
                        System.out.println("----------" + niuPoker.cards.toString());
                        System.out.println("炸弹或者葫芦：一样牌型，一样的最大牌");
                        return 0;
                    }
                } else if (typeCompareValue == 11) { //对子
                    int pairA = getPairMaxCard(this);
                    int pairB = getPairMaxCard(niuPoker);
                    if (getCardValue(pairA) > getCardValue(pairB)) {
                        return 1;
                    } else if (getCardValue(pairA) < getCardValue(pairB)) {
                        return -1;
                    } else {
                        if (getCardColor(pairA) < getCardColor(pairB)) { //值越小花色越大
                            return 1;
                        } else if (getCardColor(pairA) < getCardColor(pairB)) {
                            return -1;
                        } else {
                            System.out.println("----------" + this.cards.toString());
                            System.out.println("----------" + niuPoker.cards.toString());
                            System.out.println("对子：一样牌型，一样的最大牌");
                        }
                    }
                } else {
                    List<Integer> cardA = getSortCard(this);
                    List<Integer> cardB = getSortCard(niuPoker);
                    for (int i = 0; i < cardA.size(); i++) {
                        int a = getCardValue(cardA.get(i));
                        int b = getCardValue(cardB.get(i));
                        if (a > b) {
                            return 1;
                        } else if (a < b) {
                            return -1;
                        } else {
                            int c = getCardColor(cardA.get(i));
                            int d = getCardColor(cardB.get(i));
                            if (c < d) {
                                return 1;
                            } else if (c > d) {
                                return -1;
                            }
                        }
                    }
                    System.out.println("----------" + this.cards.toString());
                    System.out.println("----------" + niuPoker.cards.toString());
                    System.out.println("对比完所有牌：一样牌型，一样的最大牌");
                }
            }
        }
        return 0;
    }

    //对手牌按牌值大小和花色大小排序
    private static List<Integer> getSortCard(NiuNiuPoker niuPoker) {
        List<Integer> array = new ArrayList<>(niuPoker.cards);
        Collections.sort(array, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                int a = getCardValue(o1);
                int b = getCardValue(o2);
                if (a > b) {
                    return 1;
                } else if (a < b) {
                    return -1;
                } else {
                    int c = getCardColor(o1);
                    int d = getCardColor(o2);
                    if (c < d) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
        });
        return array;
    }

    //找出对子牌中花色较大的那张牌
    private static int getPairMaxCard(NiuNiuPoker niuPoker) {
        List<Integer> array = niuPoker.cards;
        List<Integer> list = array.stream() // list 对应的 Stream
                .collect(Collectors.toMap(e -> e, e -> 1, (a, b) -> a + b)) // 获得元素出现频率的 Map，键为元素，值为元素出现的次数
                .entrySet().stream() // 所有 entry 对应的 Stream
                .filter(entry -> entry.getValue() > 1) // 过滤出元素出现次数大于 1 的 entry
                .map(entry -> entry.getKey()) // 获得 entry 的键（重复元素）对应的 Stream
                .collect(Collectors.toList());
        int maxCard = -1;
        for (int i = 0; i < array.size(); i++) {
            if (list.get(list.size() - 1) == array.get(i)) {
                if (maxCard != -1) {
                    if (getCardColor(array.get(i)) < getCardColor(maxCard)) { //值越小花色越大
                        maxCard = array.get(i);
                    }
                } else {
                    maxCard = array.get(i);
                }
            }
        }
        return maxCard;
    }

    //前面的list为计算癞子之后的牌  后面的为去除完癞子玩家的手牌(可能不足五张)
    private static List<Integer> setCardColor(List<Integer> list, List<Integer> cards) {
        List<Integer> array = new ArrayList<>();
//        List<Integer> list = new ArrayList<>();
//        handCards.forEach(e -> list.add((int) e));
        for (int card : list) {
            List<Integer> color = new ArrayList<>(Ints.asList(card + 13, card + 26, card + 39));
            if (!cards.contains(color.get(0)) && !array.contains(color.get(0))) {
                array.add(color.get(0));
            } else if (!cards.contains(color.get(1)) && !array.contains(color.get(1))) {
                array.add(color.get(1));
            } else if (!cards.contains(color.get(2)) && !array.contains(color.get(2))) {
                array.add(color.get(2));
            } else {
                array.add(card);
            }
        }
        return array;
    }

    private static int getCardValue(int card) {
        return card % 13 == 0 ? 13 : card % 13;
    }

    private static int getCardColor(int card) {
        return card / 13;
    }

    private boolean setData(CardType cardType) {
        typeName = cardType.getName();
        typeCompareValue = cardType.getType();
        cards = cardType.getHandCard();
        return true;
    }

    private boolean shunJin(List<Byte> cards, int specialCards) {
        boolean tonghua = tongHua(cards, specialCards);
        boolean shunzi = shunZi(cards, specialCards);
        if (tonghua && shunzi) {
            return setData(CardType.shunjin.setHandCard(CardType.shunzi.getHandCard()));
        }
        return false;
    }


    private boolean shunZi(List<Byte> cards, int specialCards) {
        List<Integer> array = new ArrayList<>();
        cards.forEach(e -> array.add(getCardValue(e)));
        Collections.sort(array);
        List<Integer> cardsA = new ArrayList<>(Ints.asList(1, 10, 11, 12, 13));//10,J,Q,K,A  A是1
        cardsA.removeAll(array);
        if (cardsA.size() == specialCards) {
            cardsA.addAll(array);
            return setData(CardType.shunzi.setHandCard(cardsA));
        }
        int minCard = array.get(0);
        List<Integer> cardsB = new ArrayList<>(Ints.asList(minCard, minCard + 1, minCard + 2, minCard + 3, minCard + 4));
        cardsB.removeAll(array);
        if (cardsB.size() == specialCards) {
            cardsB.addAll(array);
            return setData(CardType.shunzi.setHandCard(cardsB));
        }
        return false;
    }

    private boolean tongHua(List<Byte> cards, int specialCards) {
        List<Integer> array = new ArrayList<>();
        cards.forEach(e -> array.add(getCardValue(e)));
        Collections.sort(array);
        if (cards.size() != 0) {
            int cardColor = cards.get(0) / 14;
            for (byte card : cards) {
                if (card / 14 != cardColor) {
                    return false;
                }
            }
        }
        if (specialCards > 0) {
            for (int i = 13; i > 0; i--) {
                if (!array.contains(i)) {
                    array.add(i);
                    specialCards--;
                } else {
                    continue;
                }
                if (specialCards == 0) {
                    break;
                }
                Collections.sort(array);
            }
        }
        return setData(CardType.tonghua.setHandCard(array));
    }

    private boolean wuXiao(List<Byte> cards, int specialCards) {
        List<Integer> array = new ArrayList<>();
        cards.forEach(e -> array.add(getCardValue(e)));
        int sum = 0;
        for (int card : cards) {
            sum += card % 13;
        }
        if ((array.size() != 0 && Collections.max(array) >= 5) || sum >= 10) {
            return false;
        }
        //所有的五小组成
        List<Integer> cardsA = new ArrayList<>(Ints.asList(1, 1, 1, 2, 4));
        if (cardsA.containsAll(array)) {
            return setData(CardType.wuxiao.setHandCard(cardsA));
        }
        List<Integer> cardsB = new ArrayList<>(Ints.asList(1, 1, 2, 2, 3));
        if (cardsB.containsAll(array)) {
            return setData(CardType.wuxiao.setHandCard(cardsB));
        }
        List<Integer> cardsC = new ArrayList<>(Ints.asList(1, 2, 2, 2, 2));
        if (cardsC.containsAll(array)) {
            return setData(CardType.wuxiao.setHandCard(cardsC));
        }
        return false;
    }

    private boolean zhaDan(List<Byte> cards, int specialCards) {
        List<Integer> array = new ArrayList<>();
        cards.forEach(e -> array.add(getCardValue(e)));
        Collections.sort(array);
        int count = 0;
        int template = 0;
        for (int a : array) {
            for (int b : array) {
                if (a == b) count++;
            }
            if (count >= 2) {
                template = a;
                break;
            } else {
                count = 0;
            }
        }
        if (count + specialCards < 4) {
            return false;
        }
        if (specialCards >= 4) {
            if (array.size() == 0 || array.get(0) >= 12) {
                return setData(CardType.zhadan.setHandCard(new ArrayList<>(Ints.asList(12, 13, 13, 13, 13))));
            } else {
                return setData(CardType.zhadan.setHandCard(new ArrayList<>(Ints.asList(array.get(0), 13, 13, 13, 13))));
            }
        } else {
            int card = Collections.max(array) == template ? array.get(0) : Collections.max(array);
            return setData(CardType.zhadan.setHandCard(new ArrayList<>(Ints.asList(card, template, template, template, template))));
        }
    }

    private boolean huLu(List<Byte> cards, int specialCards) {
        List<Integer> array = new ArrayList<>();
        cards.forEach(e -> array.add(getCardValue(e)));
        Collections.sort(array);
        Set<Integer> set = new HashSet<>(array);
        if (set.size() > 2 || (cards.size() >= 4 && set.size() != 2)) {//后面的条件是防止玩家拥有4张一样的牌
            return false;
        }
        if (specialCards >= 3) { //
            if (array.size() == 0 || Collections.min(set) >= 12) {
                return setData(CardType.hulu.setHandCard(new ArrayList<>(Ints.asList(12, 12, 13, 13, 13))));
            } else {
                int min = Collections.min(set);
                int max = Collections.max(set) == min ? 13 : Collections.max(set);
                return setData(CardType.hulu.setHandCard(new ArrayList<>(Ints.asList(min, min, max, max, max))));
            }
        } else {
            int min = Collections.min(set);
            int max = Collections.max(set);
            if (set.size() == 1) {
                return setData(CardType.hulu.setHandCard(new ArrayList<>(Ints.asList(min, min, min, 13, 13))));
            } else {
                List<Integer> cardA = new ArrayList<>(Ints.asList(min, min, max, max, max));
                List<Integer> cardB = new ArrayList<>(Ints.asList(min, min, min, max, max));
                if (cardA.get(2) == array.get(2)) {
                    return setData(CardType.hulu.setHandCard(cardA));
                } else {
                    return setData(CardType.hulu.setHandCard(cardB));
                }
            }
        }
    }

    private boolean wuHua(List<Byte> cards, int specialCards) {
        List<Integer> array = new ArrayList<>();
        cards.forEach(e -> array.add(getCardValue(e)));
        Collections.sort(array);
        int k = 0;
        for (int card : array) {
            if (card <= 10) {
                return false;
            } else {
                if (card == 13) k++;
            }
        }
        for (int i = 0; i < specialCards; i++) {
            if (k < 4) {
                array.add(13);
                k++;
            } else {
                array.add(12);
            }
        }
        return setData(CardType.wuhua.setHandCard(array));
    }

    private boolean duiZi(List<Byte> cards, int specialCards) {
        List<Integer> array = new ArrayList<>();
        cards.forEach(e -> array.add(getCardValue(e)));
        Collections.sort(array);
        if (specialCards == 5) {
            return setData(CardType.duiziniu.setHandCard(new ArrayList<>(Ints.asList(12, 13, 13, 13, 13))));
        } else if (specialCards == 4) {
            int card = getCardPoint(array.get(0));
            if (array.get(0) == 13) {
                return setData(CardType.duiziniu.setHandCard(new ArrayList<>(Ints.asList(12, 13, 13, 13, 13))));
            } else {
                int num = 10 - card;
                if (num == 0) {
                    num = 13;
                }
                return setData(CardType.duiziniu.setHandCard(new ArrayList<>(Ints.asList(array.get(0), num, 13, 13, 13))));
            }
        } else if (specialCards == 3) {
            int cardA = getCardPoint(array.get(0));
            int cardB = getCardPoint(array.get(1));
            int point = 10 - ((cardA + cardB) % 10);
            if (point == 0) {
                if (cardA == 13 && cardB == 13) {
                    return setData(CardType.duiziniu.setHandCard(new ArrayList<>(Ints.asList(12, 13, 13, 13, 13))));
                } else {
                    return setData(CardType.duiziniu.setHandCard(new ArrayList<>(Ints.asList(array.get(0), array.get(1), 13, 13, 13))));
                }
            } else {
                return setData(CardType.duiziniu.setHandCard(new ArrayList<>(Ints.asList(array.get(0), array.get(1), point, 13, 13))));
            }
        } else if (specialCards == 2) {
            int cardA = getCardPoint(array.get(0));
            int cardB = getCardPoint(array.get(1));
            int cardC = getCardPoint(array.get(2));
            int point = 10 - ((cardA + cardB + cardC) % 10);
            if (point == 0) {
                return setData(CardType.duiziniu.setHandCard(new ArrayList<>(Ints.asList(array.get(0), array.get(1), array.get(2), 13, 13))));
            } else {
                return setData(CardType.duiziniu.setHandCard(new ArrayList<>(Ints.asList(array.get(0), array.get(1), point, array.get(2), array.get(2)))));
            }
        } else {
            if (specialCards == 1) {
                for (int i = 13; i > 0; i--) {
                    List<Integer> list = new ArrayList<>(array);
                    list.add(i);
                    if (checkDuiZi(list)) {
                        return setData(CardType.duiziniu.setHandCard(list));
                    }
                }
            } else {
                if (checkDuiZi(array)) {
                    return setData(CardType.duiziniu.setHandCard(array));
                }
            }
        }
        return false;
    }

    private static boolean checkDuiZi(List<Integer> array) {
        List<Integer> list = array.stream() // list 对应的 Stream
                .collect(Collectors.toMap(e -> e, e -> 1, (a, b) -> a + b)) // 获得元素出现频率的 Map，键为元素，值为元素出现的次数
                .entrySet().stream() // 所有 entry 对应的 Stream
                .filter(entry -> entry.getValue() > 1) // 过滤出元素出现次数大于 1 的 entry
                .map(entry -> entry.getKey()) // 获得 entry 的键（重复元素）对应的 Stream
                .collect(Collectors.toList());
        if (list.size() != 0) {
            int sum = 0;
            int count = 0;//AAA45 count统计了两张牌后剩余的牌都相加
            for (int card : array) {
                if (card != list.get(list.size() - 1) || count == 2) {
                    sum += getCardPoint(card);
                } else {
                    count++;
                }
            }
            if (sum % 10 == 0) {
                return true;
            }
        }
        return false;
    }


    private static int getCardValue(byte card) {
        return card % 13 == 0 ? 13 : card % 13;
    }

    private static int getCardPoint(int card) {
        return card > 10 ? 10 : card;
    }

    /***
     * 获取普通牛牛牌型
     * @param list 找出的三张牌的集合
     * @param all 手中的五张牌
     * @return
     */
    private static CardType getNomalCardType(List<List<Byte>> list, List<Byte> all) {
        for (List<Byte> l : list) {
            int sum = 0;
            for (Byte b : l) {
//                int value = CardDef.getCardScoreValue(b);
                int value = getCardPoint(getCardValue(b));
                sum += value;
            }
            if (sum % 10 == 0) {
                //有牛
                List<Byte> temp = new ArrayList<>(all);
                for (Byte b : l) { //此处删除过后只剩两张牌
                    temp.remove(b);
                }
                int ct = 0;
                for (Byte b : temp) {
//                    int value = CardDef.getCardScoreValue(b);
                    int value = getCardPoint(getCardValue(b));
                    ct += value;
                }
                int result = ct % 10;
                switch (result) {
                    case 0:
                        return CardType.niuniu;
                    case 1:
                        return CardType.niuyi;
                    case 2:
                        return CardType.niuer;
                    case 3:
                        return CardType.niusan;
                    case 4:
                        return CardType.niusi;
                    case 5:
                        return CardType.niuwu;
                    case 6:
                        return CardType.niuliu;
                    case 7:
                        return CardType.niuqi;
                    case 8:
                        return CardType.niuba;
                    case 9:
                        return CardType.niujiu;
                    default:
                        return CardType.wuniu;
                }
            }
        }
        return CardType.wuniu;
    }

    private static void findOnce(FindNode parent, List<List<Byte>> all) {
        if (parent.getRemain().size() <= 2) { //数量小于2，不用再寻找了
            List<Byte> findList = new ArrayList<>();
            findList.add(parent.getFindCard());
            while (parent.getParent() != null) {
                parent = parent.getParent();
                findList.add(parent.getFindCard());
            }
            all.add(findList);
            return;
        }
        List<Byte> list = parent.getRemain();
        for (Byte b : list) {
            FindNode fn = new FindNode();
            fn.setFindCard(b);
            List<Byte> remain = new ArrayList<>(list);
            remain.remove(b);
            fn.setRemain(remain);
            fn.setParent(parent);
            findOnce(fn, all);
        }
    }

    /***
     * 从一组五张牌中，寻找出所有三张牌的集合
     * @param list
     * @return
     */
    private static List<List<Byte>> find(List<Byte> list) {
        if (list.size() != 5) { //5张牌寻三张
            return null;
        }
        List<List<Byte>> all = new ArrayList<>();
        for (Byte b : list) {
            FindNode node = new FindNode();
            node.setFindCard(b);
            List<Byte> remain = new ArrayList<>(list);
            remain.remove(b);
            node.setRemain(remain);
            findOnce(node, all);
        }
        //过滤算法
        List<List<Byte>> find = new ArrayList<>();
        for (List<Byte> l : all) {
            if (find.size() == 0) {
                find.add(l);
            } else {
                if (!containList(find, l)) {
                    find.add(l);
                }
            }
        }
        return find;
    }

    private static boolean containList(List<List<Byte>> list, List<Byte> check) {
        for (List<Byte> l : list) {
            if (sameList(l, check)) {
                return true;
            }
        }
        return false;
    }

    private static boolean sameList(List<Byte> l1, List<Byte> l2) {
        if (l1.size() != l2.size()) {
            return false;
        }
        Collections.sort(l1);
        Collections.sort(l2);
        for (int i = 0; i < l1.size(); i++) {
            if (l1.get(i) != l2.get(i)) {
                return false;
            }
        }
        return true;
    }
}
