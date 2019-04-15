package com.jule.domino.log.logobjs.impl;

import com.jule.domino.log.logobjs.AbstractPlayerLog;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 游戏sitdown 日志
 *
 * @author ran
 */
@Entity
public class Game_GameSitLog extends AbstractPlayerLog {

    private String gameId;

    private String roomId;

    private String tableId;

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

}
