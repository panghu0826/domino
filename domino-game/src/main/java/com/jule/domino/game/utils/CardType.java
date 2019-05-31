package com.jule.domino.game.utils;

public enum CardType {

    Supreme("至尊青龙", 21, 32, 0, 0),
    Dragon("一条龙", 20, 30, 0, 0),
    RoyalFamily("十二皇族", 19, 24, 0, 0),
    ThreeStraightFlush("三同花顺", 18, 20, 0, 0),
    ThreeSetsOfQuads("三分天下", 17, 20, 0, 0),
    AllMax("全大", 16, 10, 0, 0),
    AllSmall("全小", 15, 10, 0, 0),
    AllFlush("凑一色", 14, 10, 0, 0),
    FourSetsOfSet("四套三条", 13, 6, 0, 0),
    FiveSet("五对三条", 12, 5, 0, 0),
    SixPair("六对半", 11, 4, 0, 0),
    ThreeStraight("三顺子", 10, 4, 0, 0),
    ThreeFlush("三同花", 9, 3, 0, 0),
    StraightFlush("同花顺", 8, 5, 2, 5),
    Quads("铁支", 7, 4, 2, 4),
    FullHouse("葫芦", 6, 1, 2, 1),
    Flush("同花", 5, 1, 0, 0),
    Straight("顺子", 4, 1, 0, 0),
    Set("三条", 3, 1, 1, 2),
    TwoPair("两对", 2, 1, 0, 0),
    Pair("对子", 1, 1, 0, 0),
    Common("乌龙", 0, 1, 0, 0);

    private String name;

    private int type;
    private int score;
    private int index;
    private int extra;

    CardType(String name, int type, int score, int index, int extra) {
        this.name = name;
        this.type = type;
        this.score = score;
        this.index = index;
        this.extra = extra;
    }

    public int getIndex(){
        return this.index;
    }

    public int getExtra(){
        return extra;
    }

    public int getScore(){
        return this.score;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    /***
     * 比较牌型大小
     * @param cardType
     * @return
     */
    public int compare(CardType cardType) {
        if (this.type > cardType.getType()) {
            return 1;
        } else if (this.type < cardType.getType()) {
            return -1;
        } else {
            return 0;
        }
    }
}
