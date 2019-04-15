package com.jule.db.entities;

import com.jule.db.dao.BaseDbBean;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;


@Entity
@Table(name = "robot_hand_card_config")
public class RobotHandCardConfigModel extends BaseDbBean {
    private Map<Integer, RobotHandCardRateConfigModel> rateMap = new HashMap<>();

    public Map<Integer, RobotHandCardRateConfigModel> getRateMap(){
        return this.rateMap;
    }

    //序列号
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    //玩法（可以不是GameId，如Joker、Other）
    @Column(name = "playType")
    private String playType;

    //牌型（三条、同花顺、顺子、同花、手对、单张）
    @Column(name = "cardType")
    private String cardType;

    //手牌中包含的牌（比如AKQ中包含AK）
    @Column(name = "containsCards")
    private String containsCards;

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = Integer.valueOf(String.valueOf(id));
    }

    public String getPlayType() {
        return playType;
    }

    public void setPlayType(String playType) {
        this.playType = playType;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getContainsCards() {
        return containsCards;
    }

    public void setContainsCards(String containsCards) {
        this.containsCards = containsCards;
    }
}
