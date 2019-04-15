package com.jule.domino.game.dao.bean;

import com.google.common.reflect.TypeToken;
import com.jule.domino.game.utils.JsonWorker;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
    private String onlinerRoles;//在线人数规则

    private List<Integer> doubleList;

    @Override
    public int compareTo(Object o){
        RoomConfigModel oModel = (RoomConfigModel)o;
        return (int)(this.ante - oModel.ante);
    }

    public List<Integer> getDoubleList() {
        List<Integer> roles = new ArrayList<>();
        if (StringUtils.isEmpty(doubleRoles)){
            return roles;
        }

        roles = JsonWorker.OBJ.getGson().fromJson(this.doubleRoles, new TypeToken<List<Integer>>() {}.getType());
        return roles;
    }

    public long getMinScore4JoinTable(){
        return ante * 4;
    }
}
