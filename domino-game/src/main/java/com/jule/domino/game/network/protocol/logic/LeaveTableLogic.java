package com.jule.domino.game.network.protocol.logic;


import JoloProtobuf.GameSvr.JoloGame;
import com.alibaba.fastjson.JSONObject;
import com.jule.core.common.log.LoggerUtils;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.room.service.RoomOprService;
import com.jule.domino.game.service.UserTableService;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisChannel;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.enums.RoleType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LeaveTableLogic {
    private static class SingletonHolder {
        protected static final LeaveTableLogic instance = new LeaveTableLogic();
    }

    public static final LeaveTableLogic getInstance() {
        return LeaveTableLogic.SingletonHolder.instance;
    }

    private LeaveTableLogic() {
    }

    /**
     * @param player
     * @param table
     * @param ack
     */
    public void logic(PlayerInfo player, AbstractTable table, JoloGame.JoloGame_ApplyLeaveAck.Builder ack) {
        //如果玩家在座位，那么先做站起处理,因为是强制站起处理
        boolean isSuccStandUp = false;
        double currentMoney = 0L;
        if (player.getState().getValue() > PlayerStateEnum.spectator.getValue()) {
            isSuccStandUp = table.standUp(player.getSeatNum(), player.getPlayerId(), "LeaveTableLogic-logic");
            if (player.getRoleType() != null && player.getRoleType().equals(RoleType.ROBOT)) {
                LoggerUtils.robot.info("Robot standUp reason TIMEOUT_DISCARD_COUNT id:" + player.getPlayerId() + ",gameId:" + table.getPlayType()
                        + ",roomId:" + table.getRoomId() + ",tableId:" + table.getTableId());
            }
            log.debug("LeaveTable logic(), 玩家站起结果, seatNum->{}, isSuccStandUp->{}, roomId->{}, tableId->{}, gameId->{}, userId->{}",
                    player.getSeatNum(), isSuccStandUp, table.getRoomId(), table.getTableId(), table.getPlayType(), player.getPlayerId());
            if (isSuccStandUp) {
                //玩家游戏数据结算
                currentMoney = table.playerDataSettlement(player);
            } else {
                User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + player.getPlayerId(), User.class);
                currentMoney = user != null ? user.getMoney() : 0L;
            }
            if (ack != null) {
                ack.setCurrStoreScore(currentMoney);
            }
        }

        try {
            log.info("Game通知room模块执行离开桌子的操作" );
            RoomOprService.OBJ.leaveTableHandler(String.valueOf(table.getPlayType()),table.getRoomId(),table.getTableId(),player.getPlayerId());
        } catch (Exception ex) {
            log.error("Game通知room模块执行离开桌子的操作 ERROR：", ex);
        }

        UserTableService.getInstance().onPlayerOutTable(player);

        //赋值用户的总输赢
        if (ack != null) {
            ack.setWinLoseScore(player.getTotalWinLoseScore());
        }
        //清除player信息
        table.returnLobby(player.getPlayerId());

        //此对象，通知给客户端，在离开时显示玩家本桌内的打牌战绩
        JoloGame.JoloGame_TablePlay_OtherPlayerInfo.Builder otherPlayerInfo = JoloGame.JoloGame_TablePlay_OtherPlayerInfo.newBuilder();
        otherPlayerInfo.setUserId(player.getPlayerId());
        otherPlayerInfo.setChipsWon(0);
        otherPlayerInfo.setHandsTimes(player.getHandsWon().getLinkedDeque().size());
        otherPlayerInfo.setNickName(player.getNickName());
        otherPlayerInfo.setHandsWon(player.getHandsWon().won());
        otherPlayerInfo.setPlayScoreStore(player.getPlayScoreStore());
        otherPlayerInfo.setIcon(player.getIcon());
        otherPlayerInfo.setBiggest(player.getBiggestChipsWon());
        if (ack != null) {
            ack.setOtherPlayerInfo(otherPlayerInfo);
        }
        log.info("本次离开桌子玩家的战绩信息，player info:" + otherPlayerInfo.toString()
                + ", tableId->" + table.getTableId() + ", roomId->" + table.getRoomId() + ", gameId->" + table.getPlayType());
    }
}
