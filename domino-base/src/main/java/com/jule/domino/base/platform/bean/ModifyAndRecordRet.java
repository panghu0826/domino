package com.jule.domino.base.platform.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @since 2019/2/28 16:53
 */
@Setter@Getter
public class ModifyAndRecordRet {

    //0为成功，其它都是错误
    private int code;

    private String msg;
    //数据结果
    private Map<String, UserModifyBean> result = new HashMap<>();

}
