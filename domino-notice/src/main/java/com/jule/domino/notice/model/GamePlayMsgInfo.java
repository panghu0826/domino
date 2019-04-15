package com.jule.domino.notice.model;

import JoloProtobuf.NoticeSvr.JoloNotice;

import java.util.List;

public class GamePlayMsgInfo {
    private int gameId;
    private String tableId;
    private List<String> userIdList;
    private int reqNum;

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public JoloNotice.JoloNotice_SendGamePlayMsgReq getReqObj() {
        return reqObj;
    }

    public void setReqObj(JoloNotice.JoloNotice_SendGamePlayMsgReq reqObj) {
        this.reqObj = reqObj;
    }

    private JoloNotice.JoloNotice_SendGamePlayMsgReq reqObj;

    public List<String> getUserIdList() {
        return userIdList;
    }

    public void setUserIdList(List<String> userIdList) {
        this.userIdList = userIdList;
    }

    public int getReqNum() {
        return reqNum;
    }

    public void setReqNum(int reqNum) {
        this.reqNum = reqNum;
    }
}
