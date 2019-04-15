package com.jule.db.entities;

import com.jule.db.dao.BaseDbBean;
import lombok.Data;

import javax.persistence.*;

/**
 * 机器人牌力值配置
 */
@Data
@Entity
@Table(name = "robot_card_level_config")
public class RobotCardLevelConfigModel  extends BaseDbBean {

    //序列号
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;


    //玩法
    @Column(name = "play_type")
    private String playType;

    //牌型
    //单排-1 混对-2 对牌-3 混同花-4 同花-5 混顺子-6 顺子-7 混同花顺-8 同花顺-9 混三张-10 三张-11 三张混-12
    @Column(name = "card_type")
    private int cardType;

    //牌力值
    @Column(name = "card_level")
    private String cardLevel;

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = Integer.parseInt(String.valueOf(id));
    }
}
