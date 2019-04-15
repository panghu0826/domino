package com.jule.db.entities;

import com.jule.db.dao.BaseDbBean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import java.util.Date;

@Entity
@Table(name = "user")
public class User extends BaseDbBean{

    @Id
    private String id;
    @Column
    private String nick_name;
    @Column
    private String ico_url;
    @Column
    private Double money;
    @Column
    private Date last_login;
    @Column
    private String client_version;
    @Column
    private String channel_id;
    @Column
    private String user_ip;
    @Column
    private Integer platform;
    @Column
    private String device_num;
    @Column
    private String user_defined_head;
    @Column
    private String android_id;
    @Column
    private String mei_code;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId( long id ) {
        this.id = String.valueOf(id);
    }

    public void setId( String id ) {
        this.id = id;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name( String nick_name ) {
        this.nick_name = nick_name;
    }

    public String getIco_url() {
        return ico_url;
    }

    public void setIco_url( String ico_url ) {
        this.ico_url = ico_url;
    }

    public Double getMoney() {
        return money;
    }

    public void setMoney( Double money ) {
        this.money = money;
    }

    public Date getLast_login() {
        return last_login;
    }

    public void setLast_login( Date last_login ) {
        this.last_login = last_login;
    }

    public String getClient_version() {
        return client_version;
    }

    public void setClient_version( String client_version ) {
        this.client_version = client_version;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id( String channel_id ) {
        this.channel_id = channel_id;
    }

    public String getUser_ip() {
        return user_ip;
    }

    public void setUser_ip( String user_ip ) {
        this.user_ip = user_ip;
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform( Integer platform ) {
        this.platform = platform;
    }

    public String getDevice_num() {
        return device_num;
    }

    public void setDevice_num( String device_num ) {
        this.device_num = device_num;
    }

    public String getUser_defined_head() {
        return user_defined_head;
    }

    public void setUser_defined_head( String user_defined_head ) {
        this.user_defined_head = user_defined_head;
    }

    public String getAndroid_id() {
        return android_id;
    }

    public void setAndroid_id( String android_id ) {
        this.android_id = android_id;
    }

    public String getMei_code() {
        return mei_code;
    }

    public void setMei_code( String mei_code ) {
        this.mei_code = mei_code;
    }
}
