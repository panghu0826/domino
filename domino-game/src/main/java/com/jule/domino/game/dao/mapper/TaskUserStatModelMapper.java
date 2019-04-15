package com.jule.domino.game.dao.mapper;

import com.jule.domino.base.dao.bean.TaskUserStatModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskUserStatModelMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TaskUserStatModel record);

    TaskUserStatModel selectByPrimaryKey(Long id);

    TaskUserStatModel selectByUserIdGameIdTaskType(@Param("userId")String userId ,
                                                   @Param("taskGameId")String gameId,
                                                   @Param("taskType")Integer type);

    List<TaskUserStatModel> selectAll();

    int updateByPrimaryKey(TaskUserStatModel record);
}