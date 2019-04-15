package com.jule.domino.base.dao.bean;
import com.jule.core.jedis.StoredObj;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;
@Data
@EqualsAndHashCode(callSuper=false)
public class TaskConfigModel extends StoredObj {
    private Integer taskId;

    private String taskGameId;

    private Byte taskType;

    private String taskName;

    private String taskIcon;

    private String taskDesc;

    private Integer taskTargetValue;

    private Timestamp createTime;

    private Timestamp updateTime;

}