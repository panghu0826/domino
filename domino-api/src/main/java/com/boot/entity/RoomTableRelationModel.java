package com.boot.entity;

import com.boot.cache.StoredObj;
import lombok.Data;

import javax.persistence.Entity;
import java.io.Serializable;
/**
 * 房间与桌子关联对象
 */
@Data
public class RoomTableRelationModel extends StoredObj {

    private String gameId;
    private String roomId;
    private String tableId;

   /* public RoomTableRelationModel(String gameId, String roomId, String tableId) {
        this.gameId = gameId;
        this.roomId = roomId;
        this.tableId = tableId;
    }*/
}
