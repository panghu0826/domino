package com.jule.domino.game.dao.mapper;

import com.jule.domino.base.dao.bean.TaskLineConfigModel;

import java.util.List;

public interface TaskLineConfigModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TaskLineConfigModel record);

    TaskLineConfigModel selectByPrimaryKey(Integer id);

    List<TaskLineConfigModel> selectAll();

    int updateByPrimaryKey(TaskLineConfigModel record);
}