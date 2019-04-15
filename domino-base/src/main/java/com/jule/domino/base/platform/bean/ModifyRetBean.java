package com.jule.domino.base.platform.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * 修改返回对象
 * @author
 * @since 2018/11/26 16:06
 */
@Getter@Setter
public class ModifyRetBean {
    //执行状态True成功
    private boolean flag;

    private String order_id;

    private Balance balance;

}

