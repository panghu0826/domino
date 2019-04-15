package com.jule.domino.base.platform.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 返回对象
 * @since 2018/11/26 15:58
 */
@Getter@Setter
public class PlayerRecordsRet {
    //0为成功，其它都是错误
    private int code;

    private String msg;
    //数据结果
    private List<PlayerRecords> result;
}
