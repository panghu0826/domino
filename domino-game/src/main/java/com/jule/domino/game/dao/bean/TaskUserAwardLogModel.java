package com.jule.domino.game.dao.bean;

import java.util.Date;

public class TaskUserAwardLogModel {
    private Long id;

    private Integer taskId;

    private String taskGameId;

    private Byte taskType;

    private String taskName;

    private Integer taskTargetValue;

    private Date taskBeginTime;

    private Date taskEndTime;

    private String itemId;

    private Integer itemNum;

    private Byte succAward;

    private Date createTime;

    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getTaskGameId() {
        return taskGameId;
    }

    public void setTaskGameId(String taskGameId) {
        this.taskGameId = taskGameId == null ? null : taskGameId.trim();
    }

    public Byte getTaskType() {
        return taskType;
    }

    public void setTaskType(Byte taskType) {
        this.taskType = taskType;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName == null ? null : taskName.trim();
    }

    public Integer getTaskTargetValue() {
        return taskTargetValue;
    }

    public void setTaskTargetValue(Integer taskTargetValue) {
        this.taskTargetValue = taskTargetValue;
    }

    public Date getTaskBeginTime() {
        return taskBeginTime;
    }

    public void setTaskBeginTime(Date taskBeginTime) {
        this.taskBeginTime = taskBeginTime;
    }

    public Date getTaskEndTime() {
        return taskEndTime;
    }

    public void setTaskEndTime(Date taskEndTime) {
        this.taskEndTime = taskEndTime;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId == null ? null : itemId.trim();
    }

    public Integer getItemNum() {
        return itemNum;
    }

    public void setItemNum(Integer itemNum) {
        this.itemNum = itemNum;
    }

    public Byte getSuccAward() {
        return succAward;
    }

    public void setSuccAward(Byte succAward) {
        this.succAward = succAward;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}