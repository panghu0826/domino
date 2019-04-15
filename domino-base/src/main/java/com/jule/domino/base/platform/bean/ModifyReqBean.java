package com.jule.domino.base.platform.bean;

import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.enums.RoleType;
import com.jule.domino.base.platform.HallAPIService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 修改请求对象
 *
 * @author
 * @since 2018/11/26 16:06
 */
@Getter
@Setter
@ToString
public class ModifyReqBean {
    //用户ID
    private String user_id;
    //操作行为  下注bet  返奖profit  使用use  结算settlement  其他other
    private String behavior;
    //增加为正数，减少为负数
    private String gold;

    private String valid_gold = "0";
    //格式：0_yyyyMMddHHmmssSSS_{game_id}_{user_id}_内部编号，长度48位以内
    private String order_id;

    private String game_id;

    private String room_id;

    private String seat_id;

    private String round_id;

    private String comment = "report";


}

