package com.jule.domino.base.enums;

/**
 * 玩家状态
 */
public enum PlayerStateEnum {
    spectator(0), //旁观
    siteDown(1), //入座
    game_ready(2), //游戏准备
    robbing(3),//抢庄中
    already_rob(4),//已抢庄
    beting(5), //下注中
    already_bet(6), //已下注
    open_card(7), //已开牌
    fold(8),//弃牌
    settle(9); //已结算

    private final int value;

    PlayerStateEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
