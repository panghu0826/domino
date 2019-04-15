package com.jule.domino.log.logobjs.impl;

import com.jule.domino.log.logobjs.AbstractPlayerLog;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 荷官更换日志
 *
 * @author ran
 */
@Entity
public class Game_DealerChangeLog extends AbstractPlayerLog {

    private String dealerID;

    private long fee;

    @Column(length = 64)
    public String getDealerID() {
        return dealerID;
    }

    public void setDealerID( String dealerID ) {
        this.dealerID = dealerID;
    }
    @Column
    public long getFee() {
        return fee;
    }

    public void setFee( long fee ) {
        this.fee = fee;
    }
}
