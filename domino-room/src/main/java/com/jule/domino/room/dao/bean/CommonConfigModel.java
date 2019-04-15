package com.jule.domino.room.dao.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 房间通用的配置信息
 */
@Getter@Setter
public class CommonConfigModel {
    private int id;//序列,用以数据库修改数据
    private int playType; //玩法
    private boolean robDealerPct; //是否允许换牌
    private long changeCardFee; //换牌所需费用
    private List<Integer> betSeveralTimesList; //允许的加倍数列表
    private int autoSettleType; //自动结算类型：1,轮次上限 2,底池上限
    private boolean isNeedAllowSideShow; //是否需要被比牌方同意比牌申请
    private boolean isChooseSideShowUser; //是否可以选择指定用户进行比牌
    private int fixDealerCD; //被比方同意比牌弹窗的倒计时
    /**下注倒计时*/
    private int betCountDownSec;
    /**抢庄倒计时*/
    private int robZhuangCD;
    /**开牌倒计时*/
    private int openCardsCD;
    /**结算特效倒计时*/
    private int settleCD;
    /**游戏开始倒计时（发牌前倒计时）*/
    private int gameStartCountDownSec=5;
    @Override
    public String toString() {
        return "CommonConfigModel{" +
                "id=" + id +
                ", playType=" + playType +
                ", changeCardFee=" + changeCardFee +
                ", betSeveralTimesList=" + betSeveralTimesList +
                ", autoSettleType=" + autoSettleType +
                ", isNeedAllowSideShow=" + isNeedAllowSideShow +
                ", isChooseSideShowUser=" + isChooseSideShowUser +
                ", fixDealerCD=" + fixDealerCD +
                ", betCountDownSec=" + betCountDownSec +
                ", gameStartCountDownSec=" + gameStartCountDownSec +
                '}';
    }
}
