package com.jule.domino.room.dao.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 房间配置信息
 */
@Getter@Setter@ToString
public class RoomConfigModel implements Comparable{
    private int id;//序列,用以数据库修改数据
    private String roomId; //房间ID
    private long minScore4JoinTable; //入桌的最小积分
    private long ante; //底注
    private double serviceChargeRate; //服务费比率
    private String robMultiple;
    private String doubleRoles;//加倍规则
    /**1开0关*/
    private int onOff;


    @Override
    public int compareTo(Object o){
        RoomConfigModel oModel = (RoomConfigModel)o;
        return (int)(this.minScore4JoinTable - oModel.minScore4JoinTable);
    }
}
