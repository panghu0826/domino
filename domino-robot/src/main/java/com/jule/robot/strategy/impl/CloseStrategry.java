package com.jule.robot.strategy.impl;

import com.jule.robot.config.Config;
import com.jule.robot.service.holder.RobotClientHolder;
import com.jule.robot.service.holder.RobotMoneyPoolHolder;
import com.jule.robot.service.websocket.BaseWebSocketClient;
import com.jule.robot.service.websocket.RobotGameWebSocketClient;
import com.jule.robot.strategy.BaseRobotStrategry;
import lombok.extern.slf4j.Slf4j;

/**
 * 机器人退出，资金退还
 */
@Slf4j
public class CloseStrategry extends BaseRobotStrategry {

    public CloseStrategry() {
    }

    @Override
    public void doAction(BaseWebSocketClient client, int functionId, byte[] bytes) {
        try {
            if (Config.TEST_TYPE_IS_STRESS == 1){
                RobotGameWebSocketClient robotClient = (RobotGameWebSocketClient)client;
                robotClient.ExecuteJoinTableForStressTest();
                return;
            }
            log.debug("机器人回收启动，userId = {}", client.getUserId());
            //退还资金
            RobotMoneyPoolHolder.robotReturnMoneyToPool(client.getUserId(), 0);

            //通知机器人服务，关闭此机器人的连接
            RobotClientHolder.closeClient(client.getUserId(), client, "主动离桌成功");
        }catch (Exception ex){
            log.error("机器人退出 error, msg = {},exception={}", ex.getMessage(), ex);
        }
    }
}
