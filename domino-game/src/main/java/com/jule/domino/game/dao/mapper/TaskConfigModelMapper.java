package com.jule.domino.game.dao.mapper;

import com.jule.domino.base.dao.bean.TaskConfigModel;

import java.util.List;

public interface TaskConfigModelMapper {
    int deleteByPrimaryKey(Integer taskId);

    int insert(TaskConfigModel record);

    TaskConfigModel selectByPrimaryKey(Integer taskId);

    List<TaskConfigModel> selectAll();

    int updateByPrimaryKey(TaskConfigModel record);
}