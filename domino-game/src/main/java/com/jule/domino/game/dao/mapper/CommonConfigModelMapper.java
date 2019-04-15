package com.jule.domino.game.dao.mapper;

import com.jule.domino.game.dao.bean.CommonConfigModel;

import java.util.List;

public interface CommonConfigModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CommonConfigModel record);

    CommonConfigModel selectByPrimaryKey(Integer id);

    List<CommonConfigModel> selectAll();

    int updateByPrimaryKey(CommonConfigModel record);
}