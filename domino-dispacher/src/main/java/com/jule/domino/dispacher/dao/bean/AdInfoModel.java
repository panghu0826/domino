package com.jule.domino.dispacher.dao.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * 广告奖励表
 */
@Setter@Getter
public class AdInfoModel {

    //玩家UID
    private String uid;
    //当日次数
    private int times;
    //当日总领取筹码
    private long totalmoney;
    //最后一次领取时间
    private long lastTime;

}