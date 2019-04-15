package com.jule.domino.game.dao.mapper;

import com.jule.domino.base.dao.bean.TaskRelationConfigModel;

import java.util.List;

public interface TaskRelationConfigModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TaskRelationConfigModel record);

    TaskRelationConfigModel selectByPrimaryKey(Integer id);

    List<TaskRelationConfigModel> selectAll();

    int updateByPrimaryKey(TaskRelationConfigModel record);
}