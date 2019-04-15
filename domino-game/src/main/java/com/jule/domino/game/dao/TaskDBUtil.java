package com.jule.domino.game.dao;

import com.jule.core.database.DatabaseFactory;
import com.jule.domino.base.dao.bean.*;
import com.jule.domino.game.dao.bean.TaskUserAwardLogModel;
import com.jule.domino.game.dao.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

@Slf4j
public class TaskDBUtil {
    /**
     * 读取任务线配置列表
     *
     * @return
     */
    public static List<TaskLineConfigModel> getTaskLineConfigFromDb() {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<TaskLineConfigModel> ret = null;
        try {
            ret = sqlSession.getMapper(TaskLineConfigModelMapper.class).selectAll();
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return ret;
    }

    /**
     * 读取任务关联配置列表
     *
     * @return
     */
    public static List<TaskRelationConfigModel> getTaskRelationConfigFromDb() {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<TaskRelationConfigModel> ret = null;
        try {
            ret = sqlSession.getMapper(TaskRelationConfigModelMapper.class).selectAll();
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return ret;
    }

    /**
     * 获取任务配置列表
     *
     * @return
     */
    public static List<TaskConfigModel> getTaskConfigFromDb() {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<TaskConfigModel> ret = null;
        try {
            ret = sqlSession.getMapper(TaskConfigModelMapper.class).selectAll();
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return ret;
    }

    /**
     * 获取任务配置列表
     *
     * @return
     */
    public static Integer updateByPrimaryKey(TaskUserHistModel model) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(TaskUserHistModelMapper.class).updateByPrimaryKey(model);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return count;
    }

    /**
     * 获取任务配置列表
     *
     * @return
     */
    public static List<TaskAwardConfigModel> getTaskAwardConfigFromDb(int taskId) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<TaskAwardConfigModel> ret = null;
        try {
            ret = sqlSession.getMapper(TaskAwardConfigModelMapper.class).selectByTaskId(taskId);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return ret;
    }


    /**
     * 插入or修改数据
     *
     * @param taskUserStat
     */
    public static TaskUserStatModel insertOrUpdateTaskUserStat(TaskUserStatModel taskUserStat) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        TaskUserStatModel taskUserStatModel = null;
        int count = 0;
        try {
            if (taskUserStat.getId() == 0L) {
                count = sqlSession.getMapper(TaskUserStatModelMapper.class).insert(taskUserStat);
                if(count>0){
                    taskUserStatModel = sqlSession.getMapper(TaskUserStatModelMapper.class).selectByUserIdGameIdTaskType(
                            taskUserStat.getUserId(),taskUserStat.getTaskGameId(),Integer.parseInt(""+taskUserStat.getTaskType()));
                }
            } else {
                count = sqlSession.getMapper(TaskUserStatModelMapper.class).updateByPrimaryKey(taskUserStat);
                taskUserStatModel = taskUserStat;
            }
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return taskUserStatModel;
    }
    /**
     * 插入用户信息
     *
     * @param
     */
    public static TaskUserHistModel insert(TaskUserHistModel task) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        TaskUserHistModel taskUserHistModel = null;
        int count = 0;
        try {
            count = sqlSession.getMapper(TaskUserHistModelMapper.class).insert(task);

            sqlSession.commit();
            if(count>0){
                taskUserHistModel = sqlSession.getMapper(TaskUserHistModelMapper.class).selectByLineIdUserIdGameIdTaskType(
                        task.getUserId(),task.getLineId(),task.getTaskId(),task.getTaskGameId(),Integer.parseInt(""+task.getTaskType()));
            }
            sqlSession.commit();
        } catch (Exception e) {
            log.error("insert", e.toString());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return taskUserHistModel;
    }
    public static int insertUserTaskAwardLog(TaskUserAwardLogModel tasklog){
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(TaskUserAwardLogModelMapper.class).insert(tasklog);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return count;
    }
}
