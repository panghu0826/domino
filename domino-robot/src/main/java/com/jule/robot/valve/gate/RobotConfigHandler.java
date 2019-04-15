package com.jule.robot.valve.gate;

import com.jule.db.entities.*;
import com.jule.db.proxy.EntityProxy;
import com.jule.robot.model.HandCardTypeModel;
import com.jule.robot.model.eenum.PlayTypeEnum;
import com.jule.robot.util.StringTools;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RobotConfigHandler {
    private final static Logger logger = LoggerFactory.getLogger(RobotConfigHandler.class);
    private static RobotCommonConfigModel robotCommonConfig = null;
    private static ConcurrentMap<String, RobotConfigForPlaytypeModel> robotConfigForPlaytype = new ConcurrentHashMap<>();

    public static RobotCommonConfigModel getRobotCommonConfig(){
        if(null == robotCommonConfig){
            robotCommonConfig = EntityProxy.OBJ.getSingleResult("1=1",null, RobotCommonConfigModel.class);
        }

        if(null == robotCommonConfig){
            logger.error("找不到 公用 配置：RobotCommonConfigModel");
        }
        return robotCommonConfig;
    }

    public static RobotConfigForPlaytypeModel getRobotConfigForPlaytype(String playType, String cardLevel){
        RobotConfigForPlaytypeModel model = null;
        String cacheKey = playType+"|"+cardLevel;
        if(robotConfigForPlaytype.containsKey(cacheKey)){
            return robotConfigForPlaytype.get(cacheKey);
        }else{
            Map<String,Object> paramsMap = new HashMap<>();
            paramsMap.put("play_type", playType);
            paramsMap.put("card_level", cardLevel);

            model = EntityProxy.OBJ.getSingleResult("obj.playType = :play_type and obj.cardLevel = :card_level", paramsMap, RobotConfigForPlaytypeModel.class);
            if(null != model){
                robotConfigForPlaytype.put(cacheKey, model);
            }
        }

        if(null == model){
            logger.error("找不到 玩法 对应配置：RobotConfigForPlaytypeModel， playType->{}, cardLevel->{}", playType, cardLevel);
            model = getRobotConfigForPlaytype("71001001", cardLevel);
        }
        return model;
    }

    public static void setRobotCommonConfig(RobotCommonConfigModel robotCommonConfig) {
        RobotConfigHandler.robotCommonConfig = robotCommonConfig;
    }

    public static void setRobotConfigForPlaytype(RobotConfigForPlaytypeModel configForPlaytype){
        String cacheKey = configForPlaytype.getPlayType()+"|"+configForPlaytype.getCardLevel();
        robotConfigForPlaytype.put(cacheKey, configForPlaytype);
    }
}
