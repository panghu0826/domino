package com.jule.domino.game.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import com.google.common.base.Strings;
import com.jule.core.common.log.LoggerUtils;
import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;

import com.jule.domino.base.model.GameRoomTableSeatRelationModel;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.RoomConfigModel;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.play.TableUtil;
import com.jule.domino.game.service.LogService;
import com.jule.domino.game.service.PlayerService;
import com.jule.domino.game.service.RoomStateService;
import com.jule.domino.game.service.TableService;
import com.jule.domino.game.utils.log.TableLogUtil;
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.enums.RoleType;
import com.jule.domino.base.model.RoomTableRelationModel;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.acks.JoloGame_ApplySitDownAck_50001;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 申请入座(无指定座位随机)
 * return：
 */
@Slf4j
public class JoloGame_ApplySitDownReq_50001 extends ClientReq {
    private long time;

    public JoloGame_ApplySitDownReq_50001(int functionId) {
        super(functionId);
    }

    private JoloGame.JoloGame_ApplySitDownReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        time = System.currentTimeMillis();
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloGame.JoloGame_ApplySitDownReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId,req.toString());
    }

    @Override
    public void processImpl() throws Exception {
//        log.debug("收到消息, functionId->" + functionId + ", reqNum->" + header.reqNum + ", req->" + req.toString());
        log.info("收到消息, functionId->" + functionId + ", req->" + req.toString());
        JoloGame.JoloGame_ApplySitDownAck.Builder ack = JoloGame.JoloGame_ApplySitDownAck.newBuilder();
        AbstractTable table = null;

        String userId = req.getUserId();
        String roomId = req.getRoomId();
        String tableId = req.getTableId();
        int seatNum = req.getSeatNum();
//        double buyInScore = -1;
//        boolean success = StoredObjManager.hsetnx(
//                RedisConst.HASH_SET_NX.getProfix() + "REQ",
//                RedisConst.HASH_SET_NX.getField() + functionId + userId,
//                "" + System.currentTimeMillis());
//        if (!success) {
//            return;
//        }
        ack.setUserId(userId);
        ack.setRoomId(roomId);
        ack.setSeatNum(seatNum);
        ack.setTableId(tableId);
        ack.addAllChangeCards(new ArrayList<>());
        ack.addAllPlayerInfoList(new ArrayList<>());
        try {
            PlayerInfo player = null;
//            PlayerInfo player = table.getPlayer(userId);
//            if(player != null){
//                log.debug("玩家已在游戏中！！！");
//            }

            //用户信息：从缓存获取
            User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId, User.class);
            if (null == user) {
                log.debug("  -2  can't found user, userId->" + userId);
                ack.setResult(-2).setResultMsg(ErrorCodeEnum.GAME_50001_2.getCode());
                ctx.writeAndFlush(new JoloGame_ApplySitDownAck_50001(ack.build(), header));
                return;
            }

            table = TableService.getInstance().addNewTable(header.gameId + "", roomId, tableId);
            //记录桌子最后操作时间
            table.setLastActionTime(System.currentTimeMillis());
            player = table.getPlayer(userId);
            ack.setTableId(tableId);

            //首先查看自己有没有在游戏内
//            GameRoomTableSeatRelationModel gameRoomTable = StoredObjManager.getStoredObjInMap(GameRoomTableSeatRelationModel.class, RedisConst.USER_TABLE_SEAT.getProfix(),
//                    RedisConst.USER_TABLE_SEAT.getField() + userId);
//            if (gameRoomTable == null){
//                AbstractTable tableTmp = TableService.getInstance().getRandomTable(roomId, String.valueOf(header.gameId), tableId);
//                //创建player对象
//                player = new PlayerInfo(roomId, tableId, user.getId(), user.getNick_name(), user.getIco_url());
//                //将玩家加入到table信息中（完成玩家的入桌状态）
//                tableTmp.joinRoom(player);
//                tableId = tableTmp.getTableId();
//            }else {
//                tableId = gameRoomTable.getTableId();
//            }


            //缓存玩家使用的服务器IP、负载时使用
//            JedisPoolWrap.getInstance().set(RedisConst.USER_LOGIN_GAME_URL.getProfix(), Config.REST_IP + ":" + Config.REST_PORT, -1);
//            //从redis获取桌子的信息
//            RoomTableRelationModel roomTable = RoomStateService.getInstance().getExistTable("" + header.gameId, roomId, tableId);
//            if (roomTable == null) {
//                log.error("can't found table, gameId:{},roomId:{},tableId:{}", header.gameId, roomId, tableId);
//                ack.setSeatNum(0);
//                ack.setResult(-1).setResultMsg(ErrorCodeEnum.GAME_50001_1.getCode());
//                ctx.writeAndFlush(new JoloGame_ApplySitDownAck_50001(ack.build(), header));
//                return;
//            }



            //从redis获取桌子的信息
//            RoomTableRelationModel roomTable = RoomStateService.getInstance().getExistTable(header.gameId + "", roomId, tableId);
//            String icon = StringUtils.isEmpty(user.getUser_defined_head()) ? user.getIco_url() : user.getUser_defined_head();
////            PlayerInfo player = table.getPlayer(userId);
//            if (player == null) {
//                player = new PlayerInfo(roomTable, userId, user.getNick_name(), icon, RoleType.getRoleType(user.getChannel_id()),user);
//            }
//            String res = StoredObjManager.hget(RedisConst.TABLE_USERS.getProfix() + header.gameId + player.getRoomId() + player.getTableId(),
//                    RedisConst.TABLE_USERS.getField() + player.getPlayerId());
//            log.info("res" + res);
//            if (Strings.isNullOrEmpty(res)) {
//                log.debug("  -10  桌内查无此人, userId->" + userId + ", roomId->" + roomId + ",tableId->" + tableId);
//                ack.setSeatNum(0);
//                ack.setResult(-10).setResultMsg(ErrorCodeEnum.GAME_50050_2.getCode());
//                ctx.writeAndFlush(new JoloGame_ApplySitDownAck_50001(ack.build(), header));
//                return;
//            }
//            if (res.equals("" + PlayerStateEnum.siteDown.getValue())) {
//                log.error("Failed to sit down many times");
//                return;
//            }

            //修改相关map内容，增加新玩家入桌状态
            table.joinTable(player);

//            if (user.getMoney() <= 0) {
//                ack.setSeatNum(0);
//                ack.setResult(-3).setResultMsg(ErrorCodeEnum.GAME_50013_3.getCode());
//                ctx.writeAndFlush(new JoloGame_ApplySitDownAck_50001(ack.build(), header));
//                return;
//            }

//            buyInScore = user.getMoney();
//            double currScoreStore = user.getMoney(); //玩家当前积分库存
//            if (currScoreStore < table.getRoomConfig().getMinScore4JoinTable()) {
//                ack.setSeatNum(0);
//                ack.setResult(-12).setResultMsg(ErrorCodeEnum.GAME_50013_3.getCode());
//                ctx.writeAndFlush(new JoloGame_ApplySitDownAck_50001(ack.build(), header));
//                return;
//            }

            if (!req.hasRoomId()) {
                //log.debug("  -4  can't found suitable Room. userScoreStore->" + currScoreStore);
                ack.setSeatNum(0);
                ack.setResult(-4).setResultMsg(ErrorCodeEnum.GAME_50001_3.getCode());
                ctx.writeAndFlush(new JoloGame_ApplySitDownAck_50001(ack.build(), header));
                return;
            }

            //创建player对象
//            player = table.getPlayer(userId);
            if (null == player) {
                log.debug("  -5  can't found suitable player. player->" + player);
                ack.setSeatNum(0);
                ack.setResult(-5).setResultMsg(ErrorCodeEnum.GAME_50050_2.getCode());
                ctx.writeAndFlush(new JoloGame_ApplySitDownAck_50001(ack.build(), header));
                return;
            }

            ack.setResult(1);
            ack.setTableState(table.getTableStateEnum().getValue());
//            ack.setCurrPlayScore(buyInScore);
            ack.setTotalAlreadyBet(0);
            ack.setSpecialFunction(player.getSpecialFunction());

//            List<RoomConfigModel> configList = DBUtil.getRoomConfigFromDb();
//            Collections.sort(configList);
//            log.debug(player.playerToString() + "当前房间最低下注额：" + table.getRoomConfig().getAnte());
//            if (buyInScore < table.getRoomConfig().getAnte() || buyInScore < configList.get(0).getAnte()) {
//                log.debug(player.playerToString() + "玩家积分低于当前房间最低下注额 ante->" + table.getRoomConfig().getAnte());
//                ack.setSeatNum(0);
//                ack.setResult(-11).setResultMsg(ErrorCodeEnum.GAME_50013_3.getCode());
//                ctx.writeAndFlush(new JoloGame_ApplySitDownAck_50001(ack.build(), header));
//                return;
//            }

            //如果未指定座位下，那么随机分配一个座位给玩家
            log.debug("Random seat room={},table={},userid={},palyers={}.",req.getRoomId(),req.getTableId(),req.getUserId(),TableUtil.toStringInGamePlayers(table));
            if(!req.hasSeatNum()) {
                seatNum = table.getNulSeatNum();
            }
            log.debug("Random seat room={},table={},userid={},Num ={}." ,req.getRoomId(),req.getTableId(),req.getUserId(), seatNum );

            if (seatNum == 0) {
                ack.setSeatNum(0);
                ack.setResult(-13).setResultMsg(ErrorCodeEnum.GAME_50013_4.getCode());
                ctx.writeAndFlush(new JoloGame_ApplySitDownAck_50001(ack.build(), header));
                return;
            }

            //初始化ack信息
            ack.setSeatNum(seatNum);

            boolean suc = table.sitDown(seatNum, userId);

            //坐下
            if (suc) {
                //增加一个已坐下判断
                StoredObjManager.hset(RedisConst.TABLE_USERS.getProfix() + header.gameId + player.getRoomId() + player.getTableId(),
                        RedisConst.TABLE_USERS.getField() + player.getPlayerId(), "" + PlayerStateEnum.siteDown.getValue());

                StoredObjManager.hset(RedisConst.CHANGE_TABLE_STAT.getProfix(),
                        RedisConst.CHANGE_TABLE_STAT.getField() + userId, "0");
                //代入积分
                //long currentMoney = MoneyService.getInstance().buyScore(player.getPlayerId(), buyInScore);
//                log.debug(player.playerToString() + "玩家带入筹码：" + buyInScore);
//                player.setTotalWinLoseScore(0);
                player.setState(PlayerStateEnum.siteDown); //修改玩家状态值
//                player.setPlayScoreStore(buyInScore);
//                player.setTotalTakeInScore(buyInScore);
                //本局癞子牌
                ack.setMixedCardId(0);
                //本局换出去的牌
                ack.addAllChangeCards(table.getChangeCards());
                ack.setGameOrderId(table.getCurrGameOrderId());
                //判断玩家在不在游戏中
                if (table.getTableStateEnum().getValue() > TableStateEnum.GAME_READY.getValue()) {
                    ack.setNotInGame(1);
                }
                ack.setTableState(table.getTableStateEnum().getValue());
                ack.setPlayType("");
                ctx.writeAndFlush(new JoloGame_ApplySitDownAck_50001(ack.addAllPlayerInfoList(TableUtil.getPlayers(table)).build(), header));
                //桌子复盘记录（坐下）
                log.debug("玩家入座成功：" + player.toSitDownString());

                //广播玩家入座
                try {
                    NoticeBroadcastMessages.playerSitDown(table, userId);
                } catch (Exception ex) {
                    log.error("SendNotice ERROR：", ex);
                }
                //保存所在桌内位置信息
                //判断：如果在座玩家超过两人并且桌子状态为空闲状态
//                TableService.getInstance().playGame(table); //开始游戏
                if (!user.getChannel_id().equals(RoleType.ROBOT.getTypeName())) {
                    PlayerService.getInstance().onPlayerLogin(userId);
                }
                //发送坐下日志
                LogService.OBJ.sendGamesitLog(user, table);
            } else {
                table.addNullSeat(seatNum);//如果
                ack.setSeatNum(0);
                log.debug("  -9  玩家入座失败" + player.playerToString());
                ack.setResult(-9).setResultMsg(ErrorCodeEnum.GAME_50001_7.getCode());
                ctx.writeAndFlush(new JoloGame_ApplySitDownAck_50001(ack.build(), header));
            }
            TableLogUtil.sitdown(functionId, "SitDownReq", userId, user.getNick_name(), user.getChannel_id(), "" + table.getPlayType(),
                    table.getRoomId(), table.getTableId(), seatNum, suc, player.getPlayScoreStore(), table.getInGamePlayersBySeatNum().size());
            long timeMillis = System.currentTimeMillis() - time;
            if (timeMillis > GameConst.COST_TIME) {
                LoggerUtils.performance.info("ApplySitDownReq_50001,cost time:{},req:{}", timeMillis, req.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            log.debug("50001 ack 玩家入座: {}",ack.toString());
        }
    }
}
