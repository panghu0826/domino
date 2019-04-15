package com.jule.domino.room.dao;

import com.jule.core.database.DatabaseFactory;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.room.dao.bean.CommonConfigModel;
import com.jule.domino.room.dao.bean.DeskConfig;
import com.jule.domino.room.dao.bean.RoomConfigModel;
import com.jule.domino.room.dao.mapper.CommonConfigModelMapper;
import com.jule.domino.room.dao.mapper.DeskConfigMapper;
import com.jule.domino.room.dao.mapper.RoomConfigModelMapper;
import com.jule.domino.room.dao.mapper.UserMapper;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xujian on 2017/5/18 0018.
 */
public class DBUtil {

    private static final Logger logger = LoggerFactory.getLogger(DBUtil.class);

    /**
     * 读取房间配置列表
     *
     * @return
     */
    public static List<DeskConfig> getDeskConfigFromDb() {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<DeskConfig> ret = new ArrayList<>();
        try {
            ret = sqlSession.getMapper(DeskConfigMapper.class).selectAll();
            sqlSession.commit();
        }catch (Exception e){
            logger.error(e.getMessage());
            sqlSession.rollback();
        }finally {
            sqlSession.close();
        }
        return ret;
    }

    /**
     * 读取房间配置列表
     *
     * @return
     */
    public static List<RoomConfigModel> getRoomConfigFromDb() {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<RoomConfigModel> ret = null;
        try {
            ret = sqlSession.getMapper(RoomConfigModelMapper.class).selectAll();
            sqlSession.commit();
        } catch (Exception e){
            logger.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return ret;
    }

    /**
     * 读取通用配置列表
     * @return
     */
    public static List<CommonConfigModel> getCommonConfigFromDb() {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<CommonConfigModel> ret = null;
        try {
            ret = sqlSession.getMapper(CommonConfigModelMapper.class).selectAll();
            sqlSession.commit();
        } catch (Exception e){
            logger.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return ret;
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
            logger.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return user;
    }
}
