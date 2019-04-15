package com.jule.domino.log.logobjs.impl;

import com.jule.domino.log.logobjs.AbstractPlayerLog;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 游戏结算统计
 *
 * @author ran
 */
@Entity
public class Game_GameSettleLog extends AbstractPlayerLog {

    private String gameId;

    private String roomId;

    private String tableId;

    private String playType;

    /**结算玩家们*/
    private String players;

    private String winner;

    /**本局服务费*/
    private double serviceFree;

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
    @Column
    public double getServiceFree() {
        return serviceFree;
    }

    public void setServiceFree( double serviceFree ) {
        this.serviceFree = serviceFree;
    }
    @Column(length = 128)
    public String getWinner() {
        return winner;
    }

    public void setWinner( String winner ) {
        this.winner = winner;
    }
    @Column(length = 128)
    public String getPlayType() {
        return playType;
    }
    public void setPlayType( String playType ) {
        this.playType = playType;
    }
}
