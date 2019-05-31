package com.jule.domino.game.dao.mapper;

import com.jule.domino.game.dao.bean.RoomCardRecordsModel;
import java.util.List;

public interface RoomCardRecordsModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RoomCardRecordsModel record);

    RoomCardRecordsModel selectByPrimaryKey(Integer id);

    RoomCardRecordsModel selectRoomCardByMoneyToken(String moneyToken);

    List<RoomCardRecordsModel> selectRoomCardByUserId(String userId);

    List<RoomCardRecordsModel> selectAll();

    int updateByPrimaryKey(RoomCardRecordsModel record);
}