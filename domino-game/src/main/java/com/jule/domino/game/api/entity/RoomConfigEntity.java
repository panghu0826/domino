package com.jule.domino.game.api.entity;

import com.jule.domino.game.dao.bean.RoomConfigModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 房间配置信息
 */
@Getter@Setter@ToString
public class RoomConfigEntity{
    private int id;//序列,用以数据库修改数据
    private String roomId; //房间ID
    private long minScore4JoinTable; //入桌的最小积分
    private long ante; //底注
    private double serviceChargeRate; //服务费比率
    private String doubleRoles;//加倍规则
    /**1开0关*/
    private int onOff;

    public RoomConfigEntity(RoomConfigModel configModel) {
        this.id = configModel.getId();
        this.roomId = configModel.getRoomId();
        this.minScore4JoinTable = configModel.getMinScore4JoinTable() * 100;
        this.ante = configModel.getAnte() * 100;
        this.serviceChargeRate = configModel.getServiceChargeRate() * 10000;
        this.doubleRoles = configModel.getDoubleRoles();
        this.onOff = configModel.getOnOff();
    }
}
