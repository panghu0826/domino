package com.jule.domino.base.enums;

/**
 * 房间状态
 */
public enum TableStateEnum {
    IDEL(0),//空闲状态
    GAME_READY(1), //游戏准备
    PLAYER_ROB(2),//抢庄中
    BET(3),//下注状态
    OPEN_CARD(4),//比牌状态
    SETTLE_ANIMATION(5),//结算动画状态
    ;

    private final int value;

    TableStateEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
