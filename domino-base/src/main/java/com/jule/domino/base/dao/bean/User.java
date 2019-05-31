package com.jule.domino.base.dao.bean;

import com.jule.core.jedis.StoredObj;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.regex.Pattern;

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

    private int total_game_num;
//    private String encodeNickName;
//
//    public String getEncodeNickName() {
//        if(!isBase64(nick_name)) {
//            try {
//                nick_name = Base64.encodeBase64String(nick_name.getBytes("utf-8"));
//            } catch (UnsupportedEncodingException e) {
//                System.out.println("玩家昵称转码异常：" + nick_name);
//            }
//        }
//        return nick_name;
//    }
//
//    public String getNick_name() {
//        String nickName = null;
//        if(isBase64(nick_name)){
//            try {
//                nickName = new String(Base64.decodeBase64(nick_name.getBytes()), "utf-8");
//            } catch (UnsupportedEncodingException e) {
//                System.out.println("玩家昵称解码异常："+nickName);
//            }
//        }
//        return nickName == null ? nick_name : nickName;
//    }

//    public void setNick_name(String nick_name) {
//        try {
//            nick_name = Base64.encodeBase64String(nick_name.getBytes("utf-8"));
//        } catch (UnsupportedEncodingException e) {
//            System.out.println("玩家昵称转码异常："+nick_name);
//        }
//        this.nick_name = nick_name;
//    }

    public static boolean isBase64(String str) {
        String base64Pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
        return Pattern.matches(base64Pattern, str);
    }

    @Override
    public String toString() {
        return "User{" +
                "nick_name='" + nick_name + '\'' +
                '}';
    }
}