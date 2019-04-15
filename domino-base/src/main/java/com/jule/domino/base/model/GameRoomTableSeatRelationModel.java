package com.jule.domino.base.model;

import com.jule.core.jedis.StoredObj;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class GameRoomTableSeatRelationModel extends StoredObj {
    private String gameId;
    private String roomId;
    private String tableId;
    private int seat = 0;
    private String gameSvr = "";
}
