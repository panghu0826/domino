package com.jule.domino.game.api.model;

import com.hazelcast.spi.impl.eventservice.impl.Registration;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailedInformationModel {

    private String playerId;
    private String nickName;
    private String registrationTime;
    private double money;
    private int isPlayer;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(Date registrationTime) {
        if(registrationTime != null)
        this.registrationTime = sdf.format(registrationTime);
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public int getIsPlayer() {
        return isPlayer;
    }

    public void setIsPlayer(int isPlayer) {
        this.isPlayer = isPlayer;
    }
}
