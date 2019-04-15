package com.jule.domino.log.logobjs.impl;

import com.jule.domino.log.logobjs.AbstractPlayerLog;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 游戏开局统计
 *
 * @author ran
 */
@Entity
public class Game_GameStartLog extends AbstractPlayerLog {

    private String gameId;

    private String roomId;

    private String tableId;

    /**局内玩家*/
    private String players;

    private int playerNum;

    /**本局服务费*/
    private double serviceFree;

    private String startTime;

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
    @Column(length = 512)
    public String getPlayers() {
        return players;
    }

    public void setPlayers( String players ) {
        this.players = players;
    }
    @Column(length = 128)
    public int getPlayerNum() {
        return playerNum;
    }

    public void setPlayerNum( int playerNum ) {
        this.playerNum = playerNum;
    }
    @Column(length = 128)
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime( String startTime ) {
        this.startTime = startTime;
    }
    @Column
    public double getServiceFree() {
        return serviceFree;
    }

    public void setServiceFree( double serviceFree ) {
        this.serviceFree = serviceFree;
    }
}
