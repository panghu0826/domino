package com.jule.robot.model;

import com.jule.robot.service.websocket.BaseWebSocketClient;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientInfo {
    private long createTime;
    private BaseWebSocketClient client;
}