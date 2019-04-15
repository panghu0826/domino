package com.boot.entity;

import javax.persistence.Entity;
import java.text.SimpleDateFormat;
import java.util.Date;
public class DetailedInformationModel {

    private String playerId;
    private String nickName;
    private String registrationTime;
    private long money;
    private String isPlayer;
    private String isSeat;

    public String getIsSeat() {
        return isSeat;
    }

    public void setIsSeat(String isSeat) {
        this.isSeat = isSeat;
    }

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

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
    }

    public String getIsPlayer() {
        return isPlayer;
    }

    public void setIsPlayer(String isPlayer) {
        this.isPlayer = isPlayer;
    }

}
