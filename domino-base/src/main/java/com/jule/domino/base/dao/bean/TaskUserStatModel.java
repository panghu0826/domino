package com.jule.domino.base.dao.bean;

import com.jule.core.jedis.StoredObj;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;

@Data
@EqualsAndHashCode(callSuper=false)
public class TaskUserStatModel extends StoredObj {
    private Long id = 0L;

    private String userId;

    private String taskGameId;

    private Byte taskType;

    private Integer value;

    private Integer tmpValue = 0;

    private Timestamp createTime;

    private Timestamp updateTime;

}