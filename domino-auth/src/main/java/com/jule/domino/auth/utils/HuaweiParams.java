package com.jule.domino.auth.utils;

/**
 * @author
 * @since 2018/8/14 16:32
 */
public class HuaweiParams {
    private String playerId;
    private String playerLevel;
    private String ts;
    private String gameAuthSign;
    private String displayName;
    private String isAuth;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId( String playerId ) {
        this.playerId = playerId;
    }

    public String getPlayerLevel() {
        return playerLevel;
    }

    public void setPlayerLevel( String playerLevel ) {
        this.playerLevel = playerLevel;
    }

    public String getTs() {
        return ts;
    }

    public void setTs( String ts ) {
        this.ts = ts;
    }

    public String getGameAuthSign() {
        return gameAuthSign;
    }

    public void setGameAuthSign( String gameAuthSign ) {
        this.gameAuthSign = gameAuthSign;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName( String displayName ) {
        this.displayName = displayName;
    }

    public String getIsAuth() {
        return isAuth;
    }

    public void setIsAuth( String isAuth ) {
        this.isAuth = isAuth;
    }
}
