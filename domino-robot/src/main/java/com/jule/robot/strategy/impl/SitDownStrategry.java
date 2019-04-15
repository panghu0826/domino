package com.jule.robot.strategy.impl;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.robot.model.PlayerInfo;
import com.jule.robot.service.holder.FunctionIdHolder;
import com.jule.robot.service.holder.RobotClientHolder;
import com.jule.robot.service.holder.RobotMoneyPoolHolder;
import com.jule.robot.service.websocket.BaseWebSocketClient;
import com.jule.robot.strategy.BaseRobotStrategry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SitDownStrategry extends BaseRobotStrategry {
    private final static Logger logger = LoggerFactory.getLogger(SitDownStrategry.class);
    private boolean isSiteDown = false;
    public SitDownStrategry() {
    }

    @Override
    public void doAction(BaseWebSocketClient client, int functionId, byte[] bytes) {
//        setClient(client);
        try {
            JoloGame.JoloGame_ApplySitDownAck ack = JoloGame.JoloGame_ApplySitDownAck.parseFrom(bytes);
            logger.info("SiteDown ACK Header, functionId->"+functionId+", functionName->"+ FunctionIdHolder.GetFunctionName(functionId)+", ACK Body, result->" + ack.getResult() + ", ResultMsg->" + ack.getResultMsg()
                    + ", userId->"+ack.getUserId()+", roomId->"+ack.getRoomId() +", tableId->"+ack.getTableId()+", seatNum->"+ack.getSeatNum()+", currPlayScore->"+ack.getCurrPlayScore());
            if(ack.getResult() == 1 || ack.getResult() == -6){
                isSiteDown = true;
                for(JoloGame.JoloGame_TablePlay_PlayerInfo playerInfo : ack.getPlayerInfoListList()){
                    PlayerInfo pi = new PlayerInfo();
                    pi.setUserId(playerInfo.getUserId());
                    pi.setIsBlind(playerInfo.getIsBlind());
                    pi.setState(playerInfo.getState());
                    pi.setSeatNum(playerInfo.getSeatNum());
                    pi.setPlayScoreStore(playerInfo.getPlayScoreStore());
                    client.getOnTablePlayers().put(playerInfo.getUserId(), pi);
                    client.getInGamePlayersBySeatNum().put(playerInfo.getSeatNum(),pi);

                    //如果是坐下的人中有此机器人，那么设置机器人client对象的座位号属性
                    if(pi.getUserId().equals(client.getUserId())){
                        client.setSeatNum(pi.getSeatNum());
                    }
                }
                logger.info("桌内玩家Map->"+client.getOnTablePlayers());

                ExecuteGetGiftsList(client);
            }else{
                boolean isSucc = RobotMoneyPoolHolder.robotReturnMoneyToPool(ack.getUserId(), ack.getCurrPlayScore());
                logger.error("机器人坐下失败，退还机器人的货币余额到资金池中。gameId->{}, userId->{}, money->{}, 退还结果->{}",
                        client.getGameId()+"", ack.getUserId(), ack.getCurrPlayScore(), isSucc);

                //通知机器人服务，关闭此机器人的连接
                RobotClientHolder.closeClient(client.getUserId(), client, "主动离桌成功");
            }
        }catch (Exception ex){
            logger.error("AckSiteDown error, msg = "+ex.getMessage(), ex);
        }
    }

    //获得礼物列表
    private void ExecuteGetGiftsList(BaseWebSocketClient client) {
        JoloGame.JoloGame_GiftsListReq.Builder req = JoloGame.JoloGame_GiftsListReq.newBuilder();
        req.setUserId(client.getUserId());
        req.setRoomId(client.getRoomId());
        req.setTableId(client.getTableId());

        int reqNum = (int) (System.currentTimeMillis() / 1000);
        int functionId = FunctionIdHolder.Game_REQ_GiftsList;

        client.sendData(functionId, client.getGameId(), reqNum, req.build().toByteArray(), client.getGameSvrId());
    }
}
