package com.jule.domino.log.logobjs.impl;

import com.jule.domino.log.logobjs.AbstractPlayerLog;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Game_OnlineNumberLog extends AbstractPlayerLog {

    /**统计的时间*/
    private long currTime;
    /**统计的服务器*/
    private String server;
    /**玩家人数*/
    private int playerNumber;
    /**统计的服*/
    private String position;

    @Column
    public long getCurrTime() {
        return currTime;
    }

    public void setCurrTime(long currTime) {
        this.currTime = currTime;
    }

    @Column
    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    @Column
    public int getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    @Column
    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
