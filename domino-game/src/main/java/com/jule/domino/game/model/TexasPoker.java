package com.jule.domino.game.model;

import com.google.common.primitives.Ints;
import com.jule.domino.game.service.holder.CardValueHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 手中牌型Model类
 */
public class TexasPoker {

    private final static Logger logger = LoggerFactory.getLogger(TexasPoker.class);

    public final static String TYPE_STRAIGHT_FLUSH = "StraightFlush";//同花顺
    public final static String TYPE_IRON_BRANCH = "IronBranch";//铁支
    public final static String TYPE_GOURD = "Gourd";//葫芦
    public final static String TYPE_FLUSH = "Flush";//同花
    public final static String TYPE_STRAIGHT = "Straight";//顺子
    public final static String TYPE_THREE_ARTICLE = "ThreeArticle";//三条
    public final static String TYPE_TWO_PAIR = "TwoPair";//两对
    public final static String TYPE_ONE_PAIR = "OnePair";//一对
    public final static String TYPE_SCATTERED_CARDS = "ScatteredCards";//散牌

    private String typeName; //牌型类型名
    //1.散牌 2.一对 3.两对 4.三条 5.顺子 6.同花 7.葫芦 8.铁支 9.同花顺
    private int typeCompareValue; //牌型比较大小时所用的int值。（值越大代表牌型越大）
    //五张手牌
    private CardValueModel card1;
    private CardValueModel card2;
    private CardValueModel card3;
    private CardValueModel card4;
    private CardValueModel card5;
    //牌面去重
    private Set<Integer> compareValueSet = new HashSet();
    //花色去重
    private Set<Character> cardColorSet = new HashSet();
    //按升序存放五张牌牌面值
    private Integer[] sortCards;
    //存储牌面值，相同牌型时用此值做比较
    private Integer[] compareCards = new Integer[0];
    //最大单牌 or 最大对子，方便花色比较
    private CardValueModel maxCard;

    /**
     * 结算牌型大小构造器
     */
    public TexasPoker(int[] cards) {
//        int[] cards = player.getHandCards();
        this.card1 = CardValueHolder.getCardValueModel(cards[0]);
        this.card2 = CardValueHolder.getCardValueModel(cards[1]);
        this.card3 = CardValueHolder.getCardValueModel(cards[2]);
        this.card4 = CardValueHolder.getCardValueModel(cards[3]);
        this.card5 = CardValueHolder.getCardValueModel(cards[4]);
        //去掉compareValue的重复值
        compareValueSet.add(card1.getCompareValue());
        compareValueSet.add(card2.getCompareValue());
        compareValueSet.add(card3.getCompareValue());
        compareValueSet.add(card4.getCompareValue());
        compareValueSet.add(card5.getCompareValue());
        //去掉cardColor的重复值
        cardColorSet.add(card1.getCardColor());
        cardColorSet.add(card2.getCardColor());
        cardColorSet.add(card3.getCardColor());
        cardColorSet.add(card4.getCardColor());
        cardColorSet.add(card5.getCardColor());
        //存储手牌
        sortCards = new Integer[]{card1.getCompareValue(), card2.getCompareValue(), card3.getCompareValue(), card4.getCompareValue(), card5.getCompareValue()};
        Arrays.sort(sortCards);//为手牌排序
        checkCardType();//检查玩家牌型
        getMaxCard();//获取比较花色的牌（铁支，葫芦，三条不需要）
    }

    /**
     * 检查玩家牌型
     */
    private void checkCardType() {
        //铁支 or 葫芦
        if (compareValueSet.size() == 2) {
            if (sortCards[1] == sortCards[3]) {
                this.typeCompareValue = 8;
                this.typeName = TYPE_IRON_BRANCH;
            } else {
                this.typeCompareValue = 7;
                this.typeName = TYPE_GOURD;
            }
            this.compareCards = new Integer[]{sortCards[2]};
        }
        //三条 or 两对
        if (compareValueSet.size() == 3) {
            if (sortCards[0] == sortCards[2] || sortCards[1] == sortCards[3] || sortCards[2] == sortCards[4]) {
                this.typeCompareValue = 4;
                this.typeName = TYPE_THREE_ARTICLE;
                this.compareCards = new Integer[]{sortCards[2]};
            } else {
                this.typeCompareValue = 3;
                this.typeName = TYPE_TWO_PAIR;
                this.compareCards = getAllPair();
            }
        }
        //一对
        if (compareValueSet.size() == 4) {
            this.typeCompareValue = 2;
            this.typeName = TYPE_ONE_PAIR;
            this.compareCards = getAllPair();
        }
        //同花顺 or 同花 or 顺子 or 散牌
        if (compareValueSet.size() == 5) {
            //同花顺 or 顺子
            if (sortCards[0] == sortCards[4] - 4) {
                if (cardColorSet.size() == 1) {
                    this.typeCompareValue = 9;
                    this.typeName = TYPE_STRAIGHT_FLUSH;
                } else {
                    this.typeCompareValue = 5;
                    this.typeName = TYPE_STRAIGHT;
                }
                this.compareCards = new Integer[]{sortCards[4]};
            } else {
                //同花 or 散牌
                if (cardColorSet.size() == 1) {
                    this.typeCompareValue = 6;
                    this.typeName = TYPE_FLUSH;
                    this.compareCards = new Integer[]{sortCards[4]};
                } else {
                    this.typeCompareValue = 1;
                    this.typeName = TYPE_SCATTERED_CARDS;
                    this.compareCards = sortCards;
                }
            }
        }
    }

    private void getMaxCard() {
        if (TYPE_IRON_BRANCH.equals(typeName) || TYPE_GOURD.equals(typeName) || TYPE_THREE_ARTICLE.equals(typeName)) {
            return;
        }
        List<CardValueModel> cards = Arrays.asList(card1, card2, card3, card4, card5);
        List<Integer> pair = new ArrayList<>(Arrays.asList(card1.getCompareValue(), card2.getCompareValue(), card3.getCompareValue(), card4.getCompareValue(), card5.getCompareValue()));
        CardValueModel maxCard = card1;
        for (Integer in : compareValueSet) {
            pair.remove(in);
        }
        if (pair.size() != 0) {
            maxCard = null;
            Collections.sort(pair);
            for (int i = 0; i < cards.size(); i++) {
                if (pair.get(pair.size() - 1) == cards.get(i).getCompareValue()) {
                    if (maxCard != null) {
                        if (cards.get(i).getCardId() < maxCard.getCardId()) {
                            maxCard = cards.get(i);
                        }
                    } else {
                        maxCard = cards.get(i);
                    }
                }
            }
        } else {
            for (int i = 0; i < cards.size(); i++) {
                if (maxCard.getCompareValue() < cards.get(i).getCompareValue()) {
                    maxCard = cards.get(i);
                }
            }
        }
        this.maxCard = maxCard;
    }

    private Integer[] getAllPair() {
        List<Integer> list = new ArrayList<>();
        for (int cardSet : compareValueSet) {
            int in = 0;
            for (int card : sortCards) {
                if (card == cardSet) {
                    in++;
                }
            }
            if (in == 2) {
                list.add(cardSet);
            }
        }
        Integer[] pairCard = new Integer[list.size()];
        for (int i = 0; i < list.size(); i++) {
            pairCard[i] = list.get(i);
        }
        return pairCard;
    }

    /**
     * 比较手牌牌型的大小：
     * 与参数相等返回 0
     * 小于参数返回 -1
     * 大于参数返回 1
     *
     * @param model
     * @return
     */
    public int compareTo(TexasPoker model) {
        if (this.typeCompareValue > model.typeCompareValue) {
            return 1;
        } else if (this.typeCompareValue < model.typeCompareValue) {
            return -1;
        } else if (this.typeCompareValue == model.typeCompareValue) {
            //当牌型相同，比较牌面值大小
            logger.info("当前的牌型：{}，需要比 {} 张牌（为0则是bug）", typeName, compareCards.length);
            for (int i = compareCards.length - 1; i >= 0; i--) {
                if (this.compareCards[i] > model.compareCards[i]) {
                    return 1;
                } else if (this.compareCards[i] < model.compareCards[i]) {
                    return -1;
                }
            }
            logger.debug("当前牌型，牌面值都一样大，比较最大牌面值的花色(id越大花色越小)" + maxCard.getCardId() + " : " + model.maxCard.getCardId());
            if (maxCard.getCardId() < model.maxCard.getCardId()) {//牌的id越大 花色越小
                return 1;
            } else {
                return -1;
            }
        }
        return 0;
    }

    public int getTypeCompareValue() {
        return typeCompareValue;
    }

    public String toString() {
        return "handCard=" + card1.toString() + " " + card2.toString() + " " + card3.toString() + " " + card4.toString() + " " + card5.toString()
                + ", typeName=" + typeName
                + ", typeCompareValue=" + typeCompareValue
                + ", compareCards=" + Arrays.asList(compareCards).toString()
                + ", maxCard=" + (maxCard != null ? maxCard.toString() : null)
                + ", maxCardId=" + (maxCard != null ? maxCard.getCardId() : null);
    }
}
