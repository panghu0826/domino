package com.jule.domino.game.service;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.base.enums.RedisConst;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TableSubService {

    public static boolean leave(String userId, String gameId, String roomId, String tableId) throws Exception {
        boolean result = false;
        PlayerService.getInstance().onPlayerLoutOut("" + userId);
        AbstractTable table = TableService.getInstance().getTable(gameId, roomId, tableId);
        if (table == null) {
            return result;
        }
        PlayerInfo player = table.getPlayer(userId);
        if (player == null) {
            return result;
        }
        result = true;
        try {

            //如果玩家在座位，并且身上有货币，那么先做站起处理
            if (player.getState().getValue() > PlayerStateEnum.spectator.getValue()) {
                if (table.standUp(player.getSeatNum(), player.getPlayerId(), "TableSubService-SwitchTable")) {
                    //玩家游戏数据结算
                    table.playerDataSettlement(player);
                }
            }
            UserTableService.getInstance().onPlayerOutTable(player);

            //清除player信息
            table.returnLobby(player.getPlayerId(), false);

            JoloGame.JoloGame_TablePlay_OtherPlayerInfo.Builder playerInfo = JoloGame.JoloGame_TablePlay_OtherPlayerInfo.newBuilder();
            playerInfo.setUserId(player.getPlayerId());
            playerInfo.setHandsTimes(player.getHandsWon().getLinkedDeque().size());
            playerInfo.setNickName(player.getNickName());
            playerInfo.setHandsWon(player.getHandsWon().won());
            playerInfo.setPlayScoreStore(player.getPlayScoreStore());
            playerInfo.setIcon(player.getIcon());
            playerInfo.setBiggest(player.getBiggestChipsWon());
            log.info("player info:" + playerInfo.toString());


            //设置座位
            StoredObjManager.hdel(RedisConst.USER_TABLE_SEAT.getProfix(), RedisConst.USER_TABLE_SEAT.getField() + player.getPlayerId());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return result;
    }
}
