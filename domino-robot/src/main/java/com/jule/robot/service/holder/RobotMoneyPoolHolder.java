package com.jule.robot.service.holder;

import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.log.service.LogReasons;
import com.jule.robot.config.Config;
import com.jule.robot.model.eenum.PlayTypeEnum;
import com.jule.db.entities.RobotCapitalPoolConfigModel;
import com.jule.db.entities.User;
import com.jule.db.proxy.EntityProxy;
import com.jule.robot.dao.DBUtil;
import com.jule.robot.service.LogService;
import com.jule.robot.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 机器人资金池管理
 */
public class RobotMoneyPoolHolder {
    private final static Logger logger = LoggerFactory.getLogger(RobotMoneyPoolHolder.class);

    /**
     * 机器人从资金池中获得买入金额
     * @param gameId
     * @param userId
     * @param user
     * @param buyInScore
     * @return
     */
    public static boolean robotBuyinFromPool(String gameId, String userId, com.jule.db.entities.User user, double buyInScore){
        boolean result = false;
        try{
            //机器人从资金池获取货币
            boolean isSuccGetMoney = getBuyInMoney(gameId+"", userId, user.getNick_name(), buyInScore);
            if(isSuccGetMoney){
                //在DB中给机器人增加货币库存
                boolean isSuccSetMoney = DBUtil.setRobotMoney(userId, user, buyInScore, gameId+"");
                if(isSuccSetMoney){
                    //增加机器人 代入/代出 记录
                    int insertCnt = DBUtil.insertRobotCapitalPoolRecord(gameId, buyInScore, user.getId(), false);
                    if(insertCnt == 1){
                        result = true;
                    }else{
                        logger.error("robotBuyinFromPool error.insertRecoredCnt error, gameId->{}, userId->{}, insertCnt->{}",
                                gameId, userId, insertCnt);
                    }
                }else{
                    logger.error("robotBuyinFromPool error.isSuccSetDBMoney error, gameId->{}, userId->{}, isSuccSetMoney->{}",
                            gameId, userId, isSuccSetMoney);
                }
            }
        }catch (Exception ex){
            logger.error("robotBuyinFromPool error. gameId->{}, userId->{}, exception msg->{}",
                    gameId, userId, ex.getMessage(), ex);
        }
        return result;
    }

    /**
     * 机器人退还货币到资金池
     * @param userId
     * @return
     */
    public static boolean robotReturnMoneyToPool(String userId, double paramScoreBalance){
        boolean result = false;
        String gameId = "";
        try{
            //获得机器人身上余额
            User user = DBUtil.selectUserByUserId(userId);
            gameId = DBUtil.selectGameIdByLastJoin(userId);

            double dbScoreBalance = user.getMoney();
            if(dbScoreBalance != paramScoreBalance && paramScoreBalance != 0){
                logger.error("robotReturnMoneyToPool WARN!!!!! paramScore <> dbScore, gameId->{}, userId->{}, paramScore->{}, dbScore->{}",
                        gameId, userId, paramScoreBalance, dbScoreBalance);
            }

            if (dbScoreBalance <= 0){
                return true;
            }

            //TODO:增加事务操作
            RobotCapitalPoolConfigModel model = DBUtil.selectByCapitalPool(gameId);
            //向资金池中增加机器人退还的货币
            int updatePoolCnt = DBUtil.updateRobotCapitalPool(gameId, dbScoreBalance, true);
            if(updatePoolCnt >= 1){
                //修改机器人DB库存
                user.setMoney(0d);
                int updateScoreCnt = EntityProxy.OBJ.update(user, com.jule.db.entities.User.class);
                if(updateScoreCnt == 1){
                    //增加机器人 代入/代出 记录
                    int insertCnt = DBUtil.insertRobotCapitalPoolRecord(gameId, dbScoreBalance, user.getId(), true);
                    if(insertCnt == 1){
                        //发送日志
                        logger.debug("发送日志--------------------------"+(model == null));
                        if (model != null){
                            LogService.OBJ.sendRobotLedgerLog(user,model.getPlayType(),model.getTotalMoney(),model.getTotalMoney() +  dbScoreBalance, dbScoreBalance, LogReasons.CommonLogReason.LEAVE_LEAVE);
                        }
                        result = true;
                    }else{
                        logger.error("robotBuyinFromPool error.insertRecoredCnt error, gameId->{}, userId->{}, insertCnt->{}",
                                gameId, userId, insertCnt);
                    }
                    logger.warn("成功：机器人 退还 资金，userId->{}, nickName->{}, amount->{}, gameId->{}", user.getId(), user.getNick_name(), dbScoreBalance, gameId);
                }else{
                    logger.error("扣除机器人DB库存资金失败，userId->{}, nickName->{}, amount->{}, gameId->{}, updateScoreCnt->{}",
                            user.getId(), user.getNick_name(), dbScoreBalance, gameId, updateScoreCnt);
                }

                result = true;
            }else{
                logger.error("失败：机器人 扣除退还 资金，操作库返回行数无效。userId->{}, nickName->{}, amount->{}, gameId->{}, result->{}",
                        user.getId(), user.getNick_name(), dbScoreBalance, gameId, result);
            }
        }catch (Exception ex){
            logger.error("robotBuyinFromPool error. gameId->{}, userId->{}, exception msg->{}",
                    gameId, userId, ex.getMessage(), ex);
        }
        return result;
    }

    public static void resetRobotMoneyPool(String playType) {
        RobotCapitalPoolConfigModel model = DBUtil.selectByCapitalPool(playType);
        if (null != model) {
            double addMoney = Double.parseDouble(StoredObjManager.get(RedisConst.ROBOT_POOL_MONEY.getProfix()));
            if (model.getTotalMoney() < addMoney) {
                double addNum = addMoney - model.getTotalMoney();
                addMoneyToRobotCapitalPoolBySystem(playType, addNum);
            } else {
                double subNum = model.getTotalMoney() - addMoney;
                subMoneyToRobotCapitalPoolBySystem(playType, subNum);
            }
        }
    }
    /**
     * 由系统向机器人资金池中增加货币
     * @param gameId
     * @param scoreAmount
     * @return
     */
    public static boolean addMoneyToRobotCapitalPoolBySystem(String gameId, double scoreAmount){
        String userId = "ROBOT_SYSTEM";
        boolean result = false;

        try{
            //TODO:增加事务操作
            RobotCapitalPoolConfigModel model = DBUtil.selectByCapitalPool(gameId);
            if(null == model){
                logger.error("失败：机器人资金池 增资，找不到资金池配置。playType->{}", gameId);
                return false;
            }

            //向资金池中增加货币
            int updatePoolCnt = DBUtil.updateRobotCapitalPool(gameId, scoreAmount, true);
            if(updatePoolCnt >= 1){
                //增加机器人 代入/代出 记录
                int insertCnt = DBUtil.insertRobotCapitalPoolRecord(gameId, scoreAmount, userId, true);
                if(insertCnt == 1){
                    result = true;
                }else{
                    logger.error("robotBuyinFromPool error.insertRecoredCnt error, gameId->{}, userId->{}, insertCnt->{}",
                            gameId, userId, insertCnt);
                }

                logger.warn("成功：系统 填充资金池，userId->{}, nickName->{}, amount->{}, gameId->{}", userId, userId, scoreAmount, gameId);
                model = DBUtil.selectByCapitalPool(gameId);
                MailService.sendMail("机器人资金池发生增资行为-TeenPattiHappy",
                        "GameId: "+ gameId + System.getProperty("line.separator")
                                +"GameName: "+ PlayTypeEnum.parseGame(Integer.parseInt(gameId)).name() + System.getProperty("line.separator")
                                +"AddAmount: "+ scoreAmount + System.getProperty("line.separator")
                                +"newAmount: "+ model.getTotalMoney());
                result = true;
            }else{
                logger.error("失败：系统 填充资金池，操作表 robot_capital_pool_config 返回行数无效。userId->{}, nickName->{}, amount->{}, gameId->{}, result->{}",
                        userId, userId, scoreAmount, gameId, result);
            }
        }catch (Exception ex){
            logger.error("异常：系统 填充资金池，userId->{}, nickName->{}, amount->{}, gameId->{}", userId, userId, scoreAmount, gameId);
        }

        return result;
    }

    /**
     * 从资金池获得货币
     *
     * @param playType
     * @param amount
     * @return
     */
    private static boolean subMoneyToRobotCapitalPoolBySystem(String playType, double amount) {
        RobotCapitalPoolConfigModel model = DBUtil.selectByCapitalPool(playType);
        if (null != model) {

            int result = DBUtil.updateRobotCapitalPool(playType, amount, false);
            if (result == 1) {
                //发送日志
                LogService.OBJ.sendRobotLedgerLog(null, model.getPlayType(), model.getTotalMoney(), (long) (model.getTotalMoney() - amount), (long) amount, LogReasons.CommonLogReason.SYSTEM_MIUNES);
                logger.warn("成功：机器人 申请 资金，userId->{}, nickName->{}, amount->{}, playType->{}", "system","system", amount, playType);
                return true;
            } else {
                logger.error("失败：机器人 申请 资金，操作库返回行数无效。userId->{}, nickName->{}, amount->{}, playType->{}, result->{}", "system", "system", amount, playType, result);
            }
        } else {
            logger.error("失败：机器人 申请 资金，找不到资金池配置。userId->{}, nickName->{}, amount->{}, playType->{}, modelIsNull->{}, modelBalanceMoney->{}",
                    "system", "system", amount, playType, null == model, null == model ? 0 : model.getTotalMoney());
        }
        return false;
    }

    /**
     * 从资金池获得货币
     * @param playType
     * @param userId
     * @param nickName
     * @param amount
     * @return
     */
    private static boolean getBuyInMoney(String playType, String userId, String nickName, double amount){
        RobotCapitalPoolConfigModel model = DBUtil.selectByCapitalPool(playType);
        if(null != model){
            if(model.getTotalMoney() < amount){
                logger.error("失败：机器人 申请 资金，资金池货币库存不足。userId->{}, nickName->{}, amount->{}, playType->{}, poolMoney->{}", userId, nickName, amount, playType, model.getTotalMoney());
                double addMoney = Double.parseDouble(StoredObjManager.get(RedisConst.ROBOT_POOL_MONEY.getProfix()));
                addMoneyToRobotCapitalPoolBySystem(playType, addMoney);
                return false;
            }

            int result = DBUtil.updateRobotCapitalPool(playType, amount, false);
            if(result == 1){
                //发送日志
                User user = DBUtil.selectUserByUserId(userId);
                //logger.debug("发送日志-----------222---------------"+(user == null));
                if (user != null){
                    LogService.OBJ.sendRobotLedgerLog(user,model.getPlayType(),model.getTotalMoney(),model.getTotalMoney() - amount, amount, LogReasons.CommonLogReason.JOIN_TABLE);
                }
                logger.warn("成功：机器人 申请 资金，userId->{}, nickName->{}, amount->{}, playType->{}", userId, nickName, amount, playType);
                return true;
            }else{
                logger.error("失败：机器人 申请 资金，操作库返回行数无效。userId->{}, nickName->{}, amount->{}, playType->{}, result->{}", userId, nickName, amount, playType, result);
            }
        }else{
            logger.error("失败：机器人 申请 资金，找不到资金池配置。userId->{}, nickName->{}, amount->{}, playType->{}, modelIsNull->{}, modelBalanceMoney->{}",
                    userId, nickName, amount, playType, null == model, null==model?0:model.getTotalMoney());
        }
        return false;
    }

//    /**
//     * 退还货币到资金池
//     * @param playType
//     * @param userId
//     * @param amount
//     * @return
//     */
//    private static boolean returnMoney(String playType, String userId, long amount){
//        User user = DBUtil.selectUserByUserId(userId);
//        return returnMoney(playType, user, amount);
//    }
//
//    private static boolean returnMoney(String playType, com.jule.db.entities.User user, long amount){
//        int result = DBUtil.updateRobotCapitalPool(playType, amount, true);
//        if(result == 1){
//            user.setMoney(0L);
//            EntityProxy.OBJ.update(user, com.jule.db.entities.User.class);
//            logger.info("成功：机器人 扣除退还 资金，userId->{}, nickName->{}, amount->{}, playType->{}", user.getId(), user.getNick_name(), amount, playType);
//            return true;
//        }else{
//            logger.info("失败：机器人 扣除退还 资金，操作库失败。userId->{}, nickName->{}, amount->{}, playType->{}", user.getId(), user.getNick_name(), amount, playType);
//        }
//        return false;
//    }
}
