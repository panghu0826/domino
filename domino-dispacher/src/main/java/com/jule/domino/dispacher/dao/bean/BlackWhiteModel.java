package com.jule.domino.dispacher.dao.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * 黑名单、白名单表
 */
@Setter@Getter
public class BlackWhiteModel {

    //玩家UID
    private String uid;
    //1-是黑名单
    private int black;
    //1-是白名单
    private int white;

}