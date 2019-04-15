package com.jule.domino.dispacher.dao.mapper;

import com.jule.domino.base.dao.bean.TaskUserHistModel;

import java.util.List;

public interface TaskUserHistModelMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TaskUserHistModel record);

    TaskUserHistModel selectByPrimaryKey(Long id);

    List<TaskUserHistModel> selectAll();

    List<TaskUserHistModel> selectByUserId(String userId);

    int updateByPrimaryKey(TaskUserHistModel record);
}