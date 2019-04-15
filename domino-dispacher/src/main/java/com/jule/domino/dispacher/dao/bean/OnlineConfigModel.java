package com.jule.domino.dispacher.dao.bean;

public class OnlineConfigModel {
    private Integer id;

    private String grouping;

    private Byte activitySwitch;

    private Byte mailSwitch;

    private Byte advertisingSwitch;

    private String gameOrder;

    private Byte playnowTurn;

    private Byte exitAdvertising;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGrouping() {
        return grouping;
    }

    public void setGrouping(String grouping) {
        this.grouping = grouping == null ? null : grouping.trim();
    }

    public Byte getActivitySwitch() {
        return activitySwitch;
    }

    public void setActivitySwitch(Byte activitySwitch) {
        this.activitySwitch = activitySwitch;
    }

    public Byte getMailSwitch() {
        return mailSwitch;
    }

    public void setMailSwitch(Byte mailSwitch) {
        this.mailSwitch = mailSwitch;
    }

    public Byte getAdvertisingSwitch() {
        return advertisingSwitch;
    }

    public void setAdvertisingSwitch(Byte advertisingSwitch) {
        this.advertisingSwitch = advertisingSwitch;
    }

    public String getGameOrder() {
        return gameOrder;
    }

    public void setGameOrder(String gameOrder) {
        this.gameOrder = gameOrder == null ? null : gameOrder.trim();
    }

    public Byte getPlaynowTurn() {
        return playnowTurn;
    }

    public void setPlaynowTurn(Byte playnowTurn) {
        this.playnowTurn = playnowTurn;
    }

    public Byte getExitAdvertising() {
        return exitAdvertising;
    }

    public void setExitAdvertising(Byte exitAdvertising) {
        this.exitAdvertising = exitAdvertising;
    }
}