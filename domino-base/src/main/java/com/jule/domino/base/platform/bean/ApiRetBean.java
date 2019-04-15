package com.jule.domino.base.platform.bean;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

/**
 * 返回对象
 * @author
 * @since 2018/11/26 15:58
 */
@Getter@Setter
public class ApiRetBean {
    //0为成功，其它都是错误
    private int code;

    private String msg;
    //数据结果
    private JsonObject result;
    //格式YYYY-MM-DD HH:mm:ss
    private String time;

}
