package com.jule.domino.base.enums;

public enum AlarmEnum {
    NEXT_BET_PLAYER("NEXT_BET_PLAYER"),
    /**桌内玩家是否匹配*/
    TABLE_PLAYER_MATCH("TABLE_PLAYER_MATCH"),
    /**桌内玩家站起失败*/
    STAND_UP_FAILED("STAND_UP_FAILED"),
    /**根据userId查,看玩家是否在桌内*/
    TABLE_PLAYERS_MATCH_SEAT("TABLE_PLAYERS_MATCH_SEAT"),
    ;
    private String type;
    private AlarmEnum(String type){
        this.type = type;
    }
    public String getType(){
        return type;
    }
}
