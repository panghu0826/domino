package com.jule.domino.dispacher.dao.mapper;

import com.jule.domino.dispacher.dao.bean.RewardReceiveRecordModel;

import java.util.List;

public interface RewardReceiveRecordModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RewardReceiveRecordModel record);

    RewardReceiveRecordModel selectByPrimaryKey(String playerId);

    List<RewardReceiveRecordModel> selectAll();

    int updateByPrimaryKey(RewardReceiveRecordModel record);
}