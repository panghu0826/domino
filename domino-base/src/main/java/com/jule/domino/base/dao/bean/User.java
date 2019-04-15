package com.jule.domino.base.dao.bean;

import com.jule.core.jedis.StoredObj;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
@Setter@Getter@ToString
public class User extends StoredObj{
    private String id;

    private String nick_name;

    private String ico_url;

    private double money;

    private Date registration_time;

    private Date last_login;

    private Date last_offline;

    private String client_version;
    /**用户的渠道ID guest facebook robot*/
    private String channel_id;
    //渠道子ID
    private String sub_channel_id;

    private String user_ip;

    private Integer platform;

    private String down_platform;

    /**用户所用设备的编号*/
    private String device_num;

    private String user_defined_head;

    private String android_id;

    private String mei_code;
    /**包名*/
    private String package_name;

}