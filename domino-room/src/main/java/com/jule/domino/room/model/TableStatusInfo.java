package com.jule.domino.room.model;

import JoloProtobuf.RoomSvr.JoloRoom;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import java.util.HashMap;
import java.util.Map;

public class TableStatusInfo {

    private String tableId;
    private int CurrentPlayerCount;
    private int currentWaitingCount;
    private String gameSvrId;
    private Map<String, PlayerInfo> infoMap = new HashMap<>();

    public TableStatusInfo(String tableId, String gameSvrId) {
        this.tableId = tableId;
        this.gameSvrId = gameSvrId;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public int getCurrentPlayerCount() {
        return CurrentPlayerCount;
    }

    public void setCurrentPlayerCount(int currentPlayerCount) {
        CurrentPlayerCount = Math.max(0, currentPlayerCount);
    }

    public int getCurrentWaitingCount() {
        return currentWaitingCount;
    }

    public void setCurrentWaitingCount(int currentWaitingCount) {
        this.currentWaitingCount = Math.max(0, currentWaitingCount);
    }

    public String getGameSvrId() {
        return gameSvrId;
    }

    public Map<String, PlayerInfo> getInfoMap() {
        return infoMap;
    }

    public void setGameSvrId(String gameSvrId) {
        this.gameSvrId = gameSvrId;
    }

    public static class PlayerInfo {
        private String tableId;
        private String userId;
        private String nick;

        public PlayerInfo(String tableId, String userId,String nick) {
            this.tableId = tableId;
            this.userId = userId;
            this.nick = nick;
        }

        /**
         * 昵称显示名
         * @return
         */
        public String getNick() {
            return nick;
        }

        /**
         * 昵称显示名
         * @param nick
         */
        public void setNick(String nick) {
            this.nick = nick;
        }

        @Override
        public String toString() {
            return "PlayerInfo{" +
                    "tableId='" + tableId + '\'' +
                    ", userId='" + userId + '\'' +
                    ", nick='" + nick + '\'' +
                    '}';
        }

        /**
         * @param info
         * @return
         */
        public static PlayerInfo fromProtocBuf(JoloRoom.JoloRoom_Table_UserInfo info) {
            return new PlayerInfo(info.getTableId(), info.getUserId(),info.getNickName());
        }

        /**
         * @return
         */
        public JoloRoom.JoloRoom_Table_UserInfo toProtocBuf() {
            return JoloRoom.JoloRoom_Table_UserInfo.newBuilder().setTableId(this.tableId).setUserId(this.userId).setNickName(this.nick).build();
        }
    }

    /**
     * protoc 还原成对象
     *
     * @param bytes
     * @return
     */
    public static TableStatusInfo fromProtocBuf(byte[] bytes) {
        try {
            JoloRoom.JoloRoom_Table_StatusInfo info = JoloRoom.JoloRoom_Table_StatusInfo.parseFrom(bytes);
            TableStatusInfo tableStatusInfo = new TableStatusInfo(info.getTableId(), info.getGameSvrId());
            tableStatusInfo.setCurrentPlayerCount(info.getCurrPlayerCnt());
            tableStatusInfo.setCurrentWaitingCount(info.getWaitingPlayerCnt());
            if (info.getPlayersInfoList() != null && info.getPlayersInfoList().size() > 0) {
                info.getPlayersInfoList().forEach(playerInfo -> {
                    PlayerInfo playerInfo1 = PlayerInfo.fromProtocBuf(playerInfo);
                    tableStatusInfo.infoMap.put(playerInfo1.userId, playerInfo1);
                });
            }
            return tableStatusInfo;
        } catch (InvalidProtocolBufferException e) {
            return null;
        }
    }

    /**
     * @return
     */
    public MessageLite toBuf() {
        JoloRoom.JoloRoom_Table_StatusInfo.Builder builder = JoloRoom.JoloRoom_Table_StatusInfo.newBuilder();
        builder.setTableId(this.tableId)
                .setWaitingPlayerCnt(this.currentWaitingCount)
                .setCurrPlayerCnt(this.getCurrentPlayerCount())
                .setGameSvrId(this.gameSvrId);
        this.infoMap.forEach((s, playerInfo) -> builder.addPlayersInfo(playerInfo.toProtocBuf()));

        return builder.build();
    }

    @Override
    public String toString() {
        return "TableStatusInfo{" +
                "tableId='" + tableId + '\'' +
                ", CurrentPlayerCount=" + CurrentPlayerCount +
                ", currentWaitingCount=" + currentWaitingCount +
                ", gameSvrId='" + gameSvrId + '\'' +
                ", infoMap=" + infoMap +
                '}';
    }
}
