package com.jule.domino.game.dao.bean;

import java.util.Date;

public class FriendTableModel {
    private Integer id;

    private String userId;

    private String friendUserId;

    private String friendNickName;

    private String friendIcoUrl;

    private Date addTime;

    private Integer state;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(String friendUserId) {
        this.friendUserId = friendUserId;
    }

    public String getFriendNickName() {
        return friendNickName;
    }

    public void setFriendNickName(String friendNickName) {
        this.friendNickName = friendNickName;
    }

    public String getFriendIcoUrl() {
        return friendIcoUrl;
    }

    public void setFriendIcoUrl(String friendIcoUrl) {
        this.friendIcoUrl = friendIcoUrl;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}