package com.jule.domino.game.service;

import com.jule.core.jedis.StoredObjManager;
import com.jule.core.utils.ThreadPoolManager;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.utils.runnable.UserRunnable;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoneyService {
    private final static Logger logger = LoggerFactory.getLogger(MoneyService.class);

    private static class SingletonHolder {
        protected static final MoneyService instance = new MoneyService();
    }

    public static final MoneyService getInstance() {
        return MoneyService.SingletonHolder.instance;
    }


    /**
     * 兑换买入桌内积分
     */
    public double buyScore(String userId, double needScore) {
        User user = DBUtil.selectByPrimaryKey(userId);
        if (user == null) {
            logger.debug("玩家信息不存在, userId->" + userId);
            return -1;
        }
        if (user.getMoney() < needScore) {
            logger.debug("玩家积分不足, currScore->" + user.getMoney() + ", needScore->" + needScore);
            return -1;
        }
        user.setMoney(user.getMoney() - needScore);
        DBUtil.updateByPrimaryKey(user);
        /**保存玩家信息到缓存*/
        logger.info("13save userInfo->" + user.toString());
        StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
        return user.getMoney();
    }

    /**
     * 加回金额
     *
     * @param userId
     * @param score
     */
    public double updateMoney(String userId, double score) {
        //User user = DBUtil.selectByPrimaryKey(userId);//modify 2018-08-31 因为缓存里有数据
        User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId, User.class);
        if (user == null) {
            user = DBUtil.selectByPrimaryKey(userId);
        }

        if (user != null) {
            if (score < 0) {
                double userScore = user.getMoney();
                logger.info("UserMoney operation. updateMoney(), paramScore < 0,  userId->{}, paramScore->{}, userDBMoney->{}",
                        userId, score, userScore);
                return userScore;
            }

            user.setMoney(score);//user.getMoney() + score);
            ThreadPoolManager.getInstance().executeDbTask(new UserRunnable(user));
            //int result = DBUtil.updateByPrimaryKey(user);
            /**保存玩家信息到缓存*/
            logger.info("14save userInfo->" + user.toString());
            StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
            logger.info("UserMoney operation. updateMoney(), userId->{}, paramScore->{}", userId, score);
            return user.getMoney();
        }
        return 0;
    }


}
