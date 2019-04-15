package com.jule.robot.model;

/**
 * 存储Gate服务器信息
 */
public class GateServerInfo {
    private String GateSvrId;
    private String Ip;
    private int Port;

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

    public int getPort() {
        return Port;
    }

    public void setPort(int port) {
        Port = port;
    }
}
