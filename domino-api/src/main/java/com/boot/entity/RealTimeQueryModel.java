package com.boot.entity;

public class RealTimeQueryModel {

    private String gameId;
    private String roomId;
    private String tableId;
    private int totalPlayers;
    private int totalRobots;
    private int inSeatPlayers;
    private int inSeatRobots;
    private long ante;
    private String detailed;

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

    public int getTotalPlayers() {
        return totalPlayers;
    }

    public void setTotalPlayers(int totalPlayers) {
        this.totalPlayers = totalPlayers;
    }

    public int getTotalRobots() {
        return totalRobots;
    }

    public void setTotalRobots(int totalRobots) {
        this.totalRobots = totalRobots;
    }

    public int getInSeatPlayers() {
        return inSeatPlayers;
    }

    public void setInSeatPlayers(int inSeatPlayers) {
        this.inSeatPlayers = inSeatPlayers;
    }

    public int getInSeatRobots() {
        return inSeatRobots;
    }

    public void setInSeatRobots(int inSeatRobots) {
        this.inSeatRobots = inSeatRobots;
    }

    public long getAnte() {
        return ante;
    }

    public void setAnte(long ante) {
        this.ante = ante;
    }

    public String getDetailed() {
        return detailed;
    }

    public void setDetailed(String detailed) {
        this.detailed = detailed;
    }

    @Override
    public String toString() {
        return "RealTimeQueryModel{" +
                "gameId='" + gameId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", tableId='" + tableId + '\'' +
                ", totalPlayers=" + totalPlayers +
                ", totalRobots=" + totalRobots +
                ", inSeatPlayers=" + inSeatPlayers +
                ", inSeatRobots=" + inSeatRobots +
                ", ante=" + ante +
                ", detailed='" + detailed + '\'' +
                '}';
    }
}
