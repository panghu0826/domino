package com.jule.domino.gate.dao;

import com.jule.core.database.DatabaseFactory;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.gate.dao.mapper.UserMapper;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by xujian on 2017/5/18 0018.
 */
public class DBUtil {
    private static final Logger logger = LoggerFactory.getLogger(DBUtil.class);
    /**
     * 插入用户信息
     *
     * @param
     */
    public static int insert(User user) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(UserMapper.class).insert(user);
            sqlSession.commit();
        } catch (Exception e) {
            logger.error("insert",e);
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return count;
    }

    /**
     * 查询用户信息
     * @param
     * @return
     */
    public static User selectByPrimaryKey(String user_id) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        User user = null;
        try {
            user = sqlSession.getMapper(UserMapper.class).selectByPrimaryKey(user_id);
            sqlSession.commit();
        } catch (Exception e) {
            logger.error("selectByPrimaryKey",e);
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return user;
    }

    /**
     * 查询所有用户信息
     * @param
     * @return
     */
    public static List<User> selectAll() {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<User> list = null;
        try {
            list = sqlSession.getMapper(UserMapper.class).selectAll();
            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            logger.error("selectAll",e);
        } finally {
            sqlSession.close();
        }
        return list;
    }

    /**
     * 修改用户信息
     * @param
     * @return
     */
    public static int updateByPrimaryKey(User user) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(UserMapper.class).updateByPrimaryKey(user);
            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            logger.error("updateByPrimaryKey",e);
        } finally {
            sqlSession.close();
        }
        return count;
    }

    /**
     * 删除用户信息
     * @param
     * @return
     */
    public static int deleteByPrimaryKey(String user_id) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(UserMapper.class).deleteByPrimaryKey(user_id);
            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            logger.error("deleteByPrimaryKey",e);
        } finally {
            sqlSession.close();
        }
        return count;
    }

}
