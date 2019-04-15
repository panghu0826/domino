package com.jule.robot.model;

import com.google.common.primitives.Ints;
import com.jule.robot.service.holder.CardValueHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.*;

/**
 * 手中牌型Model类
 */
public class HandCardTypeModel {

    private final static Logger logger = LoggerFactory.getLogger(HandCardTypeModel.class);

    public final static String TYPE_JOKER_MAX = "JokerMax";
    public final static String TYPE_SET = "Set";
    public final static String TYPE_JOKER_SET = "JokerSet";
    public final static String TYPE_STRAIGHT_FLUSH = "StraightFlush";
    public final static String TYPE_JOKER_STRAIGHT_FLUSH = "JokerStraightFlush";
    public final static String TYPE_STRAIGHT = "Straight";
    public final static String TYPE_JOKER_STRAIGHT = "JokerStraight";
    public final static String TYPE_FLUSH = "Flush";
    public final static String TYPE_JOKER_FLUSH = "JokerFlush";
    public final static String TYPE_PAIR = "Pair";
    public final static String TYPE_JOKER_PAIR = "JokerPair";
    public final static String TYPE_HIGH_CARD = "HighCard";

    private String typeName; //牌型类型名
    private int typeCompareValue; //牌型比较大小时所用的int值。（值越大代表牌型越大）
    private int maxCardCompareValue; //牌型比较时，如遇到牌型一致，那么使用此值做比较。（值越大代表牌型越大）
    private int secondCardCompareValue; //牌型比较时，如遇到最大牌型一致，那么使用此值做比较。（值越大代表牌型越大）
    private int thirdCardCompareValue; //牌型比较时，如遇到最大牌型和第二大牌型一致，那么使用此值做比较。（值越大代表牌型越大）
    //三张手牌
    private CardValueModel card1;
    private CardValueModel card2;
    private CardValueModel card3;
    //用于判断牌型的去重Set
    private Set compareValueSet = new HashSet(); //用来去掉牌型的重复值，使程序更容易判断 set和一对 牌型
    private Set cardColorSet = new HashSet(); //用来去掉牌型的重复值，使程序更容易判断 同花 牌型
    //用于获取最大值的Integer数组
    private Integer[] sortArrCardCompare = new Integer[3]; //存放三张牌的大小比较值
    //用于获取癞子牌最大值的Integer数组
    private Integer[] sortLaizigou = new Integer[2]; //存放三张牌的大小比较值
    //用于存储玩家改变过的牌型(癞子牌)
    private int[] HandCards = new int[3];
    //是否是 A23 的顺子
    private boolean isStraightA23 = false;
    //是否是 AKQ 的顺子
    private boolean isStraightAKQ = false;
    //用来随机万能牌的color
    private List<Character> list = new ArrayList(Arrays.asList('♠','♥','♣','♦'));
    //玩家拥有的癞子牌数量
    private PlayerInfo playerInfo;

    /**
     * 无癞子牌构造器
     * @param card1
     * @param card2
     * @param card3
     */
    public HandCardTypeModel(int card1, int card2, int card3){
        this.card1 = CardValueHolder.getCardValueModel(card1);
        this.card2 = CardValueHolder.getCardValueModel(card2);
        this.card3 = CardValueHolder.getCardValueModel(card3);
        init();
    }

    /**
     * 无癞子牌构造器
     * @param card1
     * @param card2
     * @param card3
     */
    public HandCardTypeModel(CardValueModel card1, CardValueModel card2, CardValueModel card3){
        this.card1 = card1;
        this.card2 = card2;
        this.card3 = card3;
        init();
    }

    public static HandCardTypeModel getInstance(int card1, int card2, int gameId){
        PlayerInfo playerInfo = new PlayerInfo();
        int[] arrCard = new int[2];
        arrCard[0] = card1;
        arrCard[1] = card2;
        playerInfo.setHandCards(arrCard);

        return playerInfo.getHandCardTypeModel(gameId);
    }

    /**
     * 有癞子牌构造器
     * @param playerInfo
     */
    public HandCardTypeModel(PlayerInfo playerInfo){
        logger.debug("MixedCard = "+playerInfo.getMixedCard()+", playerInfo.getHandCards()->"+playerInfo.getHandCards().length);
        for(int handCard : playerInfo.getHandCards()){
            logger.debug("handCard = "+handCard);
        }
        this.playerInfo = playerInfo;
        if(playerInfo.getMixedCard() == 3){ //三张癞子时牌值设置为三张A，但牌型设置为最大
            this.card1 = CardValueHolder.getCardValueModel(1);
            this.card2 = CardValueHolder.getCardValueModel(14);
        }else if(playerInfo.getMixedCard() == 2){
            this.card1 = CardValueHolder.getCardValueModel(playerInfo.getHandCards()[0]);
            //此处的判断是让癞子没有相同的花色
            if(playerInfo.getHandCards()[0] >= 26){
                this.card2 = CardValueHolder.getCardValueModel(playerInfo.getHandCards()[0] - 13);
            }else{
                this.card2 = CardValueHolder.getCardValueModel(playerInfo.getHandCards()[0] + 13);
            }
        }else{
            this.card1 = CardValueHolder.getCardValueModel(playerInfo.getHandCards()[0]);
            this.card2 = CardValueHolder.getCardValueModel(playerInfo.getHandCards()[1]);
        }
        initJoker(playerInfo.getMixedCard());
    }

//    /**
//     * 有癞子牌构造器
//     * @param card1
//     * @param card2
//     */
//    public HandCardTypeModel(CardValueModel card1, CardValueModel card2) {
//        this.card1 = card1;
//        this.card2 = card2;
//        initJoker();
//    }

    private void init(){
        //用set去掉compareValue的重复值
        compareValueSet.add(card1.getCompareValue());
        compareValueSet.add(card2.getCompareValue());
        compareValueSet.add(card3.getCompareValue());

        //用set去掉cardColor的重复值
        cardColorSet.add(card1.getCardColor());
        cardColorSet.add(card2.getCardColor());
        cardColorSet.add(card3.getCardColor());

        //存储三张牌的比较值，方便后续获取最大牌
        sortArrCardCompare[0] = card1.getCompareValue();
        sortArrCardCompare[1] = card2.getCompareValue();
        sortArrCardCompare[2] = card3.getCompareValue();
        Arrays.sort(sortArrCardCompare);

        common(0);
    }

    private void initJoker(int mixedCardNum) {
        //用set去掉compareValue的重复值
        logger.debug("card1 is null,"+ (card1 == null)+", card2 is null,"+ (card2 == null));
        compareValueSet.add(card1.getCompareValue());
        compareValueSet.add(card2.getCompareValue());

        //用set去掉cardColor的重复值
        cardColorSet.add(card1.getCardColor());
        cardColorSet.add(card2.getCardColor());

        //按大小顺序存储两张牌的值，方便后续获取最大牌
        sortLaizigou[0] = card1.getCorresponding();
        sortLaizigou[1] = card2.getCorresponding();
        Arrays.sort(sortLaizigou);
        logger.debug("player hand card:"+sortLaizigou[0]+":"+sortLaizigou[1]);

        //设置癞子牌
        assignment(card1,card2);

        common(mixedCardNum);
    }

    private void common(int mixedCardNum){
        if(mixedCardNum == 3){
            this.typeName = TYPE_JOKER_MAX;//todo
            this.typeCompareValue = 12;
        }else if(isSet() && mixedCardNum > 1){
            this.typeName = TYPE_JOKER_SET;
            this.typeCompareValue = 10;
        }else if(isSet()){
            this.typeName = TYPE_SET;
            this.typeCompareValue = 11;
        }else if(isStraightFlush()  && mixedCardNum > 1){
            this.typeName = TYPE_JOKER_STRAIGHT_FLUSH;
            this.typeCompareValue = 8;
        }else if(isStraightFlush()){
            this.typeName = TYPE_STRAIGHT_FLUSH;
            this.typeCompareValue = 9;
        }else if(isStraight() && mixedCardNum > 1){
            this.typeName = TYPE_JOKER_STRAIGHT;
            this.typeCompareValue = 6;
        }else if(isStraight()){
            this.typeName = TYPE_STRAIGHT;
            this.typeCompareValue = 7;
        }else if(isFlush() && mixedCardNum > 1){
            this.typeName = TYPE_JOKER_FLUSH;
            this.typeCompareValue = 4;
        }else if(isFlush()){
            this.typeName = TYPE_FLUSH;
            this.typeCompareValue = 5;
        }else if(isPair() && mixedCardNum > 1){
            this.typeName = TYPE_JOKER_PAIR;
            this.typeCompareValue = 2;
        }else if(isPair()){
            this.typeName = TYPE_PAIR;
            this.typeCompareValue = 3;
        }else if(isHighCard()){
            this.typeName = TYPE_HIGH_CARD;
            this.typeCompareValue = 1;
        }
    }


    /**
     * 设置癞子牌为所能形成的最大牌型
     * @param card1
     * @param card2
     */
    private void assignment(CardValueModel card1, CardValueModel card2){
        CardValueModel card3 = null;
        if (compareValueSet.size() == 1) { //癞子三条
            logger.debug("三条");
            list.remove((Character) card1.getCardColor());
            list.remove((Character) card2.getCardColor());
            logger.debug(list.toString()+"-------------------------------:"+list.size());
            card3 = new CardValueModel(list.get(new Random().nextInt(list.size())), card1.getCardValue());
            logger.debug("三条de 花色："+card1.getCardValue()+":"+card2.getCardValue()+":"+card3.getCardValue());
        }else {
            int difference = sortLaizigou[1] - sortLaizigou[0];
            logger.debug("差值："+difference);
            if (difference <= 2 || difference == 12 || (difference == 11 && sortLaizigou[1] == 12)) { //判断是否可以形成顺子
                char color;
                if (cardColorSet.size() == 1) { //癞子同花顺
                    logger.debug("同花顺");
                    color = card1.getCardColor();
                } else {  //癞子顺子
                    logger.debug("顺子");
                    color = list.get(new Random().nextInt(list.size()));
                }
                if (difference == 2) { //顺：card1 + 2 = card2
                    logger.debug("顺：card1 + 2 = card2");
                    card3 = new CardValueModel(color, CardValueHolder.getCardValue(sortLaizigou[0] + 1));
                }else if(difference == 11 && sortLaizigou[1] == 12){ // 11为特殊牌型AKQ
                    card3 = new CardValueModel(color, CardValueHolder.getCardValue(sortLaizigou[1] + 1));
                }
                if (difference == 1 || difference == 12) { //顺：card1 + 1 = card2
                    logger.debug("顺：card1 + 1 = card2");
                    if(sortLaizigou[1] == 3){   // 3为特殊牌型A23 12为AKQ
                        card3 = new CardValueModel(color, CardValueHolder.getCardValue(sortLaizigou[0] - 1));
                    }else if(difference == 12) {
                        card3 = new CardValueModel(color, CardValueHolder.getCardValue(sortLaizigou[1] - 1));
                    }else{
                        card3 = new CardValueModel(color, CardValueHolder.getCardValue(sortLaizigou[1] + 1));
                    }
                }
            } else {
                if (cardColorSet.size() == 1) { //癞子同花
                    logger.debug("同花");
                    if(sortLaizigou[0] == 1) { //取最大同花
                        card3 = new CardValueModel(card1.getCardColor(), CardValueHolder.getCardValue(13));
                    }else{
                        card3 = new CardValueModel(card1.getCardColor(), CardValueHolder.getCardValue(1));
                    }
                }else { //癞子对牌
                    logger.debug("对牌");
                    if(card1.getCompareValue() == sortLaizigou[1]){
                        list.remove((Character) card1.getCardColor());
                    }else{

                    }
                    if(sortLaizigou[0] == 1){
                        if(card1.getCorresponding() == sortLaizigou[0]){
                            list.remove((Character) card1.getCardColor());
                        }else{
                            list.remove((Character) card2.getCardColor());
                        }
                        card3 = new CardValueModel(list.get(new Random().nextInt(list.size())),
                                CardValueHolder.getCardValue(sortLaizigou[0]));
                    }else {
                        if(card1.getCorresponding() == sortLaizigou[1]){
                            list.remove((Character) card1.getCardColor());
                        }else{
                            list.remove((Character) card2.getCardColor());
                        }
                        card3 = new CardValueModel(list.get(new Random().nextInt(list.size())),
                                CardValueHolder.getCardValue(sortLaizigou[1]));
                    }
                }
            }
        }
        this.card1 = card1;
        this.card2 = card2;
        this.card3 = card3;

        if(this.playerInfo.getMixedCard() == 3){
            this.playerInfo.setMixedCardList(Ints.asList(card1.getCardId(),card2.getCardId(),card3.getCardId()));
        }else if(this.playerInfo.getMixedCard() == 2){
            this.playerInfo.setMixedCardList(Ints.asList(card2.getCardId(),card3.getCardId()));
        }else{
            this.playerInfo.setMixedCardList(Ints.asList(card3.getCardId()));
            logger.debug(this.playerInfo.getMixedCardList().toString());
        }
        //logger.debug(playerInfo.getUserId()+"blabla--------------------------------------"+card1+":"+card2+":"+card3);
        HandCards = new int[]{card1.getCardId(),card2.getCardId(),card3.getCardId()};
        //logger.debug(playerInfo.getUserId()+"blabla" +
        //        "--------------------------------------"+card1.getCardId()+":"+card2.getCardId()+":"+card3.getCardId());
        this.playerInfo.setHandCards(HandCards);
        //存储三张牌的比较值，方便后续获取最大牌
        sortArrCardCompare[0] = card1.getCompareValue();
        sortArrCardCompare[1] = card2.getCompareValue();
        sortArrCardCompare[2] = card3.getCompareValue();

        //为牌型赋值
        compareValueSet.add(card3.getCompareValue());
        cardColorSet.add(card3.getCardColor());

        Arrays.sort(sortArrCardCompare);
    }

    /**
     * 比较手牌牌型的大小：
     * 与参数相等返回 0
     * 小于参数返回 -1
     * 大于参数返回 1
     * @param model
     * @return
     */
    public int compareTo(HandCardTypeModel model){
        if(this.typeCompareValue > model.typeCompareValue){
            return 1;
        }else if(this.typeCompareValue < model.typeCompareValue){
            return -1;
        }else if(this.typeCompareValue == model.typeCompareValue){
            //当牌型相同，且是顺子或同花顺时，并且比较的两个牌型中存在 A23 的顺子
            if(typeName.equals(TYPE_STRAIGHT) || typeName.equals(TYPE_STRAIGHT_FLUSH)){
                if(isStraightA23 || model.isStraightA23){
                    /**
                     * A23 是小于 AKQ 的第二大的顺子
                     */
                    if(isStraightA23){
                        if(model.isStraightAKQ){
                            //如果对手是AKQ，那么 输
                            return -1;
                        }else if(model.isStraightA23){
                            //如果对手是 A23，那么打平
                            return 0;
                        }else{
                            //如果对手不是 AKQ，也不是 A23，那么 赢
                            return 1;
                        }
                    }else{
                        //当本方不是 A23时，判断对手是否是 A23
                        if(model.isStraightA23){
                            if(this.isStraightAKQ){
                                //对手是 A23，而本方是 AKQ，那么 赢
                                return 1;
                            }
                        }else{
                            //不做特殊比较，走下段程序中的正常比较逻辑
                        }
                    }
                }
            }

            //当牌型相同时，比较最大牌的值
            if(this.maxCardCompareValue > model.maxCardCompareValue){
                return 1;
            }else if(this.maxCardCompareValue < model.maxCardCompareValue){
                return -1;
            }else if(this.maxCardCompareValue == model.maxCardCompareValue){
                //当最大值相同时，比较第二大牌型
                if(this.secondCardCompareValue > model.secondCardCompareValue){
                    return 1;
                }else if(this.secondCardCompareValue < model.secondCardCompareValue){
                    return -1;
                }else if(this.secondCardCompareValue == model.secondCardCompareValue){
                    //当第二大牌型相同时，比较第三张牌
                    if(this.thirdCardCompareValue > model.thirdCardCompareValue){
                        return 1;
                    }else if(this.thirdCardCompareValue < model.thirdCardCompareValue){
                        return -1;
                    }else if(this.thirdCardCompareValue == model.thirdCardCompareValue){
                        //当第二大牌型相同时，比较第三张牌
                        return 0;
                    }
                }
            }
        }

        return 0;
    }

    /**
     * 牌型是否符合 三条
     * @return
     */
    private boolean isSet(){
        if(compareValueSet.size() == 1){
            this.maxCardCompareValue = card1.getCompareValue();
            this.secondCardCompareValue = card1.getCompareValue();
            this.thirdCardCompareValue = card1.getCompareValue();
            return true;
        }
        return false;
    }

    /**
     * 牌型是否符合 同花顺
     * @return
     */
    private boolean isStraightFlush(){
        if(isStraight() && isFlush()){
            return true;
        }
        return false;
    }

    /**
     * 牌型是否符合 顺子
     * @return
     */
    private boolean isStraight(){
        boolean result = false;
        if(compareValueSet.size() == 3){
            if(sortArrCardCompare[0]+1 == sortArrCardCompare[1] && sortArrCardCompare[0]+2 == sortArrCardCompare[2]){
                result = true;
            }
            //判断是否是 A23
            if(sortArrCardCompare[2] == 13 && sortArrCardCompare[1] == 2 && sortArrCardCompare[0] == 1){
                this.isStraightA23 = true;
                result = true;
            }
            //判断是否是 AKQ
            if(sortArrCardCompare[2] == 13 && sortArrCardCompare[1] == 12 && sortArrCardCompare[0] == 11){
                this.isStraightAKQ = true;
                result = true;
            }

            if(result == true){
                this.maxCardCompareValue = sortArrCardCompare[2];
                this.secondCardCompareValue = sortArrCardCompare[1];
                this.thirdCardCompareValue = sortArrCardCompare[0];
                return true;
            }
        }
        return false;
    }

    /**
     * 牌型是否符合 同花
     * @return
     */
    private boolean isFlush(){
        if(cardColorSet.size() == 1){
            this.maxCardCompareValue = sortArrCardCompare[2];
            this.secondCardCompareValue = sortArrCardCompare[1];
            this.thirdCardCompareValue = sortArrCardCompare[0];
            return true;
        }
        return false;
    }

    /**
     * 牌型是否符合 一对
     * @return
     */
    private boolean isPair(){
        if(compareValueSet.size() == 2){
            if(sortArrCardCompare[2] == sortArrCardCompare[1]){
                //最大的那张牌 是对子
                this.maxCardCompareValue = sortArrCardCompare[2];
                this.secondCardCompareValue = sortArrCardCompare[0];
                this.thirdCardCompareValue = sortArrCardCompare[0];
            }else{
                //最大的那张牌牌 不是对子
                this.maxCardCompareValue = sortArrCardCompare[1];
                this.secondCardCompareValue = sortArrCardCompare[2];
                this.thirdCardCompareValue = sortArrCardCompare[2];
            }
            return true;
        }
        return false;
    }

    /**
     * 牌型是否符合 单牌
     * @return
     */
    private boolean isHighCard(){
        if(compareValueSet.size() == 3){
            this.maxCardCompareValue = sortArrCardCompare[2];
            this.secondCardCompareValue = sortArrCardCompare[1];
            this.thirdCardCompareValue = sortArrCardCompare[0];
            return true;
        }
        return false;
    }

    public String toString(){
        return "handCard="+card1.toString()+" "+card2.toString()+" "+card3.toString()
                + ", typeName="+typeName
                + ", maxCompareValue="+maxCardCompareValue
                + ", 2ndCompareValue="+secondCardCompareValue
                + ", 3rdCompareValue="+thirdCardCompareValue
                + ", isSet="+isSet()
                + ", isStraight="+isStraight()
                + ", isFlush="+isFlush()
                + ", isPair="+isPair()
                + ", isHighCard="+isHighCard();
    }

    public String toStringCard(){
        StringBuilder sb = new StringBuilder();
        if(null != card1){
            sb.append(card1.toString());
        }

        if(null != card2){
            sb.append(card2.toString());
        }

        if(null != card3){
            sb.append(card3.toString());
        }
        return sb.toString();
    }

    public String getTypeName() {
        return typeName;
    }

    public int getTypeCompareValue() {
        return typeCompareValue;
    }

    public int getMaxCardCompareValue() {
        return maxCardCompareValue;
    }

    public int[] getHandCards() {
        return HandCards;
    }

    public String getCardValueString(){
        StringBuilder sb = new StringBuilder();
        if(null != card1){
            sb.append(card1.getCardValue());
        }

        if(null != card2){
            sb.append(card2.getCardValue());
        }

        if(null != card3){
            sb.append(card3.getCardValue());
        }
        return sb.toString();
    }
}
