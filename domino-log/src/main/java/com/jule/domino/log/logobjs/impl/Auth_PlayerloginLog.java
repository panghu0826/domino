package com.jule.domino.log.logobjs.impl;

import com.jule.domino.log.logobjs.AbstractPlayerLog;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 角色登录日志
 *
 * @author ran
 */
@Entity
public class Auth_PlayerloginLog extends AbstractPlayerLog {

    /**
     * 登录时间
     */
    private String loginTime;

    /**登录ip*/
    private String ipAddress;

    /**设备识别*/
    private String idfa;

    @Column(length = 128)
    public String getLoginTime() {
        return loginTime;
    }
    @Column(length = 128)
    public String getIpAddress() {
        return ipAddress;
    }
    @Column(length = 256)
    public String getIdfa() {
        return idfa;
    }

    public void setLoginTime( String loginTime ) {
        this.loginTime = loginTime;
    }

    public void setIpAddress( String ipAddress ) {
        this.ipAddress = ipAddress;
    }

    public void setIdfa( String idfa ) {
        this.idfa = idfa;
    }
}
