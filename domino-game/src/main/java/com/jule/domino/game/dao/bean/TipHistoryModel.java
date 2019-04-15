package com.jule.domino.game.dao.bean;

import java.util.Date;

public class TipHistoryModel {
    private Long id;

    private String user_id;

    private String target_user_id;

    private Integer play_type;

    private String room_id;

    private String table_id;

    private Integer tip_value;

    private Date create_time;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id == null ? null : user_id.trim();
    }

    public String getTarget_user_id() {
        return target_user_id;
    }

    public void setTarget_user_id(String target_user_id) {
        this.target_user_id = target_user_id == null ? null : target_user_id.trim();
    }

    public Integer getPlay_type() {
        return play_type;
    }

    public void setPlay_type(Integer play_type) {
        this.play_type = play_type;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id == null ? null : room_id.trim();
    }

    public String getTable_id() {
        return table_id;
    }

    public void setTable_id(String table_id) {
        this.table_id = table_id == null ? null : table_id.trim();
    }

    public Integer getTip_value() {
        return tip_value;
    }

    public void setTip_value(Integer tip_value) {
        this.tip_value = tip_value;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }
}