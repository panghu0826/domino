package com.jule.domino.log.logobjs.impl;

import com.jule.domino.log.logobjs.AbstractPlayerLog;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 角色登出日志
 *
 * @author ran
 */
@Entity
public class Auth_PlayerlogoutLog extends AbstractPlayerLog {

    /**
     * 登出时间
     */
    private long logoutTime;

    /**
     * 在线时长:单位秒
     */
    private long onlineTime;

    @Column
    public long getLogoutTime() {
        return logoutTime;
    }
    @Column
    public long getOnlineTime() {
        return onlineTime;
    }

    public void setLogoutTime( long logoutTime ) {
        this.logoutTime = logoutTime;
    }

    public void setOnlineTime( long onlineTime ) {
        this.onlineTime = onlineTime;
    }
}
