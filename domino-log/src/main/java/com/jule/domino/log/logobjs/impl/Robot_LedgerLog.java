package com.jule.domino.log.logobjs.impl;

import com.jule.domino.log.logobjs.AbstractPlayerLog;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Robot_LedgerLog extends AbstractPlayerLog {

    //玩法
    private String playType;
    //资金池改变之前
    private String org_capitalPool;
    //资金池改变之后
    private String cur_capitalPool;
    //改变的筹码数量
    private String changeChips;

    @Column(length = 24)
    public String getPlayType() {
        return playType;
    }

    public void setPlayType(String playType) {
        this.playType = playType;
    }

    @Column
    public String getChangeChips() {
        return changeChips;
    }

    public void setChangeChips(String changeChips) {
        this.changeChips = changeChips;
    }

    @Column
    public String getOrg_capitalPool() {
        return org_capitalPool;
    }

    public void setOrg_capitalPool(String org_capitalPool) {
        this.org_capitalPool = org_capitalPool;
    }

    @Column
    public String getCur_capitalPool() {
        return cur_capitalPool;
    }

    public void setCur_capitalPool(String cur_capitalPool) {
        this.cur_capitalPool = cur_capitalPool;
    }
}
