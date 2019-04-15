package com.jule.domino.base.dao.bean;

import lombok.Data;

@Data
public class Product {
    private String pid;

    private Double price;

    private Integer total_reward;

    private Integer base_reward;

    private Integer extra_reward;

    private Integer extra_percent;

    private Integer ico_count;

    private Integer pos;

    private String tag;

    private String app_id;

    private String pay_channel;

    private String contain_type;
    /**物品的配置Id*/
    private int contain_item_id;

}