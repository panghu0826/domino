package com.jule.db.entities;

import com.jule.db.dao.BaseDbBean;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 机器人资金 代入/代出 记录
 */
@Data
@Entity
@Table(name = "robot_capital_pool_record")
public class RobotCapitalPoolRecordModel extends BaseDbBean {
    //序列号
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //玩法
    @Column(name = "play_type")
    private String playType;

    //操作时间
    @Column(name = "operation_time")
    private Date operationTime;

    //存取 1/取 2/存
    @Column(name = "access")
    private int access;

    //存取金额
    @Column(name = "money")
    private double money;

    //机器人ID
    @Column(name = "robotId")
    private String robotId;

    //资金池总值（操作后）
    @Column(name = "total_money")
    private double totalMoney;

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = Integer.valueOf(String.valueOf(id));
    }
}
