package com.jule.domino.game.dao.mapper;

import com.jule.domino.base.dao.bean.TaskAwardConfigModel;

import java.util.List;

public interface TaskAwardConfigModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TaskAwardConfigModel record);

    TaskAwardConfigModel selectByPrimaryKey(Integer id);

    List<TaskAwardConfigModel> selectAll();

    List<TaskAwardConfigModel> selectByTaskId(Integer taskId);

    int updateByPrimaryKey(TaskAwardConfigModel record);
}