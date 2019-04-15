package com.jule.domino.notice.model;

import com.google.protobuf.MessageLite;

public class NormalMsgInfo {
    private int gameId;
    private String userId;
    private byte[] msgContent;
    private int reqNum;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public byte[] getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(byte[] msgContent) {
        this.msgContent = msgContent;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getReqNum() {
        return reqNum;
    }

    public void setReqNum(int reqNum) {
        this.reqNum = reqNum;
    }
}


