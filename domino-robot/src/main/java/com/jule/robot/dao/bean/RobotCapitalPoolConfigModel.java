package com.jule.robot.dao.bean;

import com.jule.db.dao.BaseDbBean;
import lombok.Data;

import javax.persistence.*;

/**
 * 机器人资金池配置
 */
@Data
@Entity
@Table(name = "robot_capital_pool_config")
public class RobotCapitalPoolConfigModel extends BaseDbBean {

   //序列号
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //玩法
    @Column(name = "play_type")
    private String playType;

    //总金额
    @Column(name = "total_money")
    private long totalMoney;

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = Integer.valueOf(String.valueOf(id));
    }
}
