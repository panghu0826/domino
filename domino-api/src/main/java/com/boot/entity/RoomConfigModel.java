package com.boot.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 房间配置信息
 */
@Entity
@Table(name = "room_config")
@Getter@Setter@ToString
public class RoomConfigModel implements Serializable {
    @Id
    private int id;//序列,用以数据库修改数据
    private String roomId; //房间ID
    private long minScore4JoinTable; //入桌的最小积分
    private long ante; //底注
    private long firstBaseBetScore; //第一次下注的基础注
    private long maxBetScore; //最大下注额度，任何轮次的下注额不能大于此值
    /**服务费比率*/
    private double serviceChargeRate;
    private int maxBlindRound; //盲牌最多可以进行的轮次数
    private long maxPot; //总封：底池最大的累积积分数，达到此值，全部玩家自动开牌，进行牌局结算
    private int tipValue;//打赏荷官的数量
    private long changeCardFee;//换牌所需费用
    private int changeCardFeeTips;//换牌收取费用比例

    public int compareTo(Object o){
        RoomConfigModel oModel = (RoomConfigModel)o;
        if(this.minScore4JoinTable > oModel.minScore4JoinTable){
            return 1;
        }else if(this.minScore4JoinTable < oModel.minScore4JoinTable){
            return -1;
        }
        return 0;
    }
}
