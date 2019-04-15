package com.jule.domino.auth.dao.mapper;

import com.jule.domino.auth.dao.bean.RewardConfigModel;

import java.util.List;

public interface RewardConfigModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RewardConfigModel record);

    RewardConfigModel selectByPrimaryKey(Integer id);

    List<RewardConfigModel> selectAll();

    int updateByPrimaryKey(RewardConfigModel record);
}