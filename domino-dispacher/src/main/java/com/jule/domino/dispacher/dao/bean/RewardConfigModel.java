package com.jule.domino.dispacher.dao.bean;

import lombok.Data;

import java.util.Date;

@Data
public class RewardConfigModel {
    private Integer id;

    private String reward_type;

    private String reward_goods_type;

    private Integer reward_receive_mode;

    private String reward_picture_address;

    private Date valid_date_type;

    private Long reward_amount;

    private int reward_goods_id;

    public String getReward_type() {
        return reward_type;
    }

    public void setReward_type(String reward_type) {
        this.reward_type = reward_type == null ? null : reward_type.trim();
    }

    public Integer getReward_receive_mode() {
        return reward_receive_mode;
    }

    public void setReward_receive_mode(Integer reward_receive_mode) {
        this.reward_receive_mode = reward_receive_mode;
    }

    public String getReward_picture_address() {
        return reward_picture_address;
    }

    public void setReward_picture_address(String reward_picture_address) {
        this.reward_picture_address = reward_picture_address == null ? null : reward_picture_address.trim();
    }

    public Date getValid_date_type() {
        return valid_date_type;
    }

    public void setValid_date_type(Date valid_date_type) {
        this.valid_date_type = valid_date_type;
    }

    public Long getReward_amount() {
        return reward_amount;
    }

    public void setReward_amount(Long reward_amount) {
        this.reward_amount = reward_amount;
    }
}