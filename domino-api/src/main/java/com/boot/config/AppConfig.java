package com.boot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 游戲配置文件
 *
 * @author
 *
 * @since 2018/7/18 18:56
 *
 */
@Configuration
@ConfigurationProperties(prefix = "game")
public class AppConfig {

    private String appid;

    private String accessip;

    private String itemServer;

    private int gameId;

    public String getAppid() {
        return appid;
    }

    public void setAppid( String appid ) {
        this.appid = appid;
    }

    public String getAccessip() {
        return accessip;
    }

    public void setAccessip( String accessip ) {
        this.accessip = accessip;
    }

    public String getItemServer() {
        return itemServer;
    }

    public void setItemServer(String itemServer) {
        this.itemServer = itemServer;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }
}
