package com.jule.domino.game.model;

import com.google.common.primitives.Ints;
import com.jule.domino.game.utils.CombineUtil;
import com.jule.domino.game.utils.FindNode;
import com.jule.domino.game.utils.NumUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
@Slf4j
public class NiuNiuPoker {
    private String typeName; //牌型类型名
    private int typeCompareValue; //牌型比较大小时所用的int值。（值越大代表牌型越大）
    private boolean haveSpecialCard;//玩家有无癞子牌
    private List<Integer> handCard;//去除掉癞子牌的手牌
    private List<Integer> cards;//计算完癞子的牌

    private static byte changeableCard = -1;//demo

    public NiuNiuPoker(List<Byte> cards, List<Integer> wanfa) {
        synchronized (this) {
            List<Integer> array = new ArrayList<>();
            Iterator<Byte> iter = cards.iterator();
            int specialCards = 0;
            while (iter.hasNext()) {
                byte card = iter.next();
                if (getCardValue(card) == getCardValue(changeableCard) || card == 53 || card == 54) {
                    iter.remove();
                    specialCards++;
                } else {
                    array.add((int) card);
                }
            }
            handCard = array;
            haveSpecialCard = (specialCards != 0);
//        log.info("玩家原来的牌：{}", cards.toString());
//        log.info("去掉癞子之后的牌：{}，  癞子：{}", array.toString(), specialCards);
            if (wanfa.contains(8) && shunJin(cards, specialCards)) {
                this.cards = setCardColor(this.cards, handCard);
                return;
            }
            if (wanfa.contains(7) && wuXiao(cards, specialCards)) {
                this.cards = setCardColor(this.cards, handCard);
                return;
            }
            if (wanfa.contains(6) && zhaDan(cards, specialCards)) {
                this.cards = setCardColor(this.cards, handCard);
                return;
            }
            if (wanfa.contains(5) && huLu(cards, specialCards)) {
                this.cards = setCardColor(this.cards, handCard);
                return;
            }
            if (wanfa.contains(4) && tongHua(cards, specialCards)) {
                this.cards = setCardColor(this.cards, handCard);
                return;
            }
            if (wanfa.contains(3) && wuHua(cards, specialCards)) {
                this.cards = setCardColor(this.cards, handCard);
                return;
            }
            if (wanfa.contains(2) && shunZi(cards, specialCards)) {
                this.cards = setCardColor(this.cards, handCard);
                return;
            }
            if (wanfa.contains(1) && duiZi(cards, specialCards)) {
                this.cards = setCardColor(this.cards, handCard);
                return;
            }
            //计算牛牛牌型
            niuNiu(cards, specialCards);
        }
        return;
    }

    //计算癞子牛牛牌型
    private void niuNiu(List<Byte> cards, int specialCards) {
        List<Integer> listA = new ArrayList<>();
        cards.forEach(c -> listA.add((int) c));
        if (specialCards == 0) {
            niuNiu(cards);
        } else if (specialCards == 1) {
            Map<Integer, CardType> map = new HashMap<>();
            for (int i = 1; i <= 13; i++) {
                List<Byte> array = new ArrayList<>(cards);
                List<Integer> listB = new ArrayList<>(Ints.asList(i, i + 13, i + 26, i + 39));
                listB.removeAll(listA);
                int card = listB.get(0);
                array.add((byte) card);
                CardType cardType = niuNiu(array);
                map.put(cardType.getType(), cardType);
            }
            int maxCardType = Collections.max(map.keySet());
            setData(map.get(maxCardType));
        } else if (specialCards == 2) {
            Map<Integer, CardType> map = new HashMap<>();
            for (int i = 1; i <= 13; i++) {
                for (int j = 1; j <= 13; j++) {
                    List<Byte> array = new ArrayList<>(cards);
                    List<Integer> listB = new ArrayList<>(Ints.asList(i, i + 13, i + 26, i + 39));
                    List<Integer> listC = new ArrayList<>(Ints.asList(j, j + 13, j + 26, j + 39));
                    listB.removeAll(listA);
                    listC.removeAll(listA);
                    int cardB = listB.get(0);
                    int cardC = listC.get(0);
                    array.add((byte) cardB);
                    array.add((byte) cardC);
                    CardType cardType = niuNiu(array);
                    map.put(cardType.getType(), cardType);
                }
            }
            int maxCardType = Collections.max(map.keySet());
            setData(map.get(maxCardType));
        } else {
            log.error("玩家拥有 {} 张癞子牌，并且没有选特殊牌型");
        }
    }

    //计算牛牛牌型
    private CardType niuNiu(List<Byte> cards) {
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
        CardType cardType = getNomalCardType(find, cards);
        setData(cardType);
        return cardType;
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
//                  本局癞子牌为：-------------------------48
//                  -----------------------------玩家手牌-------------------------
//                  玩家A：[43, 49, 21, 9, 48]--♦4, ♦T, ♥8, ♠9, ♦9,
//                  玩家B：[19, 23, 1, 48, 48]--♥6, ♥T, ♠A, ♦9, ♦9,
//                  -----------------------------计算之后-------------------------
//                  玩家 A 牌型 -> 对子牛, 手牌：[43, 49, 21, 8, 10]--♦4, ♦T, ♥8, ♠8, ♠T,
//                  玩家 B 牌型 -> 对子牛, 手牌：[19, 23, 1, 3, 10]--♥6, ♥T, ♠A, ♠3, ♠T,
//                  -----------------------------对比结果-------------------------
//                  玩家 B 赢了！ 对子牌先比值，再比这对中的最大花色，再来就是比手上最大单牌
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
                            List<Integer> cardA = getSortCard(this);
                            List<Integer> cardB = getSortCard(niuPoker);
                            for (int i = cardA.size() - 1; i >= 0; i--) {
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
                            System.out.println("对子：一样牌型，一样的最大牌");
                        }
                    }
                } else {
                    List<Integer> cardA = getSortCard(this);
                    List<Integer> cardB = getSortCard(niuPoker);
                    List<Integer> maxShunZi = Ints.asList(1, 10, 11, 12, 13);
                    if (cardA.containsAll(maxShunZi) && !cardB.containsAll(maxShunZi)) {
                        return 1;
                    } else if (!cardA.containsAll(maxShunZi) && cardB.containsAll(maxShunZi)) {
                        return -1;
                    } else {
                        for (int i = cardA.size() - 1; i >= 0; i--) {
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
        List<Integer> array = new ArrayList<>();
        niuPoker.cards.forEach(card -> array.add(getCardValue(card)));
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
    private List<Integer> setCardColor(List<Integer> list, List<Integer> cards) {
        if (cards.size() == 5) return cards;//五张癞子牌时不需要改变花色
        //五小和炸弹在判断牌型时已经设置过花色了
        if (cards.size() == 0 || typeCompareValue == CardType.wuxiao.getType() || typeCompareValue == CardType.zhadan.getType())
            return list;
        for (int card : cards) { //去掉相同的牌
            int cardValue = getCardValue(card);
            for (int i = 0; i < list.size(); i++) { //删除相同的元素 一次只删除一个
                if (cardValue == list.get(i)) {
                    list.remove(i);
                    break;
                }
            }
        }
        List<Integer> array = new ArrayList<>(list);
        log.info("当前玩家有 {} 张牌需要改变花色", array.size());
        for (int card : array) { //计算完癞子牌后 所有牌都是最大花色 需要改变
            if (typeCompareValue == CardType.shunjin.getType()) {
                int color = getCardColor(cards.get(0));
                cards.add(card + (color * 13));
            } else if (typeCompareValue == CardType.hulu.getType()) {
                List<Integer> color = new ArrayList<>(Ints.asList(card, card + 13, card + 26, card + 39));
                color.removeAll(cards);
                cards.add(color.get(0));
            } else if (typeCompareValue == CardType.tonghua.getType()) {
                int color = getCardColor(cards.get(0));
                cards.add(card + (color * 13));
            } else if (typeCompareValue == CardType.wuhua.getType()) {
                List<Integer> color = new ArrayList<>(Ints.asList(card, card + 13, card + 26, card + 39));
                color.removeAll(cards);
                cards.add(color.get(0));
            } else if (typeCompareValue == CardType.shunzi.getType()) {
                List<Integer> color = new ArrayList<>(Ints.asList(card, card + 13, card + 26, card + 39));
                color.removeAll(cards);
                cards.add(color.get(0));
            } else if (typeCompareValue == CardType.duiziniu.getType()) {
                List<Integer> color = new ArrayList<>(Ints.asList(card, card + 13, card + 26, card + 39));
                color.removeAll(cards);
                cards.add(color.get(0));
            }
//            else {
//                List<Integer> color = new ArrayList<>(Ints.asList(card, card + 13, card + 26, card + 39));
//                color.removeAll(cards);
//                cards.add(color.get(0));
//            }
        }
        return cards;
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
        List<Integer> list = new ArrayList<>();
        cards.forEach(e -> {
            array.add(getCardValue(e));
            list.add((int) e);
        });
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
            if (specialCards > 0) {
                cardsB.addAll(array);
                return setData(CardType.shunzi.setHandCard(cardsB));
            } else {
                return setData(CardType.shunzi.setHandCard(list));
            }
        }
        return false;
    }

    private boolean tongHua(List<Byte> cards, int specialCards) {
        List<Integer> array = new ArrayList<>();
        List<Integer> list = new ArrayList<>();
        cards.forEach(e -> {
            array.add(getCardValue(e));
            list.add((int) e);
        });
        Collections.sort(array);
        if (cards.size() != 0) {
            int color = getCardColor(cards.get(0));
            for (byte card : cards) {
                if (getCardColor(card) != color) {
                    return false;
                }
            }
        }
        int in = specialCards;
        if (in > 0) {
            for (int i = 13; i > 0; i--) {
                if (!array.contains(i)) {
                    array.add(i);
                    in--;
                } else {
                    continue;
                }
                if (in == 0) {
                    break;
                }
                Collections.sort(array);
            }
        }
        if (specialCards > 0) {
            return setData(CardType.tonghua.setHandCard(array));
        } else {
            return setData(CardType.tonghua.setHandCard(list));
        }
    }

    private boolean wuXiao(List<Byte> cards, int specialCards) {
        List<Integer> array = new ArrayList<>();
        List<Integer> listA = new ArrayList<>();
        cards.forEach(e -> {
            array.add(getCardValue(e));
            listA.add((int) e);
        });
        int sum = 0;
        for (int card : cards) {
            sum += getCardPoint(card);
        }
        if ((array.size() != 0 && Collections.max(array) >= 5) || sum >= 10) {
            return false;
        }
        int count = 9 - sum;
        List<Integer> listB = new ArrayList<>(Ints.asList(1, 14, 27, 40));
        List<Integer> listC = new ArrayList<>(Ints.asList(2, 15, 28, 41));
        listB.removeAll(listA);
        listC.removeAll(listA);
        if (specialCards == 5) {
            return setData(CardType.wuxiao.setHandCard(new ArrayList<>(Ints.asList(1, 14, 27, 2, 4))));
        } else if (specialCards == 4 || specialCards == 3) {
            if (array.contains(3)) {
                return setData(CardType.wuxiao.setHandCard(new ArrayList<>(Ints.asList(1, 14, 2, 15, cards.get(0)))));
            } else {
                return setData(CardType.wuxiao.setHandCard(new ArrayList<>(Ints.asList(1, 14, 27, 2, cards.get(0)))));
            }
        } else if (specialCards == 2) {
            if (!array.contains(4) && count > 5) {
                listA.add(4);
                listA.add(listB.get(0));
            } else if (!array.contains(3) && count > 4) {
                listA.add(3);
                listA.add(listB.get(0));
            } else {
                listA.add(listB.get(0));
                listA.add(listB.get(1));
            }
            return setData(CardType.wuxiao.setHandCard(listA));
        } else if (specialCards == 1) {
            if (count >= 4) {
                listA.add(4);
            } else if (count >= 3) {
                listA.add(3);
            } else if (count == 2) {
                listA.add(listC.get(0));
            } else {
                listA.add(listB.get(0));
            }
        }
        return setData(CardType.wuxiao.setHandCard(listA));
    }

    private boolean zhaDan(List<Byte> cards, int specialCards) {
        List<Integer> array = new ArrayList<>();
        List<Integer> list = new ArrayList<>();
        cards.forEach(e -> {
            array.add(getCardValue(e));
            list.add((int) e);
        });
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
        if (specialCards >= 1) {
            if (specialCards >= 4) {
                if (array.size() == 0 || array.get(0) >= 12) {
                    return setData(CardType.zhadan.setHandCard(new ArrayList<>(Ints.asList(12, 13, 26, 39, 52))));
                } else {
                    return setData(CardType.zhadan.setHandCard(new ArrayList<>(Ints.asList(array.get(0), 13, 26, 39, 52))));
                }
            } else {
                if (array.get(0) != array.get(array.size() - 1)) {
                    int card = Collections.max(array) == template ? array.get(0) : Collections.max(array);
                    return setData(CardType.zhadan.setHandCard(new ArrayList<>(Ints.asList(card, template, template + 13, template + 26, template + 39))));
                } else {
                    int card = array.get(0);
                    return setData(CardType.zhadan.setHandCard(new ArrayList<>(Ints.asList(card, card + 13, card + 26, card + 39, 13))));
                }
            }
        } else { //没有癞子牌
            return setData(CardType.zhadan.setHandCard(list));
        }
    }

    private boolean huLu(List<Byte> cards, int specialCards) {
        List<Integer> array = new ArrayList<>();
        List<Integer> list = new ArrayList<>();
        cards.forEach(e -> {
            array.add(getCardValue(e));
            list.add((int) e);
        });
        Collections.sort(array);
        Set<Integer> set = new HashSet<>(array);
        if (set.size() > 2 || (cards.size() >= 4 && set.size() != 2)) {//后面的条件是防止玩家拥有4张一样的牌
            return false;
        }
        if (specialCards >= 1) {
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
        } else {
            return setData(CardType.hulu.setHandCard(list));
        }
    }

    private boolean wuHua(List<Byte> cards, int specialCards) {
        List<Integer> array = new ArrayList<>();
        List<Integer> list = new ArrayList<>();
        cards.forEach(e -> {
            array.add(getCardValue(e));
            list.add((int) e);
        });
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
        if (specialCards >= 1) {
            return setData(CardType.wuhua.setHandCard(array));
        } else {
            return setData(CardType.wuhua.setHandCard(list));
        }
    }

    private boolean duiZi(List<Byte> cards, int specialCards) {
        List<Integer> array = new ArrayList<>();
        List<Integer> listA = new ArrayList<>();
        cards.forEach(e -> {
            array.add(getCardValue(e));
            listA.add((int) e);
        });
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
            int point = 10 - ((cardA + cardB) % 10);
            if (point == 0) {
                return setData(CardType.duiziniu.setHandCard(new ArrayList<>(Ints.asList(array.get(0), array.get(1), array.get(2), 13, 13))));
            } else {
                return setData(CardType.duiziniu.setHandCard(new ArrayList<>(Ints.asList(array.get(0), array.get(1), point, array.get(2), array.get(2)))));
            }
        } else if (specialCards == 1) {
            for (int i = 13; i > 0; i--) {
                List<Integer> list = new ArrayList<>(array);
                list.add(i);
                if (checkDuiZi(list)) {
                    return setData(CardType.duiziniu.setHandCard(list));
                }
            }
        } else {
            if (checkDuiZi(array)) {
                return setData(CardType.duiziniu.setHandCard(listA));
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


    private static int getCardValue(int card) {
        return card % 13 == 0 ? 13 : card % 13;
    }

    private static int getCardValue(byte card) {
        return card % 13 == 0 ? 13 : card % 13;
    }

    private static int getCardColor(int card) {
        if (card == 13) return 0;
        if (card == 26) return 1;
        if (card == 39) return 2;
        if (card == 52) return 3;
        return card / 13;
    }

    private static int getCardColor(byte card) {
        if (card == 13) return 0;
        if (card == 26) return 1;
        if (card == 39) return 2;
        if (card == 52) return 3;
        return card / 13;
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
    private CardType getNomalCardType(List<List<Byte>> list, List<Byte> all) {
        List<Integer> array = new ArrayList<>();
        all.forEach(c -> array.add((int) c));
        Collections.sort(array);
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
                array.clear();//有牛时为手牌排序，组成牛牛的三张牌在前面
                Collections.sort(l);
                Collections.sort(temp);
                l.forEach(c -> array.add((int) c));
                temp.forEach(c -> array.add((int) c));
                int result = ct % 10;
                switch (result) {
                    case 0:
                        return CardType.niuniu.setHandCard(array);
                    case 1:
                        return CardType.niuyi.setHandCard(array);
                    case 2:
                        return CardType.niuer.setHandCard(array);
                    case 3:
                        return CardType.niusan.setHandCard(array);
                    case 4:
                        return CardType.niusi.setHandCard(array);
                    case 5:
                        return CardType.niuwu.setHandCard(array);
                    case 6:
                        return CardType.niuliu.setHandCard(array);
                    case 7:
                        return CardType.niuqi.setHandCard(array);
                    case 8:
                        return CardType.niuba.setHandCard(array);
                    case 9:
                        return CardType.niujiu.setHandCard(array);
                    default:
                        return CardType.wuniu.setHandCard(array);
                }
            }
        }
        return CardType.wuniu.setHandCard(array);
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
