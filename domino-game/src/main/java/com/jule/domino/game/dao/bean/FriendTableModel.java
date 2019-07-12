package com.jule.domino.game.dao.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter@Getter
public class FriendTableModel {
    private Integer id;

    private String userId;

    private String friendUserId;

    private String friendNickName;

    private String friendIcoUrl;

    private Date addTime;

    private Integer state;
}