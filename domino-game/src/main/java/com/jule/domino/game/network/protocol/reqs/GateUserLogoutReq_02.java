package com.jule.domino.game.network.protocol.reqs;

import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.game.network.protocol.logic.LeaveTableLogic;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.*;
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RoleType;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.log.service.LogReasons;
import com.jule.core.common.log.LoggerUtils;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 玩家下线请求
 */
public class GateUserLogoutReq_02 extends ClientReq {

    private final static Logger logger = LoggerFactory.getLogger(GateUserLogoutReq_02.class);

    private long userId;

    public long getUserId() {
        return userId;
    }

    public GateUserLogoutReq_02(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf byteBuf) throws Exception {
        userId = header.channelId;
        PlayerService.getInstance().onPlayerLoutOut("" + userId);
        if (userId >= 0) {
            AbstractTable table = UserTableService.getInstance().getTableByUserId(userId);
            if (table != null) {
                setTable(table);
            }
        }
    }

    @Override
    public void processImpl() {
        logger.info("玩家离线不做退出！！！");
        logger.info("Req logout functionId-> " + functionId + ", reqNum-> " + header.reqNum + ", userId->" + getUserId());
        AbstractTable table = getTable();
        if (table != null) {
            PlayerInfo player = table.getPlayer(userId + "");
            if(player.getTotalAlreadyBetScore4Hand() == 0) {
                //处理玩家离桌的redis信息
                LeaveTableLogic.getInstance().logic(player, table);
            }
            NoticeBroadcastMessages.sendPlayerLeaveNotice(table, player);//玩家离桌广播
        }

        if(true){
            return;
        }
        //后续操作暂时用不到所以直接return


        if (table != null) {
            logger.info("处理退出用户状态之前->" + table.toString());
            PlayerInfo currentPlayer = table.getPlayer(userId + "");
            if (currentPlayer != null) {
                logger.info("currentPlayer not null, userId->{}, playerState->{}", userId, currentPlayer.getState());
                if (currentPlayer.getState() == PlayerStateEnum.spectator) {
                    spectatorHandler(table);
                }
//                else if (currentPlayer.getState() == PlayerStateEnum.siteDown) {
//                    sitDownHandler(table, currentPlayer);
//                }
                else {
                    offlineHandler(table, currentPlayer);
                }
                logger.info("处理退出用户状态之后->" + table.toString());
            } else {
                logger.debug("Req logout, currentPlayer is null, userId->{}, {}", userId, toStringTable(table));
            }
            /*
            if(currentPlayer.getState() == PlayerStateEnum.siteDown) {//modify lyb 2018-07-20 当为坐下状态时则发布离桌
                //如果不允许断线续玩，那么直接清除玩家在游戏内的缓存数据
                JSONObject js = new JSONObject();
                js.put("gameId", "" + table.getPlayType());
                js.put("roomId", table.getRoomId());
                js.put("tableId", table.getTableId());
                js.put("userId", userId);
                logger.info("清理玩家在游戏内的缓存，mustReconnectContinueGame->{}, js->{}"
                        ,GameConst.mustReconnectContinueGame, js.toString());
                StoredObjManager.publish(js.toString(),
                        RedisChannel.LEAVE_TABLE_CHANNEL.getChannelName());
            }
            */
        }
        //StoredObjManager.hdel(RedisConst.USER_INFO.getProfix(),RedisConst.USER_INFO.getField()+getUserId());
    }

    private void spectatorHandler(AbstractTable table) {
        table.returnLobby(userId + "");
        TableService.getInstance().publishLeaveTable(table, "" + userId);
    }

    private void sitDownHandler(AbstractTable table, PlayerInfo currentPlayer) {
        logger.debug("离线玩家入座状态（siteDown），需要先站起在离开, userId->{}, {}",
                userId, toStringTable(table));
        try {
            table.standUp(currentPlayer.getSeatNum(), currentPlayer.getPlayerId(), "logoutReq-sitDownHandler");
            if (currentPlayer.getRoleType() != null && currentPlayer.getRoleType().equals(RoleType.ROBOT)) {
                LoggerUtils.robot.info("Robot standUp reason logout sitDownHandler id:" + currentPlayer.getPlayerId() + ",gameId:" + table.getPlayType()
                        + ",roomId:" + table.getRoomId() + ",tableId:" + table.getTableId());
            }
        } catch (Exception ex) {
            logger.error("UserLogoutReq seatNum->{}, userId->{}, {}, ex->{}",
                    currentPlayer.getSeatNum(), userId, toStringTable(table), ex.getMessage()
                    , ex);
        }

        //恢复用户缓存和数据库内的缓存钱数
        MoneyService.getInstance().updateMoney(currentPlayer.getPlayerId(), currentPlayer.getPlayScoreStore());
        //发送日志
        User user = DBUtil.selectByPrimaryKey(currentPlayer.getPlayerId());
        if (user != null) {
            LogService.OBJ.sendMoneyLog(user, user.getMoney(), currentPlayer.getPlayScoreStore(), user.getMoney() - currentPlayer.getPlayScoreStore(), LogReasons.CommonLogReason.OFF_LINE_RECOVER);
        }
        //广播玩家站起消息
        NoticeBroadcastMessages.playerStandUp(table, userId + "", currentPlayer.getSeatNum());

        currentPlayer.setState(PlayerStateEnum.spectator);//设置玩家状态为旁观
        table.returnLobby(userId + "");

        TableService.getInstance().publishLeaveTable(table, "" + userId);
    }


    private void offlineHandler(AbstractTable table, PlayerInfo currentPlayer) {
        logger.debug("离线玩家在游戏中>>>>>>>>>>>>>>>>>>>>>>>>游戏状态(else player state)，userId->{}, {}",
                userId, toStringTable(table));
        //标记
        if (GameConst.mustReconnectContinueGame) {
            currentPlayer.setOffLine(true);
            return;
        }
    }

    private String toStringTable(AbstractTable table) {
        String str = ", TABLE_INFO::: tableIsNull->" + (null == table)
                + ", tableId->" + (null != table ? table.getTableId() : "")
                + ", roomId->" + (null != table ? table.getRoomId() : "")
                + ", gameId->" + (null != table ? table.getPlayType() : "");
        return str;
    }
}
