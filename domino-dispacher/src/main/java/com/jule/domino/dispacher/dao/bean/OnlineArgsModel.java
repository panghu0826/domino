package com.jule.domino.dispacher.dao.bean;

import java.util.Date;

public class OnlineArgsModel {
    private Integer id;

    private Integer online_id;

    private Integer type;

    private String value;

    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOnline_id() {
        return online_id;
    }

    public void setOnline_id(Integer online_id) {
        this.online_id = online_id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value == null ? null : value.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}