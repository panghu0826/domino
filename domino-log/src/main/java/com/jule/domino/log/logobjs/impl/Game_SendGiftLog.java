package com.jule.domino.log.logobjs.impl;

import com.jule.domino.log.logobjs.AbstractPlayerLog;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 送礼日志
 *
 * @author ran
 */
@Entity
public class Game_SendGiftLog extends AbstractPlayerLog {
    //礼物ID
    private String giftID;
    //礼物名称
    private String giftName;
    //送给谁
    private String sendTo;
    //费用
    private String fee;

    @Column(length = 64)
    public String getGiftID() {
        return giftID;
    }

    public void setGiftID( String giftID ) {
        this.giftID = giftID;
    }
    @Column(length = 64)
    public String getGiftName() {
        return giftName;
    }

    public void setGiftName( String giftName ) {
        this.giftName = giftName;
    }
    @Column(length = 256)
    public String getSendTo() {
        return sendTo;
    }

    public void setSendTo( String sendTo ) {
        this.sendTo = sendTo;
    }
    @Column
    public String getFee() {
        return fee;
    }

    public void setFee( String fee ) {
        this.fee = fee;
    }
}
