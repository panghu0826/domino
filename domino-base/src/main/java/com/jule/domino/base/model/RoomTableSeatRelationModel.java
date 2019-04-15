package com.jule.domino.base.model;

import com.jule.core.jedis.StoredObj;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class RoomTableSeatRelationModel extends StoredObj {
    private String roomId;
    private String tableId;
    private int seat;
}
