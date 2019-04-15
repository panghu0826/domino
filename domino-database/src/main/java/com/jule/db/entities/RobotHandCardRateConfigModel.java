package com.jule.db.entities;

import com.jule.db.dao.BaseDbBean;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "robot_hand_card_rate_config")
public class RobotHandCardRateConfigModel extends BaseDbBean {
    public RobotHandCardRateConfigModel(){
        this.handCardId = 0;
        this.humanCount = 0;
        this.callRate = 100;
        this.showRate = 100;
        this.foldRate = 100;
        this.agreeSideShowRate = 100;
    }

    //序列号
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //对应robot_hand_card_config表的ID
    @Column(name = "handCardId")
    private int handCardId;

    //同时玩牌的人数
    @Column(name = "humanCount")
    private int humanCount;

    //跟注概率
    @Column(name = "callRate")
    private double callRate;

    //show比牌概率
    @Column(name = "showRate")
    private double showRate;

    //弃牌概率
    @Column(name = "foldRate")
    private double foldRate;

    //同意sideShow比牌概率
    @Column(name = "agreeSideShowRate")
    private double agreeSideShowRate;

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = Integer.valueOf(String.valueOf(id));
    }
}
