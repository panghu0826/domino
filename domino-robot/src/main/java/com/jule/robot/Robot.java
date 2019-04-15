package com.jule.robot;

import com.jule.robot.service.websocket.RobotGameWebSocketClient;

public class Robot {

    private RobotGameWebSocketClient gameClient = null;

    public Robot(int _gameId, String _userId, String _roomId, String _tableId) {
        gameClient = new RobotGameWebSocketClient(_gameId, _userId, _roomId, _tableId);
    }
}
