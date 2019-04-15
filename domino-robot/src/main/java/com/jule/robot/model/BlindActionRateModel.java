package com.jule.robot.model;

import lombok.Getter;

/**
 * 盲牌行动时的概率模型
 */
@Getter
public class BlindActionRateModel {
    private int blindRate; //盲牌概率(不看牌概率)
    private int callRate; //跟注概率
    private int showRate; //比牌概率

    public BlindActionRateModel(int blindRate, int callRate, int showRate) {
        this.blindRate = blindRate;
        this.callRate = callRate;
        this.showRate = showRate;
    }
}
