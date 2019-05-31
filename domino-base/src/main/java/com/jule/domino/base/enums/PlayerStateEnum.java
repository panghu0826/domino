package com.jule.domino.base.enums;

/**
 * 玩家状态
 */
public enum PlayerStateEnum {
    spectator(0), //旁观
    siteDown(1), //入座
    game_ready(2), //游戏准备
    beting(3), //下注中
    already_bet(4), //已下注
    open_card(5), //已开牌
    fold(6),//弃牌
    settle(7); //已结算

    private final int value;

    PlayerStateEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
