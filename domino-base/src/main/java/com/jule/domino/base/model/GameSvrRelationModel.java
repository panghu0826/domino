package com.jule.domino.base.model;

import com.jule.core.jedis.StoredObj;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 包手游戏模块和邮件模块
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class GameSvrRelationModel extends StoredObj {
    private String address;
    private String gameSvrId;
}
