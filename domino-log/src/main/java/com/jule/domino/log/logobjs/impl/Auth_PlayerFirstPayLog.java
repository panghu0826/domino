package com.jule.domino.log.logobjs.impl;

import com.jule.domino.log.logobjs.AbstractPlayerLog;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 首次付费日志
 *
 * @author ran
 */
@Entity
public class Auth_PlayerFirstPayLog extends AbstractPlayerLog {

    /**appid*/
    private String appid;
    /**渠道*/
    private String channel;

    private String pid;

    private String price;

    private String statement;

    private String orderId;

    private String totalReward;

    /**下单时间*/
    private String orderTime;


    @Column(length = 128)
    public String getAppid() {
        return appid;
    }
    @Column(length = 128)
    public String getChannel() {
        return channel;
    }
    @Column(length = 128)
    public String getPid() {
        return pid;
    }
    @Column(length = 128)
    public String getPrice() {
        return price;
    }
    @Column(columnDefinition = "text default null")
    public String getStatement() {
        return statement;
    }
    @Column(columnDefinition = "text default null")
    public String getOrderId() {
        return orderId;
    }
    @Column(length = 128)
    public String getTotalReward() {
        return totalReward;
    }
    @Column(length = 128)
    public String getOrderTime() {
        return orderTime;
    }

    public void setAppid( String appid ) {
        this.appid = appid;
    }

    public void setChannel( String channel ) {
        this.channel = channel;
    }

    public void setPid( String pid ) {
        this.pid = pid;
    }

    public void setPrice( String price ) {
        this.price = price;
    }

    public void setStatement( String statement ) {
        this.statement = statement;
    }

    public void setOrderId( String orderId ) {
        this.orderId = orderId;
    }

    public void setTotalReward( String totalReward ) {
        this.totalReward = totalReward;
    }

    public void setOrderTime( String orderTime ) {
        this.orderTime = orderTime;
    }
}
