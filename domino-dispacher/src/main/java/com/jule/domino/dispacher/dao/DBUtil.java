package com.jule.domino.dispacher.dao;

import com.jule.core.database.DatabaseFactory;
import com.jule.core.utils.TimeUtil;
import com.jule.domino.base.dao.bean.Product;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.dispacher.dao.bean.*;
import com.jule.domino.dispacher.dao.mapper.*;
import com.jule.domino.dispacher.service.LogService;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by xujian on 2017/5/18 0018.
 */
public class DBUtil {
    private final static Logger logger = LoggerFactory.getLogger(DBUtil.class);

    private static class SingletonHolder {
        protected static final DBUtil instance = new DBUtil();
    }

    public static final DBUtil getInstance() {
        return DBUtil.SingletonHolder.instance;
    }

    /**
     * 获取玩家数据
     *
     * @param uid
     * @return
     */
    public static void updateLastOffline(String uid) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        try {
            sqlSession.getMapper(UserMapper.class).updateLastOffline(uid, new Date());
            sqlSession.commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
    }

    /**
     * 获取所有user信息
     *
     * @return
     */
    public static List<User> selectAllUser() {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<User> list = null;
        try {
            list = sqlSession.getMapper(UserMapper.class).selectAll();
            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            logger.error("selectAll"+e.getMessage(), e);
        } finally {
            sqlSession.close();
        }

        if (list == null || list.size() == 0) {
            return null;
        } else {
            return list;
        }
    }

    /**
     * 通过设备信息查询
     *
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

        if (list == null || list.size() == 0) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * 插入用户信息
     *
     * @param
     */
    public int insert(User user) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(UserMapper.class).insert(user);
            sqlSession.commit();
        } catch (Exception e) {
            logger.error("insert", e.toString());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        if (count > 0) {
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
            logger.error("", e);
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
    public List<User> selectAll() {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<User> list = null;
        try {
            list = sqlSession.getMapper(UserMapper.class).selectAll();
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
            logger.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        if (count > 0) {
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
    public int deleteByPrimaryKey(String user_id) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(UserMapper.class).deleteByPrimaryKey(user_id);
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
     *
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
     *
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
     * 查询用户信息
     *
     * @param
     * @return
     */
    public static RewardReceiveRecordModel selectRewardReceiveRecord(String playerId) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        RewardReceiveRecordModel rcm = null;
        try {
            rcm = sqlSession.getMapper(RewardReceiveRecordModelMapper.class).selectByPrimaryKey(playerId);
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
     * 查询黑白名单
     *
     * @param userId
     * @return
     */
    public static BlackWhiteModel selectBlackWhite(String userId) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        BlackWhiteModel model = null;
        try {
            model = sqlSession.getMapper(BlackWhiteMapper.class).selectByPrimaryKey(userId);
            sqlSession.commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return model;
    }

    public static List<BlackWhiteModel> selectAllBlack() {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<BlackWhiteModel> list = new ArrayList<>();
        try {
            list = sqlSession.getMapper(BlackWhiteMapper.class).selectAllBlack();
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
     * 所有的广告配置
     *
     * @param
     * @return
     */
    public static List<AdvertisingConfigModel> selectAllAdvertisingConfigModel() {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<AdvertisingConfigModel> list = null;
        try {
            list = sqlSession.getMapper(AdvertisingConfigModelMapper.class).selectAll();
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
     * 查询在线参数
     *
     * @return String channel,String version,String packName
     */
    public static OnlineConfigModel selectOnlineConfigModel(List<String> array) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        OnlineConfigModel model = null;
        try {
            List<Integer> list2 = sqlSession.getMapper(OnlineArgsModelMapper.class).selectOnlineIds();
            int configId = 0;
            for (Integer idx : list2){
                List<String> list = sqlSession.getMapper(OnlineArgsModelMapper.class).selectByOnlineId(idx);
                if (list != null && list.containsAll(array)) {
                    configId = idx;
                    break;
                }
            }
            model = sqlSession.getMapper(OnlineConfigModelMapper.class).selectByPrimaryKey(configId);
            if (model != null) {
                return model;
            }
            sqlSession.commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return model;
    }

    /**
     * 初始化
     * @param uid
     * @return
     */
    private static AdInfoModel initAdInfo(String uid){
        //初始化信息
        AdInfoModel model = new AdInfoModel();
        model.setUid(uid);
        model.setLastTime(System.currentTimeMillis());

        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        try {
            int count = sqlSession.getMapper(AdInfoMapper.class).insert(model);
            sqlSession.commit();
            if (count == 1){
                return model;
            }
        }catch (Exception e){
            logger.error("广告数据库插入失败,excption = {}",e.getMessage());
            sqlSession.rollback();
        }finally {
            sqlSession.close();
        }
        return null;
    }

    /**
     * 查询玩家广告
     * @param uid
     * @return
     */
    private static AdInfoModel selectAdInfo(String uid){
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        try {
            AdInfoModel model = sqlSession.getMapper(AdInfoMapper.class).selectByPrimaryKey(uid);
            sqlSession.commit();
            return model;
        }catch (Exception e){
            logger.error("广告数据库查询失败,excption = {}",e.getMessage());
            sqlSession.rollback();
        }finally {
            sqlSession.close();
        }
        return null;
    }

    /**
     * 更新广告
     * @param model
     * @return
     */
    public static int updateAdInfo(AdInfoModel model){
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        try {
            int count = sqlSession.getMapper(AdInfoMapper.class).updateByPrimaryKey(model);
            sqlSession.commit();
            return count;
        }catch (Exception e){
            logger.error("广告数据库更新失败,excption = {}",e.getMessage());
            sqlSession.rollback();
        }finally {
            sqlSession.close();
        }
        return 0;
    }

    public static AdInfoModel getAdInfo(String uid){
        //查询玩家广告
        AdInfoModel model = selectAdInfo(uid);
        if (model == null){
            //如果不存在，初始化一条
            return initAdInfo(uid);
        }

        long now = System.currentTimeMillis();
        if (!TimeUtil.dateCompare(model.getLastTime()) &&
                model.getTimes() < now){
            //每日重置
            model.setTimes(0);
            model.setTotalmoney(0);
            model.setLastTime(now);
            updateAdInfo(model);
        }
        return model;
    }

    /**
     * 加载广告配置
     * @return
     */
    public static AdConfigModel loadAdConfig(){
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        try {
            AdConfigModel model = sqlSession.getMapper(AdConfigMapper.class).selectSingle();
            sqlSession.commit();
            return model;
        }catch (Exception e){
            logger.error("广告配置查询失败,excption = {}",e.getMessage());
            sqlSession.rollback();
        }finally {
            sqlSession.close();
        }
        return null;
    }

    public static List<VersionConfigModel> selectAllVersion(){
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        try {
            List<VersionConfigModel> models = sqlSession.getMapper(VersionConfigModelMapper.class).selectAll();
            sqlSession.commit();
            return models;
        }catch (Exception e){
            logger.error("版本信息查询失败,excption = {}",e.getMessage());
            sqlSession.rollback();
        }finally {
            sqlSession.close();
        }
        return null;
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
}
