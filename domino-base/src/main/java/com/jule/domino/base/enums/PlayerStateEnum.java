package com.jule.domino.base.enums;

/**
 * 玩家状态
 */
public enum PlayerStateEnum {
    spectator(0), //旁观
    siteDown(1), //入座
    game_ready(2), //游戏准备
    rob(3),//抢庄
    beting(4), //下注中
    already_bet(5), //已下注
    card_type(6), //已定牌型
    settle(7); //已结算

    private final int value;

    PlayerStateEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
