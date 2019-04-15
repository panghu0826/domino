package com.jule.domino.game.notice.model;

/**
 * 存储Gate服务器信息
 */
public class GateServerInfo {
    private String GateSvrId;
    private String Ip;
    private String Port;

    public String getGateSvrId() {
        return GateSvrId;
    }

    public void setGateSvrId(String gateSvrId) {
        GateSvrId = gateSvrId;
    }

    public String getIp() {
        return Ip;
    }

    public void setIp(String ip) {
        Ip = ip;
    }

    public String getPort() {
        return Port;
    }

    public void setPort(String port) {
        Port = port;
    }
}
