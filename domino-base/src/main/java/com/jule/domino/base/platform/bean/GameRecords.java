package com.jule.domino.base.platform.bean;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ran.wang
 * @since 2018/11/26 17:41
 */
@Setter
@Getter
public class GameRecords {
    private String user_id;

    private String game_id;

    private String room_id;

    private String room_type;

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
    //type=1 系统暗抽金额、type=2奖池放水金额等、type=3机器人获利金额、type=4买保险金额、type=5德州买牌金额，type=6系统赠送
    private String platform_profit="1";


    /** set方法重写 兼容大厅数据库 */
    public void setInit_balance(String init_balance) {
        if (StringUtils.isEmpty(init_balance)){
            this.init_balance = "0";
        }else {
            this.init_balance = init_balance;
        }
    }

    public void setBalance(String balance) {
        if (StringUtils.isEmpty(balance)){
            this.balance = "0";
        }else {
            this.balance = balance;
        }
    }

    public void setAll_bet(String all_bet) {
        if (StringUtils.isEmpty(all_bet)){
            this.all_bet = "0";
        }else {
            this.all_bet = all_bet;
        }
    }

    public void setAvail_bet(String avail_bet) {
        if (StringUtils.isEmpty(avail_bet)){
            this.avail_bet = "0";
        }else {
            this.avail_bet = avail_bet;
        }
    }

    public void setProfit(String profit) {
        if (StringUtils.isEmpty(profit)){
            this.profit = "0";
        }else {
            this.profit = profit;
        }
    }

    public void setRevenue(String revenue) {
        if (StringUtils.isEmpty(revenue)){
            this.revenue = "0";
        }else {
            this.revenue = revenue;
        }
    }

}
