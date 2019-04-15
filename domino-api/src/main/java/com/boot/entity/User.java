package com.boot.entity;

import com.boot.cache.StoredObj;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @author
 * @since 2018/7/18 18:46
 */
@Entity
@Table(name = "user")
@Setter
@Getter
@ToString
public class User extends StoredObj {
    @Id
    private String id;

    private String nick_name;

    private String ico_url;

    private Long money;

    private Date registration_time;

    private Date last_login;

    private Date last_offline;

    private String client_version;
    /**用户的渠道ID guest facebook robot*/
    private String channel_id;

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
