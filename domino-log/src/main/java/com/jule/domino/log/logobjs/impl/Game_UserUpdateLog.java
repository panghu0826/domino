package com.jule.domino.log.logobjs.impl;

import com.jule.domino.log.logobjs.AbstractPlayerLog;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * user表更新日志
 *
 * @author ran
 */
@Entity
public class Game_UserUpdateLog extends AbstractPlayerLog {

    /**
     * 操作json参数
     */
    private String oper ;

    @Column(columnDefinition = "text default null")
    public String getOper() {
        return oper;
    }

    public void setOper( String oper ) {
        this.oper = oper;
    }
}
