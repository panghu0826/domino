package com.jule.domino.base.platform.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 推送记录
 * @author
 * @since 2018/11/26 16:13
 */
@Setter@Getter
public class RecordReqBean {

    private List<Records> records = new ArrayList<>();

}

