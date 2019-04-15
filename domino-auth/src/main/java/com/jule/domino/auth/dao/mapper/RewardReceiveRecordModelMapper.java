package com.jule.domino.auth.dao.mapper;

import com.jule.domino.auth.dao.bean.RewardReceiveRecordModel;

import java.util.List;

public interface RewardReceiveRecordModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RewardReceiveRecordModel record);

    RewardReceiveRecordModel selectByPrimaryKey(Integer id);

    List<RewardReceiveRecordModel> selectAll();

    int updateByPrimaryKey(RewardReceiveRecordModel record);
}