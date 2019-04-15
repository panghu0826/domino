package com.jule.domino.log.logobjs.impl;

import com.jule.domino.log.logobjs.AbstractPlayerLog;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Game_GameLoseWinLog extends AbstractPlayerLog {
    private String gameId;

    private String roomId;

    private String tableId;

    private String gameOrderId;

    private String LoseWin;

    private String score;

    @Column(length = 128)
    public String getLoseWin() {
        return LoseWin;
    }

    public void setLoseWin(String loseWin) {
        LoseWin = loseWin;
    }
    @Column
    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    @Column(length = 256)
    public String getGameOrderId() {
        return gameOrderId;
    }

    public void setGameOrderId(String gameOrderId) {
        this.gameOrderId = gameOrderId;
    }

    @Column(length = 128)
    public String getGameId() {
        return gameId;
    }

    public void setGameId( String gameId ) {
        this.gameId = gameId;
    }
    @Column(length = 128)
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId( String roomId ) {
        this.roomId = roomId;
    }
    @Column(length = 128)
    public String getTableId() {
        return tableId;
    }

    public void setTableId( String tableId ) {
        this.tableId = tableId;
    }

    @Override
    public String toString() {
        return "Game_GameLoseWinLog{" +
                "gameId='" + gameId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", tableId='" + tableId + '\'' +
                ", gameOrderId='" + gameOrderId + '\'' +
                ", LoseWin='" + LoseWin + '\'' +
                ", score=" + score +
                '}';
    }
}
