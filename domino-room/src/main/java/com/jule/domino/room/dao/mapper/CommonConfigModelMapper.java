package com.jule.domino.room.dao.mapper;

import com.jule.domino.room.dao.bean.CommonConfigModel;

import java.util.List;

public interface CommonConfigModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CommonConfigModel record);

    CommonConfigModel selectByPrimaryKey(Integer id);

    List<CommonConfigModel> selectAll();

    int updateByPrimaryKey(CommonConfigModel record);
}