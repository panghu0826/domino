package com.jule.domino.gate.model;

/**
 * @author xujian 2018-02-26
 */
public class SubGame {
    private int gameId;
    private int gameServerId;
    private String ip;
    private int port;
    private int onlineNum;

    public SubGame(int gameId, int gameServerId, String ip, int port, int onlineNum) {
        this.gameId = gameId;
        this.gameServerId = gameServerId;
        this.ip = ip;
        this.port = port;
        this.onlineNum = onlineNum;
    }

    public int getGameId() {
        return gameId;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getGameServerId() {
        return gameServerId;
    }

    public int getOnlineNum() {
        return onlineNum;
    }

    public void setOnlineNum(int onlineNum) {
        this.onlineNum = onlineNum;
    }

    @Override
    public String toString() {
        return "SubGame{" +
                "gameId=" + gameId +
                ", gameServerId=" + gameServerId +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", onlineNum=" + onlineNum +
                '}';
    }
}
