package com.jule.domino.base.platform.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * @author
 * @since 2018/11/26 17:41
 */
@Setter
@Getter
public class Records{
    private String user_id;

    private String game_id;

    private String room_id;

    private String table_id;

    private String seat_id;

    private String user_count;

    private String round_id;

    private String card_value;

    private String init_balance;

    private String balance;

    private String all_bet;

    private String avail_bet;
    //用户获利
    private String profit;
    //平台抽水
    private String revenue;

    private String start_time;

    private String end_time;

    private String channel_id;

    private String sub_channel_id;

}
