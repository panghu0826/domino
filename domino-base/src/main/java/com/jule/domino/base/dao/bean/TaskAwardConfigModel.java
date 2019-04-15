package com.jule.domino.base.dao.bean;

import com.jule.core.jedis.StoredObj;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter@Getter
public class TaskAwardConfigModel extends StoredObj{
    private Integer id;

    private Integer taskId;

    private String itemId;

    private Integer type;

    private String url;

    private String name;

    private Integer num;

    private Timestamp createTime;

    private Timestamp updateTime;

}