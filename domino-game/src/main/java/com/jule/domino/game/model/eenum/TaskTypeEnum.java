package com.jule.domino.game.model.eenum;

public enum TaskTypeEnum {
    CARD_NUM(1),//牌局(开局时记录)
    WIN_NUM(2), //赢局（结算时记录）
    WINNING_STREAK_NUM(3),//连胜（结算）
    ;

    private final int index;
    TaskTypeEnum(int index){
        this.index = index;
    }
    public int getIndex() {
        return index;
    }
}
