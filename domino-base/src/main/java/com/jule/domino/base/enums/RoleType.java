package com.jule.domino.base.enums;

import com.jule.core.utils.EnumUtil;

import java.util.List;

public enum RoleType implements IndexedEnum {
    ROBOT(0,"robot"),
    ROBOT_V2(4,"robot_v2"),
    GUEST(1,"guest"),
    FACEBOOK(2,"facebook"),
    HUOWEI(3,"huawei"),
    ;
    public static final RoleType getRoleType(String value){
        for (RoleType roleType : RoleType.values()) {
            if(roleType.getTypeName().equals(value)){
                return roleType;
            }
        }
        return null;
    }
    /** 枚举值列表 */
    private static final List<RoleType>
            values = IndexedEnumUtil.toIndexes(RoleType.values());
    /**
     * 将 int 类型转换为枚举类型
     *
     * @param index
     * @return
     */
    public static RoleType valueOf(int index) {
        return EnumUtil.valueOf(values, index);
    }

    private int index;
    private String typeName;
    RoleType(int index,String type){
        this.index = index;
        this.typeName = type;
    }
    public int getIndex() {
        return index;
    }
    public String getTypeName(){return typeName;}
}
