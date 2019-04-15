package com.jule.domino.game.dao.bean;

import lombok.Getter;
import lombok.Setter;


/**
 * 房间通用的配置信息
 */
@Getter@Setter
public class CommonConfigModel {
    private int id;//序列,用以数据库修改数据
    private int playType; //玩法
    private int robDealerPct; //是否允许换牌
    private long changeCardFee; //换牌所需费用

    private int autoSettleType; //自动结算类型：1,底池上限 2,轮次上限
    private long poolMoney; //资金池初始量
    private int totalLosePct; //杀量池百分比
    private boolean isNeedAllowSideShow; //是否需要被比牌方同意比牌申请
    private boolean isChooseSideShowUser; //是否可以选择指定用户进行比牌
    /**被比方同意比牌弹窗的倒计时*/
    private int fixDealerCD;
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

    private double strategyMaxPct;
    private double strategyMinPct;
    private String robotJoinPct;

    @Override
    public String toString() {
        return "CommonConfigModel{" +
                "id=" + id +
                ", playType=" + playType +
                ", changeCardFee=" + changeCardFee +
                //", betSeveralTimesList=" + betSeveralTimesList +
                ", autoSettleType=" + autoSettleType +
                ", isNeedAllowSideShow=" + isNeedAllowSideShow +
                ", isChooseSideShowUser=" + isChooseSideShowUser +
                ", fixDealerCD=" + fixDealerCD +
                ", betCountDownSec=" + betCountDownSec +
                ", gameStartCountDownSec=" + gameStartCountDownSec +
                '}';
    }
}
