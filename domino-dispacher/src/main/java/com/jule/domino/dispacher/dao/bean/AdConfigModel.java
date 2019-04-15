package com.jule.domino.dispacher.dao.bean;

import com.jule.core.jedis.StoredObj;
import lombok.Getter;
import lombok.Setter;

/**
 * 静态参数配置表
 */
@Setter@Getter
public class AdConfigModel extends StoredObj{

    //序号
    private int id;
    //次数限制
    private int frequency;
    //筹码限制
    private int chipNumber;
}