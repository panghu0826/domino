package com.jule.domino.game.dao.mapper;

import com.jule.domino.base.dao.bean.TaskUserHistModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskUserHistModelMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TaskUserHistModel record);

    TaskUserHistModel selectByPrimaryKey(Long id);

    List<TaskUserHistModel> selectAll();

    TaskUserHistModel selectByLineIdUserIdGameIdTaskType(@Param("userId")String userId,
                                                         @Param("lineId")Integer lineId,
                                                         @Param("taskId")Integer taskId,
                                                         @Param("taskGameId")String taskGameId,
                                                         @Param("taskType")Integer taskType);

    int updateByPrimaryKey(TaskUserHistModel record);
}