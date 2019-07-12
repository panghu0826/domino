package com.jule.domino.game.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import com.google.common.base.Strings;
import com.jule.core.common.log.LoggerUtils;
import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.acks.JoloGame_ReconnectAck_50016;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.play.TableUtil;
import com.jule.domino.game.service.PlayerService;
import com.jule.domino.game.service.RoomStateService;
import com.jule.domino.game.service.TableService;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.enums.RoleType;
import com.jule.domino.base.model.RoomTableRelationModel;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class JoloGame_ReconnectReq_50016 extends ClientReq {
    private long time;
    private JoloGame.JoloGame_ReconnectReq req;

    public JoloGame_ReconnectReq_50016(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        time = System.currentTimeMillis();
        log.debug("收到消息");
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloGame.JoloGame_ReconnectReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId,req.toString());
    }

    @Override
    public void processImpl() throws Exception {
        //1很正常坐下
        //2已被站起 随机坐下or没座位被弹出到大厅
        log.debug("收到消息, functionId->" + functionId + ", reqNum->" + header.reqNum + ", req->" + req.toString());
        JoloGame.JoloGame_ReconnectAck.Builder ack = JoloGame.JoloGame_ReconnectAck.newBuilder();
        AbstractTable table = null;

        String userId = req.getUserId();
        String roomId = req.getRoomId();
        String tableId = req.getTableId();
        int seatNum = req.getSeatNum();
        double buyInScore = -1;

        ack.setUserId(userId);
        ack.setRoomId(roomId);
        ack.setTableId(tableId);
        ack.setSeatNum(seatNum);
        ack.setGameOrderId("0");
        ack.setBetRoundId(0);
        try {
            //缓存玩家使用的服务器IP、负载时使用
//            JedisPoolWrap.getInstance().set(RedisConst.USER_LOGIN_GAME_URL.getProfix(), Config.REST_IP + ":" + Config.REST_PORT, -1);

            RoomTableRelationModel roomTable = RoomStateService.getInstance().getExistTable(table.getPlayType()+"", roomId, tableId);

            if (roomTable == null) {
                log.error("can't found table, gameId:{},roomId:{},tableId:{}", header.gameId, roomId, tableId);
                ack.setSeatNum(0);
                ack.setResult(-1).setResultMsg(ErrorCodeEnum.GAME_50001_1.getCode());
                ctx.writeAndFlush(new JoloGame_ReconnectAck_50016(ack.build(), header));
                return;
            }

            table = TableService.getInstance().addNewTable(header.gameId + "", roomId, tableId);
            if (table == null) {
                table.returnLobby(userId, false);
                log.debug("  -12  can't found user, userId->" + userId);
                ack.setSeatNum(0);
                ack.setResult(-12).setResultMsg(ErrorCodeEnum.GAME_50001_2.getCode());
                ctx.writeAndFlush(new JoloGame_ReconnectAck_50016(ack.build(), header));
                return;
            }
            ack.setBetRoundId(0);
            //用户信息：从缓存获取
            User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId, User.class);
            if (null == user) {
                table.returnLobby(userId, false);
                log.debug("  -2  can't found user, userId->" + userId);
                ack.setSeatNum(0);
                ack.setResult(-2).setResultMsg(ErrorCodeEnum.GAME_50001_2.getCode());
                ctx.writeAndFlush(new JoloGame_ReconnectAck_50016(ack.build(), header));
                return;
            }
            log.debug("玩家当前的积分数量：==============" + user.getMoney());

            String icon = StringUtils.isEmpty(user.getUser_defined_head()) ? user.getIco_url() : user.getUser_defined_head();
            PlayerInfo player = new PlayerInfo(roomTable, userId, user.getNick_name(), icon, RoleType.getRoleType(user.getChannel_id()),user);
            String res = StoredObjManager.hget(RedisConst.TABLE_USERS.getProfix() + header.gameId + player.getRoomId() + player.getTableId(),
                    RedisConst.TABLE_USERS.getField() + player.getPlayerId());
            log.info("res" + res);
            if (Strings.isNullOrEmpty(res)) {
                table.returnLobby(userId, false);
                log.debug("  -10  桌内查无此人, userId->" + userId + ", roomId->" + roomId + ",tableId->" + tableId);
                ack.setSeatNum(0);
                ack.setResult(-10).setResultMsg(ErrorCodeEnum.GAME_50050_2.getCode());
                ctx.writeAndFlush(new JoloGame_ReconnectAck_50016(ack.build(), header));
                return;
            }
            if (user.getMoney() <= 0) {
                log.debug("  -3  用户余额不足, userId->" + userId + ", money->" + user.getMoney());
                ack.setSeatNum(0);
                ack.setResult(-3).setResultMsg(ErrorCodeEnum.GAME_50013_3.getCode());
                ctx.writeAndFlush(new JoloGame_ReconnectAck_50016(ack.build(), header));
                return;
            }

            if (req.hasBuyInScore()) {
                buyInScore = req.getBuyInScore();
            } else {
                buyInScore = user.getMoney();
            }

            //创建player对象
            log.debug(table.getPlayer(userId) + ":---" + userId);
            player = table.getPlayer(userId);
            if (null == player) {
                table.returnLobby(userId, false);
                log.debug("  -5  can't found suitable player. player->" + player);
                ack.setSeatNum(0);
                ack.setResult(-5).setResultMsg(ErrorCodeEnum.GAME_50050_2.getCode());
                ctx.writeAndFlush(new JoloGame_ReconnectAck_50016(ack.build(), header));
                return;
            }
            player.setOffLine(false);


            ack.setResult(1);
            ack.setTableState(table.getTableStateEnum().getValue());
            ack.setCurrPlayScore(buyInScore);
            ack.setTotalAlreadyBet(player.getBetMultiple());
            //初始化ack信息
            ack.setSeatNum(player.getSeatNum());
            //坐下
            StoredObjManager.hset(RedisConst.CHANGE_TABLE_STAT.getProfix(),
                    RedisConst.CHANGE_TABLE_STAT.getField() + userId, "0");
            //代入积分
            //long currentMoney = MoneyService.getInstance().buyScore(player.getPlayerId(), buyInScore);
            log.debug("玩家带入筹码：" + buyInScore);
            //player.setTotalWinLoseScore(0);
            //player.setState(PlayerStateEnum.siteDown); //修改玩家状态值
            //player.setSeatNum(seatNum);
            //player.setPlayScoreStore(buyInScore);
            //player.setTotalTakeInScore(buyInScore);
            //本局癞子牌
                ack.setMixedCardId(0);
            //本局换出去的牌
            ack.addAllChangeCards(table.getChangeCards());
            ack.setGameOrderId(table.getCurrGameOrderId());
            //判断玩家在不在游戏中
            if (player.getState().getValue() > 1) {
                ack.setNotInGame(0);
            } else {
                ack.setNotInGame(1);
            }
            ack.setPlayType(String.valueOf(header.gameId));
            ctx.writeAndFlush(new JoloGame_ReconnectAck_50016(ack
                    .addAllPlayerInfoList(TableUtil.getPlayers(table)).build(), header));
            log.debug("玩家入座成功：" + player.toSitDownString());
            //修改房间信息：玩家坐下
            //RoomStateService.getInstance().onPlayerSitDown(table, player);

            //保存所在桌内位置信息
            //判断：如果在座玩家超过两人并且桌子状态为空闲状态
//            TableService.getInstance().playGame(table); //开始游戏
            PlayerService.getInstance().onPlayerLogin(userId);

            long timeMillis = System.currentTimeMillis() - time;
            if (timeMillis > GameConst.COST_TIME) {
                LoggerUtils.performance.info("ReconnectReq_50016,cost time:{},req:{}", timeMillis, req.toString());
            }
        } catch (Exception e) {
            ack.setSeatNum(0).setResult(-10).setResultMsg(ErrorCodeEnum.GAME_50002_2.getCode());
            ctx.writeAndFlush(new JoloGame_ReconnectAck_50016(ack.build(), header));
            log.error("", e);
        } finally {
            log.debug("ACK info: " + ack.toString());
            if (null != table) {
                log.debug("All Player info: " + System.getProperty("line.separator") + TableUtil.toStringAllPlayers(table));
                log.debug("InGame Player info: " + System.getProperty("line.separator") + TableUtil.toStringInGamePlayers(table));
            }
        }

    }
}
