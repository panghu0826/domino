package com.jule.domino.game.dao.bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ItemRecordsModel {
    private Integer id;

    private String createUserId;

    private String createNickName;

    private String createIcoUrl;

    private Date createTime;

    private String itemToken;

    private Integer itemId;

    private Integer itemTime;

    private Integer state;

    private String receiveUserId;

    private String receiveNickName;

    private String receiveIcoUrl;

    private Date receiveTime;

    private Date dueTime;

    public ItemRecordsModel(){
        try {
            receiveTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2016-02-19 00:00:00");
            dueTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2016-02-19 00:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

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

    public String getItemToken() {
        return itemToken;
    }

    public void setItemToken(String itemToken) {
        this.itemToken = itemToken;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getItemTime() {
        return itemTime;
    }

    public void setItemTime(Integer itemTime) {
        this.itemTime = itemTime;
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

    public String getDueTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dueTime);
    }

    public void setDueTime(Date dueTime) {
        this.dueTime = dueTime;
    }

    @Override
    public String toString() {
        return "ItemRecordsModel{" +
                "createTime=" + this.getCreateTime() +
                ", receiveTime=" + this.getReceiveTime() +
                ", dueTime=" + this.getDueTime() +
                '}';
    }
}