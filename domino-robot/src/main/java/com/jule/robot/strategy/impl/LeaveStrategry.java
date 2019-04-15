package com.jule.robot.strategy.impl;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.robot.service.holder.FunctionIdHolder;
import com.jule.robot.service.holder.RobotClientHolder;
import com.jule.robot.service.holder.RobotMoneyPoolHolder;
import com.jule.robot.service.websocket.BaseWebSocketClient;
import com.jule.robot.strategy.BaseRobotStrategry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaveStrategry extends BaseRobotStrategry {
    private final static Logger logger = LoggerFactory.getLogger(LeaveStrategry.class);

    public LeaveStrategry() {
    }

    @Override
    public void doAction(BaseWebSocketClient client, int functionId, byte[] bytes) {
        try {
            JoloGame.JoloGame_ApplyLeaveAck ack = JoloGame.JoloGame_ApplyLeaveAck.parseFrom(bytes);
            logger.debug("离开桌子ACK， ACK Header, functionId->"+functionId+", functionName->"+ FunctionIdHolder.GetFunctionName(functionId)+", ACK Body, result->" + ack.getResult() + ", ResultMsg->" + ack.getResultMsg()
                    + ", userId->"+ack.getUserId()+", roomId->"+ack.getRoomId() +", tableId->"+ack.getTableId()+", playScoreStore->"+ack.getCurrStoreScore());
            if(ack.getResult() == 1 || ack.getResult() == -1){
                if(ack.getResult() == -1){
                    logger.warn("离开桌子失败|Game找不到用户信息，机器人默认为离桌成功， userId->{}, roomId->{}, tableId->{}, ackResult->{}, resultMsg->{}",
                            ack.getUserId(), ack.getRoomId(), ack.getTableId(), ack.getResult(), ack.getResultMsg());
                }
                //退还资金
                RobotMoneyPoolHolder.robotReturnMoneyToPool(client.getUserId(), ack.getCurrStoreScore());

                //通知机器人服务，关闭此机器人的连接
                RobotClientHolder.closeClient(client.getUserId(), client, "主动离桌成功");
            }else{
                logger.error("离开桌子失败， userId->{}, roomId->{}, tableId->{}, ackResult->{}, resultMsg->{}", ack.getUserId(), ack.getRoomId(), ack.getTableId(), ack.getResult(), ack.getResultMsg());
            }
        }catch (Exception ex){
            logger.error("AckSiteDown error, msg = "+ex.getMessage(), ex);
        }
    }
}
