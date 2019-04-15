package com.jule.robot.service.thread;

import JoloProtobuf.RoomSvr.JoloRoom;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.nio.ByteBuffer;

public class RobotThread implements Runnable  {
    private final static Logger logger = LoggerFactory.getLogger(RobotThread.class);
    private WebSocketClient client = null;

    public void run() {
        try {
        }catch (Exception ex){
            logger.error("RobotThread run, msg = "+ex.getMessage(), ex);
        }
    }
}
