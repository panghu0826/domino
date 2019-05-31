package com.jule.domino.game.room.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import JoloProtobuf.RoomSvr.JoloRoom;
import com.google.common.base.Strings;
import com.google.common.primitives.Ints;
import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.*;
import com.jule.domino.base.model.GameRoomTableSeatRelationModel;
import com.jule.domino.base.model.RoomTableRelationModel;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.FriendTableModel;
import com.jule.domino.game.dao.bean.TableCreationRecordsModel;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.room.network.protocol.acks.JoloRoom_ApplyJoinTableAck_40001;
import com.jule.domino.game.service.TableService;
import com.jule.domino.game.service.GameMaintentService;
import com.jule.domino.game.service.RoomStateService;
import com.jule.domino.game.service.holder.CommonConfigHolder;
import com.jule.domino.game.service.holder.RoomConfigHolder;
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 申请入桌(换桌)
 */
@Slf4j
public class JoloRoom_ApplyJoinTableReq_40001 extends ClientReq {

    private JoloRoom.JoloRoom_ApplyJoinTableReq req;

    public JoloRoom_ApplyJoinTableReq_40001(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf byteBuf) throws Exception {
        byte[] playLoad = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(playLoad);
        req = JoloRoom.JoloRoom_ApplyJoinTableReq.parseFrom(playLoad);
        RabbitMqSender.me.producer(functionId, req.toString());
    }

    @Override
    public void processImpl() {
        JoloRoom.JoloRoom_ApplyJoinTableAck.Builder ack = JoloRoom.JoloRoom_ApplyJoinTableAck.newBuilder();
//        log.debug("收到消息-> " + functionId + " reqNum-> " + header.reqNum + " " + req.toString());
        log.debug("收到消息-> " + functionId + ", " + req.toString());
        String userId = req.getUserId();
        String gameId = req.getGameId();
        String roomId = req.getRoomId();
        String tableId = "";
        int playerNum = req.getPlayerNum();
        AbstractTable table = null;

        ack.setUserId(userId).setGameId(gameId).setRoomId(roomId).setTableId(tableId);
        try {
            //用户信息：从缓存获取
            User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId, User.class);
            if (user == null) {
                log.error("缓存中没有user信息：{}, --{}, --{}", RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId, User.class);
                return;
            }
            String gameSvrId = /*UtilsService.getInstance().getGameSvr(table, gameId)*/Config.GAME_SERID;
            if (!StringUtil.isNullOrEmpty(gameSvrId)) {
                ack.setJoinGameSvrId(gameSvrId);//没有时前端提示 "服务器爆满"
            }
            //首先查看自己有没有在游戏内
//            GameRoomTableSeatRelationModel gameRoomTableSeatRelationModel = StoredObjManager.getStoredObjInMap(
//                    GameRoomTableSeatRelationModel.class,
//                    RedisConst.USER_TABLE_SEAT.getProfix(),
//                    RedisConst.USER_TABLE_SEAT.getField() + userId
//            );
//            log.info("玩家是否在游戏中：{}",(gameRoomTableSeatRelationModel != null));
            //创建房间扣除相应房卡
//            if(!req.hasTableId() && gameRoomTableSeatRelationModel == null) {
            if (!req.hasTableId()) {
                int money = req.getGameNum() / 5;
                if (user.getMoney() >= money) {
                    user.setMoney(user.getMoney() - money);
                    DBUtil.updateByPrimaryKey(user);
                } else {
                    //创建房间失败
                    ack.setResult(-1);
                    ack.setResultMsg("玩家房卡不足");
                    ack.setBetMultiple("");
                    ack.addAllPlayerInfoList(new ArrayList<>());
                    ack.setTableInfo(JoloRoom.JoloGame_Table_Info.newBuilder()
                            .setTableId("")
                            .setPlayerNum(0)
                            .setBaseBetScore(0)
                            .setBetMaxScore(0)
                            .setGameNum(0)
                            .setCurrGameNum(0).build());
                    ctx.writeAndFlush(new JoloRoom_ApplyJoinTableAck_40001(ack.build(), header));
                    return;
                }
            }

            //玩家在在游戏中走断线重连，否则新创建桌子
//            if (gameRoomTableSeatRelationModel != null) {
//                if (gameRoomTableSeatRelationModel.getSeat() >= 0) {//在座位上或旁观
//                    ack.setSeatId(gameRoomTableSeatRelationModel.getSeat() + "");
//                    ack.setReconnection(1);
//                }
//                table = TableService.getInstance().addNewTable(gameRoomTableSeatRelationModel.getGameId(),
//                        gameRoomTableSeatRelationModel.getRoomId(), gameRoomTableSeatRelationModel.getTableId());
//                tableId = table.getTableId();
//            } else {
//                if(!req.hasTableId()) {
//                    ...
//                }
//            }
            if (!req.hasTableId()) {
                table = TableService.getInstance().createNewTable(gameId, roomId, playerNum);
                table.setEveryoneCanJoinIn(!"true".equals(user.getMei_code()));
                initTable(table, userId);//初始化桌子参数
                ack.setReconnection(0);
                tableId = table.getTableId();
                insertTableCreationRecord(table);//插入桌子创建记录
            } else {
                ack.setReconnection(0);
                tableId = req.getTableId();
                //如果有桌子则加入，否则创建
                table = TableService.getInstance().addNewTable(gameId, roomId, tableId);
            }

            if (table == null) {
                log.error("找不到桌子对象：gameId: {}, roomId: {}, tableId: {}", gameId, roomId, tableId);
                ack.setTableId("").setBetMultiple("").addAllPlayerInfoList(new ArrayList<>()).setResultMsg("桌子不存在");
                ack.setTableInfo(JoloRoom.JoloGame_Table_Info.newBuilder()
                        .setTableId("")
                        .setPlayerNum(0)
                        .setBaseBetScore(0)
                        .setGameNum(0)
                        .setCurrGameNum(0)
                        .setBetMaxScore(0)
                        .build());
                ctx.writeAndFlush(new JoloRoom_ApplyJoinTableAck_40001(ack.setResult(-2).build(), header));
                return;
            }

            if (req.hasTableId() && !table.isEveryoneCanJoinIn() && !userId.equals(table.getCreateTableUserId())) {
                //桌子不是所有人可加入，判断是好友否
                List<FriendTableModel> friends = DBUtil.selectFriendByUserId(table.getCreateTableUserId());
                boolean isFriend = false;
                if (friends != null) {
                    for (FriendTableModel ftm : friends) {
                        if (userId.equals(ftm.getFriendUserId()) && ftm.getState() == 3) {
                            isFriend = true;
                        }
                    }
                }
                if (!isFriend) {
                    ack.setTableId("").setBetMultiple("").addAllPlayerInfoList(new ArrayList<>()).setResultMsg("该桌子开启了好友验证，你还不是他的好友");
                    ack.setTableInfo(JoloRoom.JoloGame_Table_Info.newBuilder()
                            .setTableId("")
                            .setPlayerNum(0)
                            .setBaseBetScore(0)
                            .setGameNum(0)
                            .setCurrGameNum(0)
                            .setBetMaxScore(0).build());
                    ctx.writeAndFlush(new JoloRoom_ApplyJoinTableAck_40001(ack.setResult(-3).build(), header));
                    return;
                }
            }

            //从redis获取桌子的信息
            RoomTableRelationModel roomTable = RoomStateService.getInstance().getExistTable(gameId, roomId, tableId);
//            String icon = StringUtils.isEmpty(user.getUser_defined_head()) ? user.getIco_url() : user.getUser_defined_head();
            String icon = user.getIco_url();
            PlayerInfo player = table.getPlayer(userId);
            //修改相关map内容，增加新玩家进入房间状态
            if (player == null) {
                player = new PlayerInfo(roomTable, userId, user.getNick_name(), icon, RoleType.getRoleType(user.getChannel_id()), user);
            }
            //首先查看自己有没有在游戏内
            GameRoomTableSeatRelationModel gameRoomTableSeatRelationModel = StoredObjManager.getStoredObjInMap(
                    GameRoomTableSeatRelationModel.class,
                    RedisConst.USER_TABLE_SEAT.getProfix(),
                    RedisConst.USER_TABLE_SEAT.getField() + userId
            );
            //设置桌子的最后操作时间
            table.setLastActionTime(System.currentTimeMillis());
            //如果玩家不在游戏中 将玩家加入房间
            if (gameRoomTableSeatRelationModel == null) {
                table.joinRoom(player);
            }else {
                if(!tableId.equals(gameRoomTableSeatRelationModel.getTableId())){
                    table.joinRoom(player);
                }else {
                    log.debug("玩家回到自己原来的桌子不做加入游戏：{}",table.getAllPlayers().containsKey(player.getPlayerId()));
                }
            }
            log.info("房间总人数：{}，座位上人数：{}，游戏中人数：{}", table.getAllPlayers().size(), table.getInGamePlayersBySeatNum().size(), table.getInGamePlayers().size());
            player.setHeadSculpture(req.getHeadSculpture());
            player.setCardSkin(req.getCardSkin());
            player.setSpecialFunction("vip".equals(user.getDevice_num()) ? 2 : "true".equals(user.getDevice_num()) ? 1 : 0);

            ack.setTableId(tableId);
            ack.addAllPlayerInfoList(getPlayersInTable(table, userId));
            ack.setTableInfo(getCurrTableInfo(table));
            ack.setBetMultiple(table.getBetMultiple());
            ack.setMoney((int) user.getMoney());
            //缓存玩法
            JedisPoolWrap.getInstance().set(GameConst.CACHE_PLAY_TYPE + userId, gameId, -1);
            ctx.writeAndFlush(new JoloRoom_ApplyJoinTableAck_40001(ack.setResult(1).build(), header));
            //通知有玩家入桌
            NoticeBroadcastMessages.playerJoinTable(table,player);
            NoticeBroadcastMessages.useItem(table,req.getUserId(),req.getHeadSculpture(),req.getCardSkin());
            log.info("40001 builder ack 成功->: {}", ack.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    //初始化桌子参数
    private void initTable(AbstractTable table, String createTableUserId) {
        table.setPlayerNum(req.getPlayerNum());
        table.setCurrBaseBetScore(req.getBaseBetScore());
        table.setReadyCd(req.hasReadyCd() ? req.getReadyCd() : 8);
        table.setBetCd(req.hasBetCd() ? req.getBetCd() : 15);
        table.setOpenCardCd(req.hasOpenCardCd() ? req.getOpenCardCd() : 5);
        table.setWatch(req.hasIsWatch());
        table.setBetMaxScore(req.getBetMaxScore());
        table.setGameNum(req.getGameNum());
        table.setBetMultiple(req.getBetMultiple());
        table.setCreateTableUserId(createTableUserId);
        log.debug("新建桌子参数：{}", table.toString());
    }

    public void insertTableCreationRecord(AbstractTable table) {
        TableCreationRecordsModel tcrm = new TableCreationRecordsModel();
        tcrm.setTableId(table.getTableId());
        tcrm.setCreateTime(new Date());
        tcrm.setPlayerNum(table.getPlayerNum());
        tcrm.setBaseScore((int) table.getCurrBaseBetScore());
        tcrm.setReadyCd(table.getReadyCd());
        tcrm.setBetCd(table.getBetCd());
        tcrm.setOpenCardCd(table.getOpenCardCd());
        tcrm.setBetMaxScore(table.getBetMaxScore());
        tcrm.setGameNum(table.getGameNum());
        tcrm.setBetMultiple(table.getBetMultiple());
        tcrm.setIsWatch(table.isWatch() ? 1 : 0);
        tcrm.setCreateUserId(req.getUserId());
        tcrm.setTableState(0);
        tcrm.setGameId(req.getGameId());
        DBUtil.insertTableCreateRecord(tcrm);//插入桌子创建记录
    }

    private JoloRoom.JoloGame_Table_Info getCurrTableInfo(AbstractTable table) {
        return JoloRoom.JoloGame_Table_Info.newBuilder()
                .setTableId(table.getTableId())
                .setPlayerNum(table.getPlayerNum())
                .setBaseBetScore((int) table.getCurrBaseBetScore())
                .setBetMaxScore(table.getBetMaxScore())
                .setGameNum(table.getGameNum())
                .setCurrGameNum(table.getCurrGameNum())
                .setReadyCd(table.getReadyCd())
                .setBetCd(table.getBetCd())
                .setOpenCardCd(table.getOpenCardCd())
                .setIsWatch(table.isWatch() ? 1 : 0)
                .setTotalAlreadyBet(table.getTableAlreadyBetScore())
                .setTableState(table.getTableStateEnum().getValue())
                .build();
    }

    public static List<JoloRoom.JoloRoom_TablePlay_PlayerInfo> getPlayersInTable(AbstractTable table, String currPlayerId) {
        List<JoloRoom.JoloRoom_TablePlay_PlayerInfo> list = new ArrayList<>();
        for (PlayerInfo player : table.getAllPlayers().values()) {
            if (player != null) {
                JoloRoom.JoloRoom_TablePlay_PlayerInfo.Builder tablePlay = JoloRoom.JoloRoom_TablePlay_PlayerInfo.newBuilder()
                        .setUserId(player.getPlayerId())
                        .setNickName(player.getNickName())
                        .setIcourl(player.getIcon())
                        .setPlayScoreStore(player.getPlayScoreStore())
                        .setState(player.getState().getValue())
                        .setSeatNum(player.getSeatNum())
                        .setIsCurrAction(player.getIsCurrActive())
                        .setAlreadyBetScore(player.getWinLoseScore4Hand())
                        .setBetMode(player.getBetMode())
                        .setBetScore(player.getBetScore())
                        .setHeadSculpture(player.getHeadSculpture())
                        .setCardSkin(player.getCardSkin())
                        .setSpecialFunction(player.getSpecialFunction());
                if (player.getIsCurrActive() == 1) {
                    int actionTime = table.getBetCd() - (int) ((System.currentTimeMillis() - player.getStartActionTime()) / 1000);
                    log.debug("当前玩家剩余操作时间：{}", actionTime);
                    tablePlay.setCurrActionSurplusTime(actionTime >= 0 ? actionTime : 0);
                }
                if (player.getHandCards() != null) {
                    tablePlay.addAllHandCards(Ints.asList(player.getHandCards()));
                    //广播里自己的手牌齐全，其他人的前两张为0
                    if (!player.getPlayerId().equals(currPlayerId)) {
                        tablePlay.setHandCards(0, 0)
                                .setHandCards(1, 0);
                    } else {
                        //玩家该跟注多少
                        int maxBetScore = table.getEqualScore().size() == 0 ? 0 : Collections.max(table.getEqualScore()) - (int) player.getWinLoseScore4Hand();
                        tablePlay.setPreviousBetScore(maxBetScore);
                    }
                } else {
                    tablePlay.addAllHandCards(new ArrayList<>());
                }
                list.add(tablePlay.build());
            }
        }
        return list;
    }


//    @Override
//    public void processImpl() {
//        JoloRoom.JoloRoom_ApplyJoinTableAck.Builder ack = JoloRoom.JoloRoom_ApplyJoinTableAck.newBuilder();
//        try {
//
//            log.debug("收到消息-> " + functionId + " reqNum-> " + header.reqNum + " " + req.toString());
//            String userId = req.getUserId();
//            String gameId = req.getGameId();
//            String roomId = req.getRoomId();
//            String tableId = "";
//            int playerNum = req.getPlayerNum();
//
//
//            //首先查看自己有没有在游戏内
//            GameRoomTableSeatRelationModel gameRoomTableSeatRelationModel = StoredObjManager.getStoredObjInMap(
//                    GameRoomTableSeatRelationModel.class,
//                    RedisConst.USER_TABLE_SEAT.getProfix(),
//                    RedisConst.USER_TABLE_SEAT.getField() + userId
//            );
//
//            if (GameMaintentService.OBJ.isDefense()) {
//                log.error("游戏进入维护状态，无法入桌");
//                ctx.writeAndFlush(new JoloRoom_ApplyJoinTableAck_40001(ack.setResult(-1).setResultMsg("游戏进入维护状态,无法入桌").build(), header));
//                return;
//            }
//
//            //用户信息：从缓存获取
//            User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId, User.class);
//            if (user == null) {
//                log.info("缓存中没有user信息");
//                return;
//            }
//            ack.setResult(1).setUserId(userId).setGameId(gameId).setRoomId(roomId);
//
//            //如果参数中没有指定roomId，那么由系统自动分配一个房间(分配规则以入场积分限制为准)
//
//            double currScoreStore = user.getMoney(); //玩家当前积分库存
//            if (gameRoomTableSeatRelationModel == null && !RoomConfigHolder.getInstance().canFindSuuitableRoom(currScoreStore)) {
//                ack.setTableId("")
//                        .setSeatId("")
//                        .setJoinGameSvrId("")
//                        .setBootAmount(0)
//                        .setResult(-1).setResultMsg(ErrorCodeEnum.ROOM_40001_1.getCode());
//                ctx.writeAndFlush(new JoloRoom_ApplyJoinTableAck_40001(ack.build(), header));
//                return;
//            }
//
////            RoomConfigModel roomConfig;
////            CommonConfigModel commonConfig;
////            if (gameRoomTableSeatRelationModel == null) {
////                roomConfig = RoomConfigHolder.getInstance().getRoomConfig(roomId);
////                commonConfig = CommonConfigHolder.getInstance().getCommonConfig(Integer.parseInt(gameId));
////            } else {
////                roomConfig = RoomConfigHolder.getInstance().getRoomConfig(gameRoomTableSeatRelationModel.getRoomId());
////                commonConfig = CommonConfigHolder.getInstance().getCommonConfig(
////                        Integer.parseInt(gameRoomTableSeatRelationModel.getGameId()));
////            }
////            if (null == roomConfig || commonConfig == null) {
////                ack.setTableId("")
////                        .setSeatId("")
////                        .setJoinGameSvrId("")
////                        .setBootAmount(0)
////                        .setResult(-1).setResultMsg(ErrorCodeEnum.ROOM_40001_2.getCode());
////                log.error("can't found suitable Room. userScoreStore->" + currScoreStore);
////                ctx.writeAndFlush(new JoloRoom_ApplyJoinTableAck_40001(ack.build(), header));
////                return;
////            }
////            roomId = roomConfig.getRoomId();
//            AbstractTable table = null;
//            //玩家在在游戏中走断线重连，否则新创建桌子
////            if (gameRoomTableSeatRelationModel != null) {
////                log.info("gameRoomTableSeatRelationModel:" + gameRoomTableSeatRelationModel.toString());
////                if (gameRoomTableSeatRelationModel.getSeat() >= 0) {//在座位上或旁观
////                    ack.setSeatId(gameRoomTableSeatRelationModel.getSeat() + "");
////                    ack.setReconnection(1);
////                }
////                table = TableService.getInstance().addExitTable(gameRoomTableSeatRelationModel.getGameId(),
////                        gameRoomTableSeatRelationModel.getRoomId(), gameRoomTableSeatRelationModel.getTableId());
////            } else {
////
////            }
//            if(!req.hasTableId()) {
//                table = TableService.getInstance().createNewTable(gameId, roomId, true, playerNum);
//                initTable(table);//初始化桌子参数
//                ack.setReconnection(0);
//                tableId = table.getTableId();
//            }else{
//                tableId = req.getTableId();
//                //如果有桌子则加入，否则创建
////                table = TableService.getInstance().addExitTable(gameId, roomId, tableId);
//                table = TableService.getInstance().addNewTable(gameId, roomId, tableId);
//                log.debug("玩家根据房间号加入房间：tableId {}",tableId);
//            }
//
//
//
//            //从redis获取桌子的信息
//            RoomTableRelationModel roomTable = RoomStateService.getInstance().getExistTable(gameId, roomId, tableId);
//
//            String icon = StringUtils.isEmpty(user.getUser_defined_head()) ? user.getIco_url() : user.getUser_defined_head();
//
//            PlayerInfo player = table.getPlayer(userId);
//
//            //修改相关map内容，增加新玩家进入房间状态
//            if (player == null) {
//                player = new PlayerInfo(roomTable, userId, user.getNick_name(), icon, RoleType.getRoleType(user.getChannel_id()),user);
//            }
//
//            PlayerInfo playerInfo = table.joinRoom(player);
//            log.info("-----------------桌子信息：{}",table.toString());
//
////            String res = StoredObjManager.hget(RedisConst.TABLE_USERS.getProfix() + header.gameId + player.getRoomId() + player.getTableId(),
////                    RedisConst.TABLE_USERS.getField() + player.getPlayerId());
////            log.info("res" + res);
////            if (Strings.isNullOrEmpty(res)) {
////                log.debug("桌子的redis缓存找不到此人, userId->" + userId + ", roomId->" + roomId + ",tableId->" + tableId);
////            }
////            if (res.equals("" + PlayerStateEnum.siteDown.getValue())) {
////                log.error("Failed to sit down many times");
////                return;
////            }
//
//            log.info("房间人数：{} -------- {}",table.getAllPlayers().size(),table.hashCode());
//            List<String> list = StoredObjManager.hvals(RedisConst.TABLE_SEAT.getProfix() + gameId + roomId + tableId);
////            log.debug("选座成功useId={}, tableid={},players={}", userId, tableId, TableUtil.toStringInGamePlayers(table));
//            log.debug("选座成功useId={}, tableid={},list={}", userId, tableId, list.toString());
//
//            ack.setTableId(tableId);
//            if (!ack.hasSeatId()) {
//                ack.setSeatId("");
//            }
//            if (gameRoomTableSeatRelationModel != null && !roomId.equals(gameRoomTableSeatRelationModel.getRoomId())) {
//                //TODO 需前端一起配合做跳转提示
//                ack.setResult(1);
//                ack.setResultMsg("返回原来所在桌");
//            }
////            ack.setBootAmount(roomConfig.getAnte());
////            ack.setBetCd(commonConfig.getBetCountDownSec());
////            ack.setGameStartCd(commonConfig.getGameStartCountDownSec());
//            ack.setMinJoinTableScore((int) RoomConfigHolder.getInstance().getMinJoinTableScore());
//
//            String gameSvrId = /*UtilsService.getInstance().getGameSvr(table, gameId)*/Config.GAME_SERID;
//            if (!StringUtil.isNullOrEmpty(gameSvrId)) {
//                ack.setJoinGameSvrId(gameSvrId);//没有时前端提示 "服务器爆满"
//            }
//
//            //通知有玩家入桌
//            NoticeBroadcastMessages.playerJoinTable(table,userId,0);
//            //缓存玩法
//            JedisPoolWrap.getInstance().set(GameConst.CACHE_PLAY_TYPE + userId, gameId, -1);
//            ctx.writeAndFlush(new JoloRoom_ApplyJoinTableAck_40001(ack.build(), header));
//            log.info("40001 builder ack 成功->: {}",ack.toString());
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
//    }
}
