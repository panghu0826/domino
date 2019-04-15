package com.jule.db.entities;

import com.jule.db.dao.BaseDbBean;
import lombok.Data;

import javax.persistence.*;
/**
 * 机器人公用配置
 */
@Data
@Entity
@Table(name = "robot_common_config")
public class RobotCommonConfigModel  extends BaseDbBean {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;//序列,用以数据库修改数据

    //单人等待时间
    @Column(name = "single_player_wait_sec")
    private int singlePlayerWaitSec;

    //两人游戏时间
    @Column(name = "two_player_wait_sec")
    private int twoPlayerWaitSec;

    //带入量min(房间底注乘以百分比，最小房间则固定去2000-3000之间)
    @Column(name = "buyin_min")
    private long buyinMin;

    //带入量max(房间底注乘以百分比）
    @Column(name = "buyin_max")
    private long buyinMax;

    //看牌携带量（百分比）
    @Column(name = "money_store_to_see")
    private long moneyStoreToSee;

    //看牌随机概率（百分比）
    @Column(name = "see_card_rate")
    private int seeCardRate;

    //盲牌加注概率（百分比）
    @Column(name = "raise_rate_for_blind")
    private int raiseRateForBlind;

    @Override
    public void setId(long id) {
        this.id = Integer.parseInt(String.valueOf(id));
    }
}
