package com.jule.domino.log.logobjs.impl;

import com.jule.domino.log.logobjs.AbstractPlayerLog;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 游戏筹码统计
 *
 * @author ran
 */
@Entity
public class Game_PlayerChipsLog extends AbstractPlayerLog {

    /**变化前*/
    private String org_chips;
    /**变化后*/
    private String cur_chips;
    /**变化值*/
    private String change;

    @Column
    public String getOrg_chips() {
        return org_chips;
    }
    @Column
    public String getCur_chips() {
        return cur_chips;
    }
    @Column
    public String getChange() {
        return change;
    }

    public void setOrg_chips( String org_chips ) {
        this.org_chips = org_chips;
    }

    public void setCur_chips( String cur_chips ) {
        this.cur_chips = cur_chips;
    }

    public void setChange( String change ) {
        this.change = change;
    }

}
