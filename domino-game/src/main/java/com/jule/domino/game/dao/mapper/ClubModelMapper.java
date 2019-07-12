package com.jule.domino.game.dao.mapper;

import com.jule.domino.game.dao.bean.ClubModel;
import java.util.List;

public interface ClubModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ClubModel record);

    ClubModel selectByPrimaryKey(Integer id);

    ClubModel selectLastData(String userId);

    List<ClubModel> selectByUserId(String userId);

    int updateByPrimaryKey(ClubModel record);
}