package com.jule.domino.game.api.model;

public class RealTimeQueryModel {

    private String gameId;
    private String roomId;
    private String tableId;
    private int players;
    private int robots;
    private long ante;
    private String detailed;

    public String getDetailed() {
        return detailed;
    }

    public void setDetailed(String detailed) {
        this.detailed = detailed;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public int getPlayers() {
        return players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public int getRobots() {
        return robots;
    }

    public void setRobots(int robots) {
        this.robots = robots;
    }

    public long getAnte() {
        return ante;
    }

    public void setAnte(long ante) {
        this.ante = ante;
    }
}
