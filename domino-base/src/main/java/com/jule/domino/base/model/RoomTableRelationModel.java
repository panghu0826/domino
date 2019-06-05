package com.jule.domino.base.model;

import com.jule.core.jedis.StoredObj;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * 房间与桌子关联对象
 */
@Getter@Setter
public class RoomTableRelationModel extends StoredObj {
    private String gameId;
    private String roomId;
    private String tableId;
    /**TableStateEnum的状态*/
    private int tableStatus=0;

    public RoomTableRelationModel(String gameId, String roomId, String tableId,int tableStatus) {
        this.tableId = tableId;
        this.tableStatus = tableStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RoomTableRelationModel that = (RoomTableRelationModel) o;
        return Objects.equals(gameId, that.gameId) &&
                Objects.equals(roomId, that.roomId) &&
                Objects.equals(tableId, that.tableId);
    }

    public String getCompareCode(){
        return getGameId() + "_"+getRoomId()+"_"+getTableId();
    }

    @Override
    public int hashCode() {

        return Objects.hash(getCompareCode());
    }
}
