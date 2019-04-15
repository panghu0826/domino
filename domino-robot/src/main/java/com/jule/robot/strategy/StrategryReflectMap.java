package com.jule.robot.strategy;

import com.jule.robot.service.holder.FunctionIdHolder;
import com.jule.robot.strategy.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

public class StrategryReflectMap {
    private final static Logger logger = LoggerFactory.getLogger(StrategryReflectMap.class);
    private static Map<Integer, Class<? extends BaseRobotStrategry>> reflectMap = new HashMap<>();

    static {
        reflectMap.put(FunctionIdHolder.Room_ACK_ApplyJoinTable, JoinTableStrategry.class);
        reflectMap.put(FunctionIdHolder.Game_ACK_ApplySitDown,SitDownStrategry.class);
        reflectMap.put(FunctionIdHolder.Game_ACK_ApplyLeave,LeaveStrategry.class);
        reflectMap.put(FunctionIdHolder.Game_ACK_ApplyBet,BetStrategry.class);
        reflectMap.put(FunctionIdHolder.Game_Notice_leaveReq,CloseStrategry.class);
    }

    public static BaseRobotStrategry getStrategry(int code) {
        try {
            if (reflectMap.containsKey(code)) {
                return (BaseRobotStrategry) ((Class<? extends BaseRobotStrategry>) reflectMap.get(code)).newInstance();
            }
        } catch (Exception e) {
            logger.error("BaseRobotStrategry.getStrategry ERROR, msg->{}", e.getMessage(), e);
        }
        return null;
    }
}
