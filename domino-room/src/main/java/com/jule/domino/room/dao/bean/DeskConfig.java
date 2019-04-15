package com.jule.domino.room.dao.bean;

public class DeskConfig {
    private String id;

    private String room_desc;

    private Integer limit_bet;//最小带入

    private Integer mix_bet;

    private Integer max_bet;

    private Integer zhuang_limit;//庄家最下带入

    private Integer desk_begin;

    private Integer desk_end;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getRoom_desc() {
        return room_desc;
    }

    public void setRoom_desc(String room_desc) {
        this.room_desc = room_desc == null ? null : room_desc.trim();
    }

    public Integer getLimit_bet() {
        return limit_bet;
    }

    public void setLimit_bet(Integer limit_bet) {
        this.limit_bet = limit_bet;
    }

    public Integer getMix_bet() {
        return mix_bet;
    }

    public void setMix_bet(Integer mix_bet) {
        this.mix_bet = mix_bet;
    }

    public Integer getMax_bet() {
        return max_bet;
    }

    public void setMax_bet(Integer max_bet) {
        this.max_bet = max_bet;
    }

    public Integer getZhuang_limit() {
        return zhuang_limit;
    }

    public void setZhuang_limit(Integer zhuang_limit) {
        this.zhuang_limit = zhuang_limit;
    }

    public Integer getDesk_begin() {
        return desk_begin;
    }

    public void setDesk_begin(Integer desk_begin) {
        this.desk_begin = desk_begin;
    }

    public Integer getDesk_end() {
        return desk_end;
    }

    public void setDesk_end(Integer desk_end) {
        this.desk_end = desk_end;
    }
}