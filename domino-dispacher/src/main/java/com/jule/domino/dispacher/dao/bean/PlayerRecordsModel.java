package com.jule.domino.dispacher.dao.bean;

import com.jule.core.jedis.StoredObj;

/**
 * 缓存对象
 *
 * @author
 *
 * @since 2018/9/11 20:13
 */
public class PlayerRecordsModel extends StoredObj {
    private String uid;
    //游戏盘局ID
    private String gameOrderId;
    //房间ID
    private String roomId;
    //桌子ID
    private String tableId;
    //净胜
    private double wins;
    //时间
    private String time;

    private boolean isWin;

    public boolean isWin() {
        return isWin;
    }

    public void setWin(boolean win) {
        isWin = win;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getGameOrderId() {
        return gameOrderId;
    }

    public void setGameOrderId(String gameOrderId) {
        this.gameOrderId = gameOrderId;
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

    public double getWins() {
        return wins;
    }

    public void setWins(double wins) {
        this.wins = wins;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
