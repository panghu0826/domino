package com.jule.robot.service.thread;

import com.jule.db.entities.RobotCardLevelConfigModel;
import com.jule.db.entities.RobotCommonConfigModel;
import com.jule.db.entities.RobotConfigForPlaytypeModel;
import com.jule.db.proxy.EntityProxy;
import com.jule.robot.dao.DBUtil;
import com.jule.robot.valve.gate.RobotConfigHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class RefreshRoboConfigThread implements Runnable  {
    private final static Logger logger = LoggerFactory.getLogger(RefreshRoboConfigThread.class);

    @Override
    public void run() {
        //获得 公用 配置
        RobotCommonConfigModel robotCommonConfig = EntityProxy.OBJ.getSingleResult("1=1",null, RobotCommonConfigModel.class);
        if(null != robotCommonConfig){
            RobotConfigHandler.setRobotCommonConfig(robotCommonConfig);
        }

        //获得 玩法 配置
        List<RobotConfigForPlaytypeModel> configForPlaytypList =  EntityProxy.OBJ.getResultList("1=1",null, RobotConfigForPlaytypeModel.class);
        for(RobotConfigForPlaytypeModel model : configForPlaytypList){
            RobotConfigHandler.setRobotConfigForPlaytype(model);
        }

        if(logger.isDebugEnabled()){
            logger.debug("机器人配置已更新");
        }
    }
}
