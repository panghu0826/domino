package com.jule.domino.game.dao.bean;

import lombok.Data;

import java.sql.Timestamp;
@Data
public class GiftHistoryModel {
    private long id;
    private String userId;
    private String targetUserId;
    private String giftItemId;
    private int count;
    private int playType;
    private String roomId;
    private String tableId;
    private Timestamp createTime;

}
