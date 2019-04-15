package com.jule.domino.dispacher.dao.mapper;

import com.jule.domino.base.dao.bean.TaskUserStatModel;

import java.util.List;

public interface TaskUserStatModelMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TaskUserStatModel record);

    TaskUserStatModel selectByPrimaryKey(Long id);

    List<TaskUserStatModel> selectAll();

    List<TaskUserStatModel> selectAllByUserId(String userId);

    int updateByPrimaryKey(TaskUserStatModel record);
}