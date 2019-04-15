package com.jule.domino.log.logobjs.impl;

import com.jule.domino.log.logobjs.AbstractPlayerLog;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 头像更换日志
 *
 * @author ran
 */
@Entity
public class Game_IcoChangeLog extends AbstractPlayerLog {

    private String orgIco;

    private String curIco;

    @Column(columnDefinition = "text default null")
    public String getOrgIco() {
        return orgIco;
    }
    @Column(columnDefinition = "text default null")
    public String getCurIco() {
        return curIco;
    }

    public void setOrgIco( String orgIco ) {
        this.orgIco = orgIco;
    }

    public void setCurIco( String curIco ) {
        this.curIco = curIco;
    }
}
