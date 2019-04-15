package com.jule.domino.log.logobjs.impl;

import com.jule.domino.log.logobjs.AbstractPlayerLog;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 重连日日志
 *
 * @author ran
 */
@Entity
public class Game_ReconnectLog extends AbstractPlayerLog {

    //断线时长
    private long offTime;

    @Column
    public long getOffTime() {
        return offTime;
    }

    public void setOffTime( long offTime ) {
        this.offTime = offTime;
    }
}
