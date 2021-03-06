package com.jule.domino.game.dao;

import com.jule.core.database.DatabaseFactory;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.enums.RedisConst;
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

//    /**
//     * 插入用户信息
//     *
//     * @param
//     */
//    public static int insert(User user) {
//        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
//        int count = 0;
//        try {
////            log.debug("玩家现在的昵称：{},         {}",user.toString(),user.getNick_name());
//            count = sqlSession.getMapper(UserMapper.class).insert(user);
//            sqlSession.commit();
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            sqlSession.rollback();
//        } finally {
//            sqlSession.close();
//        }
//
//        if (count>0){
//            LogService.OBJ.sendUserUpdateLog(user);
//        }
//
//        return count;
//    }

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
    public static User selectBySubChannelId(String subChannelId) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        User user = null;
        try {
            user = sqlSession.getMapper(UserMapper.class).selectBySubChannelId(subChannelId);
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
            StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
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

    /**
     * 插入桌子创建记录
     * @param tcrm
     * @return
     */
    public static int insertTableCreateRecord(TableCreationRecordsModel tcrm) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(TableCreationRecordsModelMapper.class).insert(tcrm);
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
     * 查询桌子信息
     * @param tableId
     * @return
     */
    public static TableCreationRecordsModel selectTableCreateRecord(String tableId) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        TableCreationRecordsModel tcrm = null;
        try {
            tcrm = sqlSession.getMapper(TableCreationRecordsModelMapper.class).selectByPrimaryKey(tableId);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return tcrm;
    }

    /**
     * 查询桌子信息
     * @param createUserId
     * @return
     */
    public static List<TableCreationRecordsModel> selectTableCreateByUserId(String createUserId) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<TableCreationRecordsModel> list = null;
        try {
            list = sqlSession.getMapper(TableCreationRecordsModelMapper.class).selectTableCreateByUserId(createUserId);
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
     * 查询桌子记录的最后一个id
     * @return
     */
    public static int selectLastId() {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int lastId = 0;
        try {
            lastId = sqlSession.getMapper(TableCreationRecordsModelMapper.class).selectLastId();
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return lastId;
    }

    /**
     * 插入牌局记录
     * @param
     * @return
     */
    public static int insertGameRecord(GameRecordModel gameRecord) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(GameRecordModelMapper.class).insert(gameRecord);
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
     * 根据userId查询玩家的牌局记录
     * @param tableId
     * @return
     */
    public static List<GameRecordModel> selectByUserId(String tableId) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<GameRecordModel> list = null;
        try {
            list = sqlSession.getMapper(GameRecordModelMapper.class).selectByUserId(tableId);
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
     * 根据tableId查询桌子的牌局记录
     * @param tableId
     * @return
     */
    public static List<GameRecordModel> selectByTableId(String tableId) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<GameRecordModel> list = null;
        try {
            list = sqlSession.getMapper(GameRecordModelMapper.class).selectByTableId(tableId);
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
     * 插入房卡发送记录
     * @param rcrm
     * @return
     */
    public static int insertRoomCardRecords(RoomCardRecordsModel rcrm) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(RoomCardRecordsModelMapper.class).insert(rcrm);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return count;
    }

    public static int updateRoomCardRecords(RoomCardRecordsModel rcrm) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(RoomCardRecordsModelMapper.class).updateByPrimaryKey(rcrm);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return count;
    }

    public static RoomCardRecordsModel selectRoomCardByMoneyToken(String moneyToken) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        RoomCardRecordsModel rcrm = null;
        try {
            rcrm = sqlSession.getMapper(RoomCardRecordsModelMapper.class).selectRoomCardByMoneyToken(moneyToken);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return rcrm;
    }

    public static List<RoomCardRecordsModel> selectRoomCardByUserId(String userId) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<RoomCardRecordsModel>  rcrm = null;
        try {
            rcrm = sqlSession.getMapper(RoomCardRecordsModelMapper.class).selectRoomCardByUserId(userId);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return rcrm;
    }

    /**
     * 插入好友记录
     * @param ftm
     * @return
     */
    public static int insertFriend(FriendTableModel ftm) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(FriendTableModelMapper.class).insert(ftm);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return count;
    }

    public static int updateFriend(FriendTableModel ftm) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(FriendTableModelMapper.class).updateByUserIdAndFriendId(ftm);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return count;
    }

    public static List<FriendTableModel> selectFriendByUserId(String userId) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<FriendTableModel>  rcrm = null;
        try {
            rcrm = sqlSession.getMapper(FriendTableModelMapper.class).selectFriendByUserId(userId);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return rcrm;
    }

    public static FriendTableModel selectByUserIdAndFriendId(String userId,String friendUserId) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        FriendTableModel  rcrm = null;
        try {
            rcrm = sqlSession.getMapper(FriendTableModelMapper.class).selectByUserIdAndFriendId(userId,friendUserId);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return rcrm;
    }

    public static int deleteFriend(String userId,String friendUserId) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(FriendTableModelMapper.class).deleteFriend(userId,friendUserId);
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
     * 插入房卡发送记录
     * @param rcrm
     * @return
     */
    public static int insertItemRecords(ItemRecordsModel rcrm) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(ItemRecordsModelMapper.class).insert(rcrm);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return count;
    }

    public static int updateItemRecords(ItemRecordsModel rcrm) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(ItemRecordsModelMapper.class).updateByPrimaryKey(rcrm);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return count;
    }

    public static ItemRecordsModel selectItemByItemToken(String itemToken) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        ItemRecordsModel rcrm = null;
        try {
            rcrm = sqlSession.getMapper(ItemRecordsModelMapper.class).selectItemByItemToken(itemToken);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return rcrm;
    }

    public static List<ItemRecordsModel> selectItemByUserId(String userId) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<ItemRecordsModel>  rcrm = null;
        try {
            rcrm = sqlSession.getMapper(ItemRecordsModelMapper.class).selectItemByUserId(userId);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return rcrm;
    }

    public static int accumulationNumberOfGames(String userId) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(UserMapper.class).accumulationNumberOfGames(userId);
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
     * 道具操作
     * @return
     */
    public static int insertItem(UserItemModel uim) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(UserItemModelMapper.class).insertItem(uim);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return count;
    }

    public static int updateByItemId(UserItemModel uim) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(UserItemModelMapper.class).updateByItemId(uim);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return count;
    }

    public static List<UserItemModel> selectByUserIdItem(String userId) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        List<UserItemModel>  rcrm = null;
        try {
            rcrm = sqlSession.getMapper(UserItemModelMapper.class).selectByUserIdItem(userId);
            sqlSession.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return rcrm;
    }

    public static int deleteByItemId(String itemId) {
        SqlSession sqlSession = DatabaseFactory.getInstance().getSqlSession();
        int count = 0;
        try {
            count = sqlSession.getMapper(UserItemModelMapper.class).deleteByItemId(itemId);
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
