package com.jule.domino.game.dao.mapper;


import com.jule.domino.game.dao.bean.TaskUserAwardLogModel;

import java.util.List;

public interface TaskUserAwardLogModelMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TaskUserAwardLogModel record);

    TaskUserAwardLogModel selectByPrimaryKey(Long id);

    List<TaskUserAwardLogModel> selectAll();

    int updateByPrimaryKey(TaskUserAwardLogModel record);
}