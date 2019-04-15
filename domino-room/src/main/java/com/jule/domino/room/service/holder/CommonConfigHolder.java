package com.jule.domino.room.service.holder;

import com.jule.domino.room.dao.DBUtil;
import com.jule.domino.room.dao.bean.CommonConfigModel;
import com.jule.domino.room.model.eenum.PlayTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用游戏配置
 */
@Slf4j
public class CommonConfigHolder {
    private final static Logger logger = LoggerFactory.getLogger(CommonConfigHolder.class);

    private static Map<Integer, CommonConfigModel> COMMON_CONFIG = new HashMap<>(); //所有玩法对应的通用配置

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
    public void init() {
        List<CommonConfigModel> configList = DBUtil.getCommonConfigFromDb();
        if (configList == null) {
            throw new Error("game CommonConfig is null");
        }
        for (int i = 0; i < configList.size(); i++) {
            CommonConfigModel model = configList.get(i);
            COMMON_CONFIG.put(PlayTypeEnum.getGameByType(model.getPlayType()), model);
        }
        log.info("加载 room " + COMMON_CONFIG.size() + " 通用配置");
    }
}
