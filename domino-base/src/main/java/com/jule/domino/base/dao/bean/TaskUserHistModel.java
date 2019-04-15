package com.jule.domino.base.dao.bean;

import com.jule.core.jedis.StoredObj;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;

@EqualsAndHashCode(callSuper=false)
@Data
public class TaskUserHistModel extends StoredObj {
    private Long id;

    private String userId;

    private Integer lineId;

    private Integer taskId;

    private String taskGameId;

    private Byte taskType;

    private Integer taskTargetValue;

    private Byte taskStatus;

    private Timestamp createTime;

    private Timestamp updateTime;

}