package com.jule.domino.game.dao.bean;

import org.eclipse.persistence.jpa.jpql.parser.DateTime;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GameRecordModel {
    private Integer id;

    private Integer gameId;

    private String tableId;

    private Integer currGameNum;

    private String userId;

    private String nickName;

    private Integer cardType;

    private String handCards;

    private Integer totalTableScore;

    private Integer winLoseScore;

    private Date startTime;

    private Date endTime;

    private Integer playerCurrScore;

    private String icoUrl;

    public String getIcoUrl() {
        return icoUrl;
    }

    public void setIcoUrl(String icoUrl) {
        this.icoUrl = icoUrl;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId == null ? null : tableId.trim();
    }

    public Integer getCurrGameNum() {
        return currGameNum;
    }

    public void setCurrGameNum(Integer currGameNum) {
        this.currGameNum = currGameNum;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName == null ? null : nickName.trim();
    }

    public Integer getCardType() {
        return cardType;
    }

    public void setCardType(Integer cardType) {
        this.cardType = cardType;
    }

    public String getHandCards() {
        return handCards;
    }

    public void setHandCards(String handCards) {
        this.handCards = handCards == null ? null : handCards.trim();
    }

    public Integer getTotalTableScore() {
        return totalTableScore;
    }

    public void setTotalTableScore(Integer totalTableScore) {
        this.totalTableScore = totalTableScore;
    }

    public Integer getWinLoseScore() {
        return winLoseScore;
    }

    public void setWinLoseScore(Integer winLoseScore) {
        this.winLoseScore = winLoseScore;
    }

    public String getStartTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTime);
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endTime);
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getPlayerCurrScore() {
        return playerCurrScore;
    }

    public void setPlayerCurrScore(Integer playerCurrScore) {
        this.playerCurrScore = playerCurrScore;
    }
}