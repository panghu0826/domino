package com.jule.domino.dispacher.dao.mapper;

import com.jule.domino.dispacher.dao.bean.RewardConfigModel;
import java.util.List;

public interface RewardConfigModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RewardConfigModel record);

    RewardConfigModel selectByPrimaryKey(Integer id);

    List<RewardConfigModel> selectAll();

    int updateByPrimaryKey(RewardConfigModel record);
}