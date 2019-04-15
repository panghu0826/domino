package com.jule.robot.dao;

import com.jule.db.entities.*;
import com.jule.db.proxy.EntityProxy;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

@Slf4j
public class DBUtil {
    public static User selectUserByUserId(String userId){
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("userId",userId);
        com.jule.db.entities.User user = EntityProxy.OBJ.getSingleResult("obj.id = :userId", paramsMap, com.jule.db.entities.User.class);
        return user;
    }

    public static double selectUserDBScoreByUserId(String userId){
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("userId",userId);
        com.jule.db.entities.User user = EntityProxy.OBJ.getSingleResult("obj.id = :userId", paramsMap, com.jule.db.entities.User.class);
        if(null == user){
            return 0;
        }
        return user.getMoney();
    }

    /**
     *  查询资金池
     */
    public static RobotCapitalPoolConfigModel selectByCapitalPool(String playType) {
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("play_type",playType);
        RobotCapitalPoolConfigModel rcpcm = EntityProxy.OBJ.getSingleResult("obj.playType = :play_type",paramsMap,RobotCapitalPoolConfigModel.class);
        return rcpcm;
    }

    /**
     *  查询机器人最后一次加入的游戏ID
     */
    public static String selectGameIdByLastJoin(String robotUserId) {
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("robotId", robotUserId);
        RobotCapitalPoolRecordModel rcpcm = EntityProxy.OBJ.getSingleResult("obj.access = 1 and obj.robotId = :robotId ORDER BY obj.operationTime DESC",paramsMap,RobotCapitalPoolRecordModel.class);
        if(null != rcpcm){
            return rcpcm.getPlayType();
        }else{
            return "71001001";
        }
    }

    /**
     * 查询机器人最后一条资金池记录，是否是代入记录
     * @param robotUserId
     * @return
     */
    public static boolean selectLastPoolRecordIsJoin(String robotUserId) {
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("robotId", robotUserId);
        RobotCapitalPoolRecordModel rcpcm = EntityProxy.OBJ.getSingleResult("obj.robotId = :robotId ORDER BY obj.operationTime DESC",paramsMap,RobotCapitalPoolRecordModel.class);
        if(null != rcpcm){
            if(rcpcm.getAccess() == 1){
                return true;
            }
        }
        return false;
    }

    /**
     * 修改（增加/扣减）
     *
     * isAdd 为 true 则增加 否则 扣减
     * @param
     * @return
     */
    public static int updateRobotCapitalPool(String playType, double money, boolean isAdd) {
        double addmoney = 0d;
        if(isAdd){
            addmoney = money;
        }else{
            addmoney = money * -1;
        }

        String sql = "update `robot_capital_pool_config` t set t.total_money = t.total_money + "+addmoney+" where t.play_type = "+playType;
        log.debug("退钱sql = {}", sql);
        int count = EntityProxy.OBJ.executeNativeUpdate(sql, null);

        return count;
    }

    /**
     * 增加机器人 代入/代出 记录
     * @param playType
     * @param money
     * @param robotId
     * @param isAdd
     * @return
     * @throws Exception
     */
    public static int insertRobotCapitalPoolRecord(String playType, double money, String robotId, boolean isAdd) throws Exception {
        RobotCapitalPoolConfigModel poolModel = selectByCapitalPool(playType);
        double poolValue = 0;
        if(null != poolModel){
            poolValue = poolModel.getTotalMoney();
        }else{
            throw new Exception("can't found robot capital pool, playType->"+playType);
        }

        RobotCapitalPoolRecordModel model = new RobotCapitalPoolRecordModel();
        model.setId(1);
        model.setPlayType(playType);
        model.setOperationTime(new Date());
        model.setAccess(isAdd?2:1);
        model.setMoney(money);
        model.setRobotId(robotId);
        model.setTotalMoney(poolValue);
        int count = EntityProxy.OBJ.insert(model);
        return count;
    }

    /**
     * 机器人增加货币库存
     * @param userId
     * @param user
     * @param score
     * @param playType
     * @return
     */
    public static boolean setRobotMoney(String userId, com.jule.db.entities.User user, double score, String playType){
        try {
            if (user != null) {
                //在DB中给机器人增加货币库存
                log.info("Exec setRobotMoney(), userId->{}, playType->{}, setScore->{}, userOldScore->{}", userId, playType, score, user.getMoney());
                user.setMoney(user.getMoney() + score);
                int updateCnt = EntityProxy.OBJ.update(user, com.jule.db.entities.User.class);
                if (updateCnt <= 0) {
                    log.error("setRobotMoney error, updateCnt error, userId->{}, updateCnt->{}", userId, updateCnt);
                    return false;
                }else{
                    return true;
                }
            } else {
                log.error("setRobotMoney error, object user is null, userId->{}", userId);
                return false;
            }
        }catch(Exception ex){
            log.error("setRobotMoney error, exception msg->{}, userId->{}", ex.getMessage(), userId, ex);
        }
        return false;
    }
}
