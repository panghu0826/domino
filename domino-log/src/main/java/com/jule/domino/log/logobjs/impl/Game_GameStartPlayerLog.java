package com.jule.domino.log.logobjs.impl;


import com.jule.domino.log.logobjs.AbstractPlayerLog;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Game_GameStartPlayerLog extends AbstractPlayerLog {

    private String gameId;

    private String roomId;

    private String tableId;

    private String gameOrderId;

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

}
