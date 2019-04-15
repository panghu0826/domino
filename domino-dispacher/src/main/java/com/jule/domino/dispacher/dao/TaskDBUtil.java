package com.jule.domino.dispacher.dao;

import com.jule.core.database.DatabaseFactory;
import com.jule.domino.base.dao.bean.TaskUserHistModel;
import com.jule.domino.base.dao.bean.TaskUserStatModel;
import com.jule.domino.dispacher.dao.mapper.TaskUserHistModelMapper;
import com.jule.domino.dispacher.dao.mapper.TaskUserStatModelMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

@Slf4j
public class TaskDBUtil {
    /**
     * 插入用户信息
     *
     * @param
     */
    public static int insert(TaskUserHistModel task) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(TaskUserHistModelMapper.class).insert(task);
            sqlSession.commit();
        } catch (Exception e) {
            log.error("insert", e.toString());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return count;
    }
    /**
     * 获取玩家自身任务列表
     * @return
     */
    public static List<TaskUserHistModel> getTaskUserHistFromDb(String userId){
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<TaskUserHistModel> ret = null;
        try {
            ret = sqlSession.getMapper(TaskUserHistModelMapper.class).selectByUserId(userId);
            sqlSession.commit();
        } catch (Exception e){
            log.error(e.getMessage());
            sqlSession.rollback();
        }finally {
            sqlSession.close();
        }
        return ret;
    }

    /**
     * 获取玩家任务状态列表
     * @return
     */
    public static List<TaskUserStatModel> getTaskUserStatFromDb(String userId){
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<TaskUserStatModel> ret = null;
        try {
            ret = sqlSession.getMapper(TaskUserStatModelMapper.class).selectAllByUserId(userId);
            sqlSession.commit();
        } catch (Exception e){
            log.error(e.getMessage());
            sqlSession.rollback();
        }finally {
            sqlSession.close();
        }
        return ret;
    }
}
