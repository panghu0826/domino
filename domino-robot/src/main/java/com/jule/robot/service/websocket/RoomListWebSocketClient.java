package com.jule.robot.service.websocket;

import com.jule.robot.service.holder.FunctionIdHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户维护房间列表的WebSocketClient
 */
public class RoomListWebSocketClient extends BaseWebSocketClient {
    private final static Logger logger = LoggerFactory.getLogger(RoomListWebSocketClient.class);
    private int gameId;
    private String userId;

    public RoomListWebSocketClient(String webSocketURI){
        super(webSocketURI);
    }

    @Override
    protected void routeFunctionId(int functionId, byte[] bytes) {
        switch (functionId){
//            case FunctionIdHolder.Room_ACK_ApplyJoinTable:
//                AckJoinTable(functionId, bytes);
//                return;
//            case FunctionIdHolder.GATE_ACK_loginUser:
//                AckLoginUser(functionId, bytes);
//                return;
            default:
                logger.debug("UNKNOW functionID ->"+functionId+", functionName ->"+FunctionIdHolder.GetFunctionName(functionId));
        }
    }

//    public void ExecuteLoginUser(int _gameId, String _userId){
//        gameId = _gameId;
//        userId = _userId;
//        JoloAuth.JoloCommon_LoginReq.Builder req = JoloAuth.JoloCommon_LoginReq.newBuilder();
//        req.setUserId(userId);
//        req.setToken(userId);
//        req.setClientVersion("guest");
//        req.setChannelId("guest");
//        req.setUserIp("10.0.0.81");
//        req.setPlatform(0);
//        req.setPlatformVersion("1.0.0");
//        req.setDeviceNum("abcdefg-gfedcba");
//
//        int reqNum = (int)(System.currentTimeMillis()/1000);
//        sendData(FunctionIdHolder.GATE_REQ_loginUser, gameId, reqNum, req.build().toByteArray());
//    }
//
//    public void ExecuteJoinTable(){
//        JoloRoom.JoloRoom_ApplyJoinTableReq.Builder req = JoloRoom.JoloRoom_ApplyJoinTableReq.newBuilder();
//        req.setGameId(gameId+"");
//        req.setUserId(userId);
//
//        int reqNum = (int)(System.currentTimeMillis()/1000);
//        sendData(FunctionIdHolder.Room_REQ_ApplyJoinTable, gameId, reqNum, req.build().toByteArray());
//    }
//
//    private void AckJoinTable(int functionId, byte[] bytes){
//        try {
//            JoloRoom.JoloRoom_ApplyJoinTableAck ack = JoloRoom.JoloRoom_ApplyJoinTableAck.parseFrom(bytes);
//            logger.debug("ACK Header, functionId->"+functionId+", functionName->"+FunctionIdHolder.GetFunctionName(functionId)+", ACK Body, result->" + ack.getResult() + ", resultMsg->" + ack.getResultMsg());
//
//            String roomId = ack.getRoomId();
//            String tableId = ack.getTableId();
//            String seatId = ack.getSeatId();
//            String JoinGameSvrId = ack.getJoinGameSvrId();
//
//            if(ack.getResult() == 1){
//                logger.debug("Ack roomId->"+ack.getRoomId()+", tableId->"+ack.getTableId()+", ");
//                RobotGameWebSocketClient gameWSC = new RobotGameWebSocketClient(FunctionIdHolder.GATE_SVR_URI,
//                        Integer.parseInt(ack.getGameId()),
//                        "66",
//                        "1",
//                        //ack.getRoomId(),
//                        //ack.getTableId(),
//                        ack.getUserId(),
//                        Integer.parseInt(ack.getJoinGameSvrId()));
//                gameWSC.ExecuteSitDown(Integer.parseInt(ack.getSeatId()), 5000);
//            }
//
//        }catch (Exception ex){
//            logger.error("AckGetRoomList error, msg = "+ex.getMessage(), ex);
//        }
//    }
//
//    private void AckLoginUser(int functionId, byte[] bytes){
//        try {
//            JoloAuth.JoloCommon_LoginAck ack = JoloAuth.JoloCommon_LoginAck.parseFrom(bytes);
//            logger.debug("ACK Header, functionId->"+functionId+", functionName->"+FunctionIdHolder.GetFunctionName(functionId)+", ACK Body, result->" + ack.getResult() + ", ResultMsg->" + ack.getResultMsg() + ", userId->"+ack.getUserId()
//                    +", nickName->"+ack.getNickName()+", money->"+ack.getMoney());
//
//            if(ack.getResult() == 1 && ack.getMoney() > 0) {
//                userId = ack.getUserId();
//                ExecuteJoinTable();
//            }else{
//                logger.error("login failed. userId->"+ack.getUserId()+", money->"+ack.getMoney()+", result->"+ack.getResult()+", resultMsg->"+ack.getResultMsg());
//            }
//        }catch (Exception ex){
//            logger.error("AckLoginUser error, msg = "+ex.getMessage(), ex);
//        }
//    }
}
