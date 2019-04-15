package com.jule.domino.base.model;

import com.jule.core.jedis.StoredObj;
import lombok.Data;

/**
 * 缓存对象
 *
 * @author
 *
 * @since 2018/9/11 20:13
 */
@Data
public class PlayerRecordsModel extends StoredObj {
    private String uid;
    //游戏盘局ID
    private String gameOrderId;
    //房间ID
    private String roomId;
    //桌子ID
    private String tableId;
    //净胜
    private double wins;
    //时间
    private String time;

    private boolean isWin;

}
