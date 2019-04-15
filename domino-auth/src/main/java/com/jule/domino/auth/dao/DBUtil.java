package com.jule.domino.auth.dao;


import com.jule.domino.auth.dao.bean.Currency;
import com.jule.domino.auth.dao.bean.Payment;
import com.jule.domino.auth.dao.bean.RewardConfigModel;
import com.jule.domino.auth.dao.bean.RewardReceiveRecordModel;
import com.jule.domino.auth.dao.mapper.*;
import com.jule.domino.auth.service.LogService;
import com.jule.core.database.DatabaseFactory;
import com.jule.domino.base.dao.bean.Product;
import com.jule.domino.base.dao.bean.User;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * //todo cache in redis
 * Created by xujian on 2017/5/18 0018.
 */
public class DBUtil {
    private static final Logger logger = LoggerFactory.getLogger(DBUtil.class);

    /**
     * 插入订单
     *
     * @param
     */
    public static int insertPayment(Payment payment) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(PaymentMapper.class).insert(payment);
            sqlSession.commit();
        } catch (Exception e) {
            logger.error("insert payment", e);
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return count;
    }

    /**
     * 查询用户信息
     *
     * @param
     * @return
     */
    public static Payment selectByStatement(String statement) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        Payment payment = null;
        try {
            payment = sqlSession.getMapper(PaymentMapper.class).selectByStatement(statement);
            sqlSession.commit();
        } catch (Exception e) {
            logger.error("selectByPrimaryKey", e);
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return payment;
    }

    /**
     * 修改用户信息
     *
     * @param
     * @return
     */
    public static int updateByPrimaryKey(Payment payment) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(PaymentMapper.class).updateByPrimaryKey(payment);
            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            logger.error("updateByPrimaryKey", e);
        } finally {
            sqlSession.close();
        }
        return count;
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
            logger.error("insert", e);
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
     *
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
            logger.error("selectByPrimaryKey", e);
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return user;
    }

    /**
     * 查询所有用户信息
     *
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
            logger.error("selectAll", e);
        } finally {
            sqlSession.close();
        }
        return list;
    }

    public static List<Product> selectAllData(String containType) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<Product> list = null;
        try {
            list = sqlSession.getMapper(ProductMapper.class).selectAllByType(containType);
            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            logger.error("selectAllData", e);
        } finally {
            sqlSession.close();
        }
        return list;
    }

    public static List<Product> selectAllData() {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<Product> list = null;
        try {
            list = sqlSession.getMapper(ProductMapper.class).selectAll();
            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            logger.error("selectAllData", e);
        } finally {
            sqlSession.close();
        }
        return list;
    }

    /**
     * 修改用户信息
     *
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
            logger.error("updateByPrimaryKey", e);
        } finally {
            sqlSession.close();
        }

        if (count>0){
            LogService.OBJ.sendUserUpdateLog(user);
        }
        return count;
    }

    /**
     * 删除用户信息
     *
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
            logger.error("deleteByPrimaryKey", e);
        } finally {
            sqlSession.close();
        }
        return count;
    }

    /**
     * 插入用户货币信息
     *
     * @param
     */
    public static int insertCurrency(Currency currency) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(CurrencyMapper.class).insert(currency);
            sqlSession.commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
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
    public static RewardConfigModel selectRewardConfig(int id) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        RewardConfigModel rcm = null;
        try {
            rcm = sqlSession.getMapper(RewardConfigModelMapper.class).selectByPrimaryKey(id);
            sqlSession.commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return rcm;
    }

    /**
     * 查询所有用户信息
     * @param
     * @return
     */
    public static List<RewardConfigModel> selectAllRewardConfig() {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<RewardConfigModel> list = null;
        try {
            list = sqlSession.getMapper(RewardConfigModelMapper.class).selectAll();
            sqlSession.commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
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
    public static int insertRewardConfig(RewardConfigModel record) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(RewardConfigModelMapper.class).insert(record);
            sqlSession.commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
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
    public static int updateRewardConfig(RewardConfigModel record) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(RewardConfigModelMapper.class).updateByPrimaryKey(record);
            sqlSession.commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
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
    public static RewardReceiveRecordModel selectRewardReceiveRecord(int id) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        RewardReceiveRecordModel rcm = null;
        try {
            rcm = sqlSession.getMapper(RewardReceiveRecordModelMapper.class).selectByPrimaryKey(id);
            sqlSession.commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return rcm;
    }

    /**
     * 查询所有用户信息
     * @param
     * @return
     */
    public static List<RewardReceiveRecordModel> selectAllRewardReceiveRecord() {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<RewardReceiveRecordModel> list = null;
        try {
            list = sqlSession.getMapper(RewardReceiveRecordModelMapper.class).selectAll();
            sqlSession.commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
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
    public static int insertRewardReceiveRecord(RewardReceiveRecordModel record) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(RewardReceiveRecordModelMapper.class).insert(record);
            sqlSession.commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
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
    public static int updateRewardReceiveRecord(RewardReceiveRecordModel record) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(RewardReceiveRecordModelMapper.class).updateByPrimaryKey(record);
            sqlSession.commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return count;
    }

    /**
     * 根据设备码去取一个用户
     * @param android_id
     * @return
     */
    public static User selectUserByOpenId(String android_id) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<User> list = null;
        try {
            list = sqlSession.getMapper(UserMapper.class).selectUserByOpenId(android_id);
            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            logger.error("selectAll", e);
        } finally {
            sqlSession.close();
        }

        if (list == null || list.size() == 0){
            return null;
        }else {
            return list.get(0);
        }
    }

    /**
     * 玩家是否首充
     * @param userId
     * @return
     */
    public static boolean isFirstPay(String userId){
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        try {
            List<Payment> list = sqlSession.getMapper(PaymentMapper.class).selectAllByUser(userId);
            if (list == null || list.size() == 0){
                return false;
            }
            if (list.size() == 1){
                return true;
            }
        }catch (Exception e){
            logger.error(e.getMessage());
            return false;
        }finally {
            sqlSession.close();
        }
        return false;
    }

}
