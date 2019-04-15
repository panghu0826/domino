package com.jule.domino.base.dao.bean;

import com.jule.core.jedis.StoredObj;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;

@Data
@EqualsAndHashCode(callSuper=false)
public class TaskRelationConfigModel extends StoredObj {
    private Integer id;

    private Integer lineId;

    private Integer taskId;

    private Integer parentTaskId;

    private Integer childTaskId;
    private Timestamp createTime;
    private Timestamp updateTime;
}