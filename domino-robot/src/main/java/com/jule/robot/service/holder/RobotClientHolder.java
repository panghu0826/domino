package com.jule.robot.service.holder;

import com.jule.robot.dao.DBUtil;
import com.jule.robot.model.ClientInfo;
import com.jule.robot.service.websocket.BaseWebSocketClient;
import com.jule.robot.service.websocket.RobotGameWebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RobotClientHolder {
    private final static Logger logger = LoggerFactory.getLogger(RobotClientHolder.class);
    private static Map<String, ClientInfo> clientMap = new ConcurrentHashMap<>();
    public static Map<String, ClientInfo> getClientMap(){
        return clientMap;
    }

    //记录req发送时间，用于接收ACK时判断 发送和接收的 时间差
    public static ConcurrentMap<String, Object[]> reqSendTimeMap = new ConcurrentHashMap<>();

    public static void addClient(String userId, BaseWebSocketClient client){
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setCreateTime(System.currentTimeMillis());
        clientInfo.setClient(client);
        clientMap.put(userId, clientInfo);
        logger.warn("增加一个机器人连接。userId->{}, clientMapSize->{}, clientObject->{}", userId, getCurrClientCnt(), client.toString());
    }

    public static boolean isHasClient(String userId){
        return clientMap.containsKey(userId);
    }

    public static void closeClient(String userId, BaseWebSocketClient client, String sourceFrom){
        try {
            //如果不是RobotGameWebSocketClient对象
            if(null != client && !(client instanceof RobotGameWebSocketClient)){
                logger.info("不是RobotGameWebSocketClient对象，userId->{}, sourceFrom->{}, clientType->{}", userId, sourceFrom, client.getClass());
                return;
            }

            int oldRobotCnt = RobotUserHolder.getRobotCnt();
            if (clientMap.containsKey(userId)) {
                client = (RobotGameWebSocketClient) client;
                if (null != client) {
                    //((RobotGameWebSocketClient) client).getAuthWebsocketClient().close(); //关闭认证连接
                    client.close(); //关闭机器人连接
                    RobotUserHolder.addUser(userId);
                    client = null;
                }
                clientMap.remove(userId); //连接关闭后，从map中删除此账号的连接信息
                logger.warn("关闭连接，移除机器人，来源->{}。 userId->{}, clientMapSize->{}, oldRobotCnt->{}, newRobotCnt->{}, userDBScore->{}",
                        sourceFrom, userId, clientMap.size(), oldRobotCnt, RobotUserHolder.getRobotCnt(), DBUtil.selectUserDBScoreByUserId(userId));
            }
        }catch (Exception e){
            logger.error("closeClient() Exception, userId->{}, sourceFrom->{}, msg->{}", userId, sourceFrom, e.getMessage(), e);
        }
    }

//    public static void closeClient(String userId, BaseWebSocketClient client){
//        int oldRobotCnt = RobotUserHolder.getRobotCnt();
//        if(clientMap.containsKey(userId)){
//            client = (RobotGameWebSocketClient)client;
//            if(null != client) {
//                ((RobotGameWebSocketClient) client).getAuthWebsocketClient().close(); //关闭认证连接
//                client.close(); //关闭机器人连接
//                RobotUserHolder.addUser(userId);
//                logger.warn("关闭连接，移除机器人。 userId->" + userId + ", clientMapSize->" + clientMap.size()
//                        + ", oldRobotCnt->" + oldRobotCnt + ", newRobotCnt->" + RobotUserHolder.getRobotCnt() + ", userDBScore->" + DBUtil.selectUserDBScoreByUserId(userId));
//                client = null;
//            }
//            clientMap.remove(userId); //从map中清除client数据
//        }
//    }

    public static int getCurrClientCnt(){
        return clientMap.size();
    }
}
