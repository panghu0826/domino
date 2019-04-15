package com.jule.domino.base.platform.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * @author
 * @since 2019/2/28 16:53
 */
@Setter@Getter
public class ModifyAndRecord {

    private ModifyReqBean account;

    private GameRecords game_record;
}
