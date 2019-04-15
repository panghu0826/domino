package com.jule.robot.strategy;

import com.jule.robot.service.websocket.BaseWebSocketClient;

public abstract class BaseRobotStrategry implements IRobotStrategy {
    @Override
    public void doAction(BaseWebSocketClient client, int functionId, byte[] bytes) {

    }
}
