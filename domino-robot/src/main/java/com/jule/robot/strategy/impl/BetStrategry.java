package com.jule.robot.strategy.impl;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.robot.service.holder.FunctionIdHolder;
import com.jule.robot.service.websocket.BaseWebSocketClient;
import com.jule.robot.service.websocket.RobotGameWebSocketClient;
import com.jule.robot.strategy.BaseRobotStrategry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetStrategry extends BaseRobotStrategry {
    private final static Logger logger = LoggerFactory.getLogger(BetStrategry.class);

    public BetStrategry() {
    }

    @Override
    public void doAction(BaseWebSocketClient client, int functionId, byte[] bytes) {
//        setClient(client);
        try {
            JoloGame.JoloGame_ApplyBetAck ack = JoloGame.JoloGame_ApplyBetAck.parseFrom(bytes);
            logger.debug("ACK Header, functionId->"+functionId+", functionName->"+ FunctionIdHolder.GetFunctionName(functionId)+", ACK Body, result->" + ack.getResult() + ", ResultMsg->" + ack.getResultMsg()
                    + ", userId->"+ack.getUserId()+", GameOrderId->"+ack.getGameOrderId() +", BetScore->"+ack.getBetScore());

            RobotGameWebSocketClient robotClient = (RobotGameWebSocketClient)client;
            if(ack.getResult() != 1){
                int reqNum = (int) (System.currentTimeMillis() / 1000);
                if(ack.getResult() == -7){
                    int betScore = ack.getBetScore() / 2;
                    logger.info("机器人下注失败， 原因：下注额超过最大下注额，那么减半下注额再次下注。 result->{}, againBetScore->{}, userId->{}, gameOrderId->{}, roomId->{}, tableId->{}",
                            ack.getResult(), betScore, ack.getUserId(), ack.getGameOrderId(), ack.getRoomId(), ack.getTableId());
                    robotClient.ExecuteBet(ack.getGameOrderId(), betScore, betScore, reqNum, betScore);
                }else if(ack.getResult() == -6){
                    int betScore = ack.getBetScore() * 2;
                    logger.info("机器人下注失败， 原因：下注额小于最小下注额，那么乘2下注额再次下注。 result->{}, againBetScore->{}, userId->{}, gameOrderId->{}, roomId->{}, tableId->{}",
                            ack.getResult(), betScore, ack.getUserId(), ack.getGameOrderId(), ack.getRoomId(), ack.getTableId());
                    robotClient.ExecuteBet(ack.getGameOrderId(), betScore, betScore, reqNum, betScore);
                }else{
                    logger.error("机器人下注失败， result->{}, 马上执行弃牌操作。", ack.getResult());
                }
            }
        }catch (Exception ex){
            logger.error("AckApplyBet error, msg = "+ex.getMessage(), ex);
        }
    }
}
