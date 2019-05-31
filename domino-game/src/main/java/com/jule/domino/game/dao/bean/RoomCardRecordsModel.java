package com.jule.domino.game.dao.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RoomCardRecordsModel {
    private Integer id;

    private String createUserId;

    private String createNickName;

    private String createIcoUrl;

    private Date createTime;

    private String moneyToken;

    private Integer money;

    private Integer state;

    private String receiveUserId;

    private String receiveNickName;

    private String receiveIcoUrl;

    private Date receiveTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId == null ? null : createUserId.trim();
    }

    public String getCreateNickName() {
        return createNickName;
    }

    public void setCreateNickName(String createNickName) {
        this.createNickName = createNickName == null ? null : createNickName.trim();
    }

    public String getCreateIcoUrl() {
        return createIcoUrl;
    }

    public void setCreateIcoUrl(String createIcoUrl) {
        this.createIcoUrl = createIcoUrl == null ? null : createIcoUrl.trim();
    }

    public String getCreateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(createTime);
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getMoneyToken() {
        return moneyToken;
    }

    public void setMoneyToken(String moneyToken) {
        this.moneyToken = moneyToken == null ? null : moneyToken.trim();
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getReceiveUserId() {
        return receiveUserId;
    }

    public void setReceiveUserId(String receiveUserId) {
        this.receiveUserId = receiveUserId == null ? null : receiveUserId.trim();
    }

    public String getReceiveNickName() {
        return receiveNickName;
    }

    public void setReceiveNickName(String receiveNickName) {
        this.receiveNickName = receiveNickName == null ? null : receiveNickName.trim();
    }

    public String getReceiveIcoUrl() {
        return receiveIcoUrl;
    }

    public void setReceiveIcoUrl(String receiveIcoUrl) {
        this.receiveIcoUrl = receiveIcoUrl == null ? null : receiveIcoUrl.trim();
    }

    public String getReceiveTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(receiveTime);
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }
}