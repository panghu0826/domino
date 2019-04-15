package com.jule.domino.auth.dao.bean;

import java.util.Date;

//用户领取各项奖励记录
public class RewardReceiveRecordModel {
    private int id;     //序列,用以数据库修改数据
    private String playerId;        //奖励领取用户
    private String rewardType;      //领取的奖励类型
    private Date receiveTime;       //领取时间
    private int receiveMode;        //领取方式 1/guest 2/facebook
    private long receiveAmount;     //领取数量
    private long receiveNumber;       //领取次数
    private int continuityLoginDay;     //当前连续登陆天数

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getRewardType() {
        return rewardType;
    }

    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
    }

    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    public int getReceiveMode() {
        return receiveMode;
    }

    public void setReceiveMode(int receiveMode) {
        this.receiveMode = receiveMode;
    }

    public long getReceiveAmount() {
        return receiveAmount;
    }

    public void setReceiveAmount(long receiveAmount) {
        this.receiveAmount = receiveAmount;
    }

    public long getReceiveNumber() {
        return receiveNumber;
    }

    public void setReceiveNumber(long receiveNumber) {
        this.receiveNumber = receiveNumber;
    }

    public int getContinuityLoginDay() {
        return continuityLoginDay;
    }

    public void setContinuityLoginDay(int continuityLoginDay) {
        this.continuityLoginDay = continuityLoginDay;
    }

    @Override
    public String toString() {
        return "RewardReceiveRecordModel{" +
                "id=" + id +
                ", playerId='" + playerId + '\'' +
                ", rewardType='" + rewardType + '\'' +
                ", receiveTime=" + receiveTime +
                ", receiveMode=" + receiveMode +
                ", receiveAmount=" + receiveAmount +
                ", receiveNumber=" + receiveNumber +
                ", continuityLoginDay=" + continuityLoginDay +
                '}';
    }
}
