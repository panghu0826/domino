package com.jule.domino.game.dao.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TableCreationRecordsModel {
    private Integer id;

    private String tableId;

    private Date createTime;

    private Integer playerNum;

    private Integer baseScore;

    private Integer readyCd;

    private Integer betCd;

    private Integer openCardCd;

    private Integer betMaxScore;

    private Integer gameNum;

    private String betMultiple;

    private Integer isWatch;

    private String createUserId;

    private Integer tableState;

    private String gameId;

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getBetMultiple() {
        return betMultiple;
    }

    public void setBetMultiple(String betMultiple) {
        this.betMultiple = betMultiple;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId == null ? null : tableId.trim();
    }

    public String getCreateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(createTime);
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getPlayerNum() {
        return playerNum;
    }

    public void setPlayerNum(Integer playerNum) {
        this.playerNum = playerNum;
    }

    public Integer getBaseScore() {
        return baseScore;
    }

    public void setBaseScore(Integer baseScore) {
        this.baseScore = baseScore;
    }

    public Integer getReadyCd() {
        return readyCd;
    }

    public void setReadyCd(Integer readyCd) {
        this.readyCd = readyCd;
    }

    public Integer getBetCd() {
        return betCd;
    }

    public void setBetCd(Integer betCd) {
        this.betCd = betCd;
    }

    public Integer getOpenCardCd() {
        return openCardCd;
    }

    public void setOpenCardCd(Integer openCardCd) {
        this.openCardCd = openCardCd;
    }

    public Integer getBetMaxScore() {
        return betMaxScore;
    }

    public void setBetMaxScore(Integer betMaxScore) {
        this.betMaxScore = betMaxScore;
    }

    public Integer getGameNum() {
        return gameNum;
    }

    public void setGameNum(Integer gameNum) {
        this.gameNum = gameNum;
    }

    public Integer getIsWatch() {
        return isWatch;
    }

    public void setIsWatch(Integer isWatch) {
        this.isWatch = isWatch;
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public Integer getTableState() {
        return tableState;
    }

    public void setTableState(Integer tableState) {
        this.tableState = tableState;
    }

    @Override
    public String toString() {
        return "TableCreationRecordsModel{" +
                "id=" + id +
                ", tableId='" + tableId + '\'' +
                ", createTime=" + createTime +
                ", playerNum=" + playerNum +
                ", baseScore=" + baseScore +
                ", readyCd=" + readyCd +
                ", betCd=" + betCd +
                ", openCardCd=" + openCardCd +
                ", betMaxScore=" + betMaxScore +
                ", gameNum=" + gameNum +
                ", betMultiple='" + betMultiple + '\'' +
                ", isWatch=" + isWatch +
                ", createUserId='" + createUserId + '\'' +
                ", tableState=" + tableState +
                ", gameId='" + gameId + '\'' +
                '}';
    }
}