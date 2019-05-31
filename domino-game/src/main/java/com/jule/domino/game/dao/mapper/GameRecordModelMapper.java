package com.jule.domino.game.dao.mapper;

import com.jule.domino.game.dao.bean.GameRecordModel;
import java.util.List;

public interface GameRecordModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(GameRecordModel record);

    List<GameRecordModel> selectByTableId(String tableId);

    List<GameRecordModel> selectByUserId(String userId);

    int updateByPrimaryKey(GameRecordModel record);
}