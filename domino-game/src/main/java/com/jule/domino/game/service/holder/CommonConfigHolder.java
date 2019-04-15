package com.jule.domino.game.service.holder;

import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.CommonConfigModel;
import com.jule.domino.game.model.eenum.PlayTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 通用游戏配置
 */
@Slf4j
public class CommonConfigHolder {

    /**所有玩法对应的通用配置*/
    private static Map<Integer, CommonConfigModel> COMMON_CONFIG = new HashMap<>();

    private static class SingletonHolder {
        protected static final CommonConfigHolder instance = new CommonConfigHolder();
    }

    public static final CommonConfigHolder getInstance() {
        return CommonConfigHolder.SingletonHolder.instance;
    }

    private CommonConfigHolder() {
        init();
    }

    /**
     * @param playType
     * @return
     */
    //获取桌子的所有信息
    public CommonConfigModel getCommonConfig(int playType) {
        return COMMON_CONFIG.get(playType);
    }

    /**
     * 获取所有配置
     *
     * @return
     */
    public Map<Integer, CommonConfigModel> getAllConfig() {
        return COMMON_CONFIG;
    }

    /**
     * 加载内存
     */
    public boolean init() {
        try {
            List<CommonConfigModel> configList = DBUtil.getCommonConfigFromDb();
            if (configList == null) {
                throw new Error("game CommonConfig is null");
            }
            for (int i = 0; i < configList.size(); i++) {
                CommonConfigModel model = configList.get(i);
                COMMON_CONFIG.put(PlayTypeEnum.getGameByType(model.getPlayType()), model);

                //放入内存
                StoredObjManager.set(RedisConst.ROBOT_POOL_MONEY.getProfix(), "" + model.getPoolMoney());
                StoredObjManager.set(RedisConst.ROBOT_JOIN_PCT.getProfix(),model.getRobotJoinPct());
            }
            log.info("加载 game " + COMMON_CONFIG.size() + " 通用配置");
            return true;
        }catch(Exception ex){
            log.error(ex.getMessage());
        }
        return false;
    }
}
