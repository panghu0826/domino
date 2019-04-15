package com.jule.core.model;

import com.jule.core.jedis.StoredObj;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
public class ServerState extends StoredObj {
    //停服开始时间
    private long startTime ;
    //停服截止时间
    private long endTime;
    //停服描述
    private String details;
}
