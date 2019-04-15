package com.jule.domino.base.platform.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * @author
 * @since 2018/11/19 12:03
 */
@Getter@Setter
public class PlatUserBean {

    //用户ID
    private String id;
    //用户名称
    private String username;
    //昵称
    private String nickname;
    //渠道ID
    private String channel_id;
    //渠道子ID
    private String sub_channel_id;
    //用户平台
    private String platform;
    //头像
    private String icon;
    //货币金额
    private Double gold;
    //有效货币金额
    private Double valid_gold;
    //Server端后续操作
    private String token;
    //用户创建时间
    private String create_time;
    //邀请码
    private String invite_code;

    public PlatUserBean() {
    }

    public String getIcon(){
        if ("".equals(this.icon)){
            return "1";
        }
        return this.icon;
    }
}
