package com.jule.robot.strategy;

import com.jule.robot.service.websocket.BaseWebSocketClient;

public interface IRobotStrategy {
    /**
     * 执行机器人动作
     */
    void doAction(BaseWebSocketClient client, int functionId, byte[] bytes);
}
