package com.jule.domino.auth.dao.bean;

import java.util.Date;

//奖励的信息
public class RewardConfigModel {
    private int id;     //序列,用以数据库修改数据
    private String rewardType;      //奖励类型："checkin"/签到
    private String rewardGoodsType;     //奖励的物品类型
    private int rewardReceiveMode;        //奖励获取方式：1/guest登陆领取 2/facebook登陆领取
    private String rewardPictureAddress;      //奖励图片地址
    private Date validDateType;      //奖励领取有效期：登陆当日
    private long rewardAmount;      //奖励数量

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRewardType() {
        return rewardType;
    }

    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
    }

    public String getRewardGoodsType() {
        return rewardGoodsType;
    }

    public void setRewardGoodsType(String rewardGoodsType) {
        this.rewardGoodsType = rewardGoodsType;
    }

    public int getRewardReceiveMode() {
        return rewardReceiveMode;
    }

    public void setRewardReceiveMode(int rewardReceiveMode) {
        this.rewardReceiveMode = rewardReceiveMode;
    }

    public String getRewardPictureAddress() {
        return rewardPictureAddress;
    }

    public void setRewardPictureAddress(String rewardPictureAddress) {
        this.rewardPictureAddress = rewardPictureAddress;
    }

    public Date getValidDateType() {
        return validDateType;
    }

    public void setValidDateType(Date validDateType) {
        this.validDateType = validDateType;
    }

    public long getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(long rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    @Override
    public String toString() {
        return "RewardConfigModel{" +
                "id=" + id +
                ", rewardType='" + rewardType + '\'' +
                ", rewardGoodsType='" + rewardGoodsType + '\'' +
                ", rewardReceiveMode=" + rewardReceiveMode +
                ", rewardPictureAddress='" + rewardPictureAddress + '\'' +
                ", validDateType=" + validDateType +
                ", rewardAmount=" + rewardAmount +
                '}';
    }
}
