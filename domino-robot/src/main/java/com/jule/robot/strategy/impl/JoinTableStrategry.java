package com.jule.robot.strategy.impl;

import JoloProtobuf.GameSvr.JoloGame;
import JoloProtobuf.RoomSvr.JoloRoom;
import com.jule.db.proxy.EntityProxy;
import com.jule.robot.Robot;
import com.jule.robot.config.Config;
import com.jule.robot.service.holder.FunctionIdHolder;
import com.jule.robot.service.holder.RobotClientHolder;
import com.jule.robot.service.websocket.BaseWebSocketClient;
import com.jule.robot.strategy.BaseRobotStrategry;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class JoinTableStrategry extends BaseRobotStrategry {
    private final static Logger logger = LoggerFactory.getLogger(JoinTableStrategry.class);

    public JoinTableStrategry() {
    }

    @Override
    public void doAction(BaseWebSocketClient client, int functionId, byte[] bytes) {
//        setClient(client);
        try {
            JoloRoom.JoloRoom_ApplyJoinTableAck ack = JoloRoom.JoloRoom_ApplyJoinTableAck.parseFrom(bytes);
            logger.debug("ACK Header, functionId->" + functionId + ", functionName->" + FunctionIdHolder.GetFunctionName(functionId) + ", ACK Body, result->" + ack.getResult() + ", resultMsg->" + ack.getResultMsg());

            if (ack.getResult() == 1) {
                client.setGameSvrId(Integer.parseInt(ack.getJoinGameSvrId()));

                //存储牌桌基础信息
//                logger.debug("设置bootAmount->"+ack.getBootAmount());
                client.setBootAmount(ack.getBootAmount());

                if(Config.TEST_TYPE_IS_STRESS == 1){
                    client.setRoomId(ack.getRoomId());
                    client.setTableId(ack.getTableId());
                }

                logger.info("Ack roomId->" + ack.getRoomId() + ", tableId->" + ack.getTableId() + ", seatId->" + ack.getSeatId());
                com.jule.db.entities.User user = EntityProxy.OBJ.get(client.getUserId(), com.jule.db.entities.User.class);
                ExecuteSitDown(client, user.getMoney());
            }
        } catch (Exception ex) {
            logger.error("AckJoinTable error, msg = " + ex.getMessage(), ex);
        }
    }

    public void ExecuteSitDown(BaseWebSocketClient client, double buyInScore) {
        JoloGame.JoloGame_ApplySitDownReq.Builder req = JoloGame.JoloGame_ApplySitDownReq.newBuilder();
        req.setUserId(client.getUserId());
        req.setRoomId(client.getRoomId());
        req.setTableId(client.getTableId());
        req.setSeatNum(0);
        req.setBuyInScore(buyInScore);

        int reqNum = (int) (System.currentTimeMillis() / 1000);
        int functionId = FunctionIdHolder.Game_REQ_ApplySitDown;

        logger.debug("GameReq_SiteDown Body, reqNum->" + reqNum + ", functionName->" + FunctionIdHolder.GetFunctionName(functionId) + ", userId->" + req.getUserId() + ", roomId->" + req.getRoomId() + ", tableId->" + req.getTableId() + ", seatNum->" + req.getSeatNum()
                + ", buyinScore->" + req.getBuyInScore());

        client.sendData(functionId, client.getGameId(), reqNum, req.build().toByteArray(), client.getGameSvrId());
    }
}
