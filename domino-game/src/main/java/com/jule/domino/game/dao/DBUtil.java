package com.jule.domino.game.dao;

import com.jule.core.database.DatabaseFactory;
import com.jule.domino.game.dao.bean.CommonConfigModel;
import com.jule.domino.game.dao.bean.GiftHistoryModel;
import com.jule.domino.game.dao.mapper.*;
import com.jule.domino.base.dao.bean.Product;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.game.dao.bean.*;
import com.jule.domino.game.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * Created by xujian on 2017/5/18 0018.
 */
@Slf4j
public class DBUtil {
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
            log.error(e.getMessage());
            sqlSession.rollback();
        }finally {
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
        }catch (Exception e){
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return ret;
    }

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
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }

        if (count>0){
            LogService.OBJ.sendUserUpdateLog(user);
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
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return user;
    }

    /**
     * 查询用户信息
     * @param
     * @return
     */
    public static User selectByOpenId(String openId) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        User user = null;
        try {
            List<User> list = sqlSession.getMapper(UserMapper.class).selectUserByOpenId(openId);
            sqlSession.commit();
            if (list != null && list.size() >0){
                user = list.get(0);
            }
        } catch (Exception e) {
            log.error("selectByPrimaryKey",e);
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
            log.error(e.getMessage());
            sqlSession.rollback();
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
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        if (count > 0){
            LogService.OBJ.sendUserUpdateLog(user);
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
            log.error(e.getMessage());
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
    public static CommonConfigModel selectCommon(int id) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        CommonConfigModel ccm = null;
        try {
            ccm = sqlSession.getMapper(CommonConfigModelMapper.class).selectByPrimaryKey(id);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return ccm;
    }

    /**
     * 查询所有通用配置
     * @param
     * @return
     */
    public static List<CommonConfigModel> selectAllCommon() {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<CommonConfigModel> list = null;
        try {
            list = sqlSession.getMapper(CommonConfigModelMapper.class).selectAll();
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return list;
    }


    /**
     * 插入用户信息
     *
     * @param
     */
    public static int insertCommon(CommonConfigModel record) {
        CommonConfigModel c = new CommonConfigModel();
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(CommonConfigModelMapper.class).insert(record);
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
     * 修改用户信息
     * @param
     * @return
     */
    public static int updateCommon(CommonConfigModel record) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(CommonConfigModelMapper.class).updateByPrimaryKey(record);
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
     * 查询用户信息
     * @param
     * @return
     */
    public static RoomConfigModel selectRoom(int id) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        RoomConfigModel rcm = null;
        try {
            rcm = sqlSession.getMapper(RoomConfigModelMapper.class).selectByPrimaryKey(id);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return rcm;
    }

    /**
     * 插入用户信息
     *
     * @param
     */
    public static int insertRoom(RoomConfigModel record) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(RoomConfigModelMapper.class).insert(record);
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
     * 修改房间信息
     * @param
     * @return
     */
    public static int updateRoom(RoomConfigModel record) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(RoomConfigModelMapper.class).updateByPrimaryKey(record);
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
     * 查询所有用户信息
     * @param
     * @return
     */
    public static List<RoomConfigModel> selectAllRoom() {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<RoomConfigModel> list = null;
        try {
            list = sqlSession.getMapper(RoomConfigModelMapper.class).selectAll();
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return list;
    }

    /**
     * 插入打赏记录
     *
     * @param
     */
    public static int insertTipHistory(TipHistoryModel record) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(TipHistoryModelMapper.class).insert(record);
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
     * 插入送礼记录
     *
     * @param
     */
    public static int insertGiftHistory(GiftHistoryModel record) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(GiftHistoryModelMapper.class).insert(record);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return count;
    }

    public static List<Product> selectAllData(String containType) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<Product> list = null;
        try {
            list = sqlSession.getMapper(ProductMapper.class).selectAllByType(containType);
            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            log.error("selectAllData", e);
        } finally {
            sqlSession.close();
        }
        return list;
    }
    public static List<CardTypeMultipleModel> getAllCardTypeMultiple(){
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<CardTypeMultipleModel> list = null;
        try {
            list = sqlSession.getMapper(CardTypeMultipleModelMapper.class).selectAll();
            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            log.error("selectAllData", e);
        } finally {
            sqlSession.close();
        }
        return list;
    }

    public static int updateOnlineRoles(String roles) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(RoomConfigModelMapper.class).updateOnlineRoles(roles);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return count;
    }
}
