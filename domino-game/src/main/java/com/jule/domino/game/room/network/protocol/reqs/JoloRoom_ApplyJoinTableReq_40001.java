package com.jule.domino.game.room.network.protocol.reqs;

import JoloProtobuf.RoomSvr.JoloRoom;
import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.GameRoomTableSeatRelationModel;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.dao.bean.CommonConfigModel;
import com.jule.domino.game.dao.bean.RoomConfigModel;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.play.TableUtil;
import com.jule.domino.game.room.network.protocol.acks.JoloRoom_ApplyJoinTableAck_40001;
import com.jule.domino.game.room.service.TableService;
import com.jule.domino.game.room.service.UtilsService;
import com.jule.domino.game.service.GameMaintentService;
import com.jule.domino.game.service.holder.CommonConfigHolder;
import com.jule.domino.game.service.holder.RoomConfigHolder;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
        RabbitMqSender.me.producer(functionId,req.toString());
    }

    @Override
    public void processImpl() {
        JoloRoom.JoloRoom_ApplyJoinTableAck.Builder ack = JoloRoom.JoloRoom_ApplyJoinTableAck.newBuilder();
        try {
            //初始化默认返回值
            this.initDefaultReturnValue(ack);

            log.debug("收到消息-> " + functionId + " reqNum-> " + header.reqNum + " " + req.toString());
            String userId = this.userId;
            String gameId = req.getGameId();
            String roomId = req.getRoomId();
            String tableId = "";

            //首先查看自己有没有在游戏内
            GameRoomTableSeatRelationModel gameRoomTableSeatRelationModel = StoredObjManager.getStoredObjInMap(
                    GameRoomTableSeatRelationModel.class,
                    RedisConst.USER_TABLE_SEAT.getProfix(),
                    RedisConst.USER_TABLE_SEAT.getField() + userId
            );

            if (GameMaintentService.OBJ.isDefense()){
                log.error("游戏进入维护状态，无法入桌");
                ctx.writeAndFlush(new JoloRoom_ApplyJoinTableAck_40001(ack.setResult(-1).setResultMsg("游戏进入维护状态,无法入桌").build(), header));
                return;
            }

            //用户信息：从缓存获取
            User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId, User.class);
            if (user == null) {
                log.info("缓存中没有user信息");
                return;
            }
            ack.setResult(1).setUserId(userId).setGameId(gameId).setRoomId(roomId);

            //如果参数中没有指定roomId，那么由系统自动分配一个房间(分配规则以入场积分限制为准)

            double currScoreStore = user.getMoney(); //玩家当前积分库存
            if (gameRoomTableSeatRelationModel == null && !RoomConfigHolder.getInstance().canFindSuuitableRoom(currScoreStore)) {
                ack.setTableId("")
                        .setSeatId("")
                        .setJoinGameSvrId("")
                        .setBootAmount(0)
                        .setResult(-1).setResultMsg(ErrorCodeEnum.ROOM_40001_1.getCode());
                ctx.writeAndFlush(new JoloRoom_ApplyJoinTableAck_40001(ack.build(), header));
                return;
            }

            RoomConfigModel roomConfig;
            CommonConfigModel commonConfig;
            if (gameRoomTableSeatRelationModel == null) {
                roomConfig = RoomConfigHolder.getInstance().getRoomConfig(roomId);
                commonConfig = CommonConfigHolder.getInstance().getCommonConfig(Integer.parseInt(gameId));
            } else {
                roomConfig = RoomConfigHolder.getInstance().getRoomConfig(gameRoomTableSeatRelationModel.getRoomId());
                commonConfig = CommonConfigHolder.getInstance().getCommonConfig(
                        Integer.parseInt(gameRoomTableSeatRelationModel.getGameId()));
            }
            if (null == roomConfig || commonConfig == null) {
                ack.setTableId("")
                        .setSeatId("")
                        .setJoinGameSvrId("")
                        .setBootAmount(0)
                        .setResult(-1).setResultMsg(ErrorCodeEnum.ROOM_40001_2.getCode());
                log.error("can't found suitable Room. userScoreStore->" + currScoreStore);
                ctx.writeAndFlush(new JoloRoom_ApplyJoinTableAck_40001(ack.build(), header));
                return;
            }
            roomId = roomConfig.getRoomId();
            AbstractTable table = null;
            if (gameRoomTableSeatRelationModel != null) {
                log.info("gameRoomTableSeatRelationModel:" + gameRoomTableSeatRelationModel.toString());
                if ( gameRoomTableSeatRelationModel.getSeat() >= 0) {//在座位上或旁观
                    ack.setSeatId(gameRoomTableSeatRelationModel.getSeat() + "");
                    ack.setReconnection(1);
                }
                table = TableService.getInstance().addExitTable(gameRoomTableSeatRelationModel.getGameId(),
                        gameRoomTableSeatRelationModel.getRoomId(), gameRoomTableSeatRelationModel.getTableId());
            } else {
                table = TableService.getInstance().getRandomTable(roomId, gameId, "");
                ack.setReconnection(0);
            }

            tableId = table.getTableId();

            List<String> list = StoredObjManager.hvals(RedisConst.TABLE_SEAT.getProfix() + gameId + roomId + tableId);
            log.debug("选座成功useId={}, tableid={},players={}",userId, tableId, TableUtil.toStringInGamePlayers(table));
            log.debug("选座成功useId={}, tableid={},list={}",userId, tableId, list.toString());


            if (gameRoomTableSeatRelationModel == null) {
                //创建player对象
                //PlayerInfo player = new PlayerInfo(roomId, tableId, user.getId(), user.getNick_name(), user.getIco_url());
                //将玩家加入到table信息中（完成玩家的入桌状态）
                //table.joinRoom(player);
            }
            ack.setTableId(tableId);
            if (!ack.hasSeatId()) {
                ack.setSeatId("");
            }
            if (gameRoomTableSeatRelationModel != null && !roomId.equals(gameRoomTableSeatRelationModel.getRoomId())) {
                //TODO 需前端一起配合做跳转提示
                ack.setResult(1);
                ack.setResultMsg("返回原来所在桌");
            }
            ack.setBootAmount(roomConfig.getAnte());
            ack.setBetCd(commonConfig.getBetCountDownSec());
            ack.setGameStartCd(commonConfig.getGameStartCountDownSec());
            ack.setMinJoinTableScore((int) RoomConfigHolder.getInstance().getMinJoinTableScore());

            String gameSvrId = /*UtilsService.getInstance().getGameSvr(table, gameId)*/Config.GAME_SERID;
            if (!StringUtil.isNullOrEmpty(gameSvrId)) {
                ack.setJoinGameSvrId(gameSvrId);//没有时前端提示 "服务器爆满"
            }

            //缓存玩法
            JedisPoolWrap.getInstance().set(GameConst.CACHE_PLAY_TYPE + userId, gameId, -1);
            ctx.writeAndFlush(new JoloRoom_ApplyJoinTableAck_40001(ack.build(), header));
            log.info("ApplyJoinTableReq is finished, player = {}",userId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 初始化默认返回值
     * @param ack
     */
    private void initDefaultReturnValue(JoloRoom.JoloRoom_ApplyJoinTableAck.Builder ack){
        ack.setUserId(req.getUserId());
        ack.setGameId(req.getGameId());
        ack.setRoomId(req.getRoomId());
        ack.setTableId("");
        ack.setSeatId("");
        ack.setResult(1);
        ack.setJoinGameSvrId("");
        ack.setBootAmount(0);
        ack.setBetCd(0);
        ack.setGameStartCd(0);
        ack.setMinJoinTableScore(0);
        ack.setReconnection(0);
    }
}
