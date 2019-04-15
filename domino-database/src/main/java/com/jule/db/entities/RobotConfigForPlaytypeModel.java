package com.jule.db.entities;

import com.jule.db.dao.BaseDbBean;
import lombok.Data;

import javax.persistence.*;

/**
 * 机器人配置 - 分玩法
 */
@Data
@Entity
@Table(name = "robot_playtype_config")
public class RobotConfigForPlaytypeModel extends BaseDbBean {

    //序列号
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //玩法
    @Column(name = "play_type")
    private String playType;

    //牌力值
    @Column(name = "card_level")
    private String cardLevel;

    //非盲牌-加注概率（百分比）
    @Column(name = "raise_rate_for_see")
    private int raiseRateForSee;

    //非盲牌-弃牌概率（百分比）
    @Column(name = "fold_rate_for_see")
    private int foldRateForSee;

    //非盲牌-跟注概率（百分比）
    @Column(name = "call_rate_for_see")
    private int callRateForSee;

    //非盲牌-比牌概率（百分比）
    @Column(name = "show_rate_for_see")
    private int showRateForSee;

    //不同意比牌（百分比）
    @Column(name = "rate_un_agree_sideshow")
    private int rateUnAgreeSideShow;

    //同意比牌（百分比）
    @Column(name = "rate_agree_sideshow")
    private int rateAgreeSideShow;

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = Integer.parseInt(String.valueOf(id));
    }
}