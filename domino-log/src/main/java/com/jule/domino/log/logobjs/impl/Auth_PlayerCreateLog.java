package com.jule.domino.log.logobjs.impl;

import com.jule.domino.log.logobjs.AbstractPlayerLog;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 角色建角日志
 *
 * @author ran
 */
@Entity
public class Auth_PlayerCreateLog extends AbstractPlayerLog {

    /**
     * 登录时间
     */
    private String createTime;

    private String giveMoney;
    @Column
    public String getGiveMoney() {
        return giveMoney;
    }

    public void setGiveMoney(String giveMoney) {
        this.giveMoney = giveMoney;
    }

    @Column(length = 128)
    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime( String createTime ) {
        this.createTime = createTime;
    }
}
