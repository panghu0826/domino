package com.jule.domino.game.room.network.protocol.reqs;

import JoloProtobuf.RoomSvr.JoloRoom;
import com.google.common.base.Strings;
import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.game.dao.bean.CommonConfigModel;
import com.jule.domino.game.dao.bean.RoomConfigModel;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.room.network.protocol.acks.JoloRoom_ApplyChangeTableAck_40002;
import com.jule.domino.game.room.service.TableService;
import com.jule.domino.game.room.service.UtilsService;
import com.jule.domino.game.service.holder.CommonConfigHolder;
import com.jule.domino.game.service.holder.RoomConfigHolder;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class JoloRoom_ApplyChangeTableReq_40002 extends ClientReq {
    private final static Logger logger = LoggerFactory.getLogger(JoloRoom_ApplyChangeTableReq_40002.class);
    public JoloRoom.JoloRoom_ApplyChangeTableReq req;

    public JoloRoom_ApplyChangeTableReq_40002(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf byteBuf) throws Exception {
        byte[] playLoad = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(playLoad);
        req = JoloRoom.JoloRoom_ApplyChangeTableReq.parseFrom(playLoad);
        RabbitMqSender.me.producer(functionId,req.toString());
    }

    @Override
    public void processImpl() throws Exception {
        JoloRoom.JoloRoom_ApplyChangeTableAck.Builder ack = JoloRoom.JoloRoom_ApplyChangeTableAck.newBuilder();
        try {
            logger.debug("收到消息-> " + functionId + " reqNum-> " + header.reqNum + " " + req.toString());
            String userId = req.getUserId();
            String gameId = req.getGameId();
            String roomId = req.getRoomId();
            String tableId = req.getTableId();
            ack.setUserId(userId).setGameId(gameId).setRoomId(roomId).setTableId(tableId).setSeatId("");

            if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(gameId) ||
                    StringUtils.isEmpty(roomId) ||
                    StringUtils.isEmpty(tableId)) {
                logger.info("参数不能为空");
            }
            //首先查看自己有没有在游戏内
            String res = StoredObjManager.hget(RedisConst.TABLE_USERS.getProfix() + header.gameId + roomId + tableId,
                    RedisConst.TABLE_USERS.getField() + userId);
            logger.info("res" + res);
            if (Strings.isNullOrEmpty(res)) {
                logger.info("This player is not in the table.");
                ack.setResult(0).setResultMsg(ErrorCodeEnum.ROOM_40002_1.getCode());
                ctx.writeAndFlush(new JoloRoom_ApplyChangeTableAck_40002(ack.build(), header));
                return;
            }
            PlayerInfo player = null;
            AbstractTable tableInfo = TableService.getInstance().getTable(gameId, roomId, tableId);
            if (tableInfo != null) {
                player = tableInfo.getPlayer(userId);
            }

            if (player == null) {
                logger.error("This player is null. gameId:{},roomId:{},tableId:{},userId:{}", header.gameId, roomId, tableId, userId);
                ack.setResult(-1).setResultMsg(ErrorCodeEnum.GAME_50050_2.getCode());
                ctx.writeAndFlush(new JoloRoom_ApplyChangeTableAck_40002(ack.build(), header));
                return;
            }


            User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId,
                    User.class);
            if (user == null) {
                logger.info("缓存中没有user信息");
                return;
            }
            StoredObjManager.hset(RedisConst.CHANGE_TABLE_STAT.getProfix(),
                    RedisConst.CHANGE_TABLE_STAT.getField() + userId, "1");


            ack.setResult(1);
            //如果参数中没有指定roomId，那么由系统自动分配一个房间(分配规则以入场积分限制为准)

            double currScoreStore = user.getMoney(); //玩家当前积分库存
            logger.info("当前积分：" + user.getMoney());
            if (!RoomConfigHolder.getInstance().canFindSuuitableRoom(currScoreStore)) {
                ack.setUserId(req.getUserId())
                        .setGameId(req.getGameId())
                        .setJoinGameSvrId("")
                        .setBootAmount(0)
                        .setChaalLimit(0)
                        .setMaxBlinds(0)
                        .setPotLimit(0)
                        .setResult(-1).setResultMsg(ErrorCodeEnum.ROOM_40001_1.getCode());
                ctx.writeAndFlush(new JoloRoom_ApplyChangeTableAck_40002(ack.build(), header));
                return;
            }

            RoomConfigModel roomConfig;
            CommonConfigModel commonConfig;
            roomConfig = RoomConfigHolder.getInstance().getRoomConfigByScore(currScoreStore);
            commonConfig = CommonConfigHolder.getInstance().getCommonConfig(Integer.parseInt(gameId));
            if (null == roomConfig || commonConfig == null) {
                logger.debug("roomConfig--------------------" + roomConfig);
                ack.setUserId(req.getUserId())
                        .setGameId(req.getGameId())
                        .setRoomId("")
                        .setTableId("")
                        .setSeatId("")
                        .setJoinGameSvrId("")
                        .setBootAmount(0)
                        .setChaalLimit(0)
                        .setMaxBlinds(0)
                        .setPotLimit(0)
                        .setResult(-1).setResultMsg("can't found suitable Room. userScoreStore->" + currScoreStore);
                ctx.writeAndFlush(new JoloRoom_ApplyChangeTableAck_40002(ack.build(), header));
                return;
            }
            roomId = roomConfig.getRoomId();
            AbstractTable table = TableService.getInstance().getRandomTable(roomId, gameId, tableId);

            //上桌内的玩家信息删除 modify 2018-07-27
            StoredObjManager.hdel(RedisConst.TABLE_USERS.getProfix() + gameId + roomId + tableId,
                    RedisConst.TABLE_USERS.getField() + userId);

            tableId = table.getTableId();

            //创建player对象

            //将玩家加入到table信息中（完成玩家的入桌状态）
            table.joinRoom(player);
            //初始化ack信息
            ack.setUserId(userId);
            ack.setGameId(gameId);
            ack.setRoomId(roomId);
            ack.setTableId(tableId);
            if (!ack.hasSeatId()) {
                ack.setSeatId("");
            }
            ack.setBootAmount(roomConfig.getAnte());
            ack.setChaalLimit(0);
            ack.setMaxBlinds(0);
            ack.setPotLimit(0);
            ack.setAllowSideshowCd(0);
            ack.setBetCd(commonConfig.getBetCountDownSec());
            ack.setGameStartCd(commonConfig.getGameStartCountDownSec());
            ack.setMinJoinTableScore((int) RoomConfigHolder.getInstance().getMinJoinTableScore());

            //synchronized (this) {
            String gameSvrId = UtilsService.getInstance().getGameSvr(table, gameId);
            if (!StringUtil.isNullOrEmpty(gameSvrId)) {
                ack.setJoinGameSvrId(gameSvrId);//没有时前端提示 "服务器爆满"
            }
            //缓存玩法
            JedisPoolWrap.getInstance().set(GameConst.CACHE_PLAY_TYPE + userId, gameId, -1);

            ctx.writeAndFlush(new JoloRoom_ApplyChangeTableAck_40002(ack.build(), header));

            logger.info("ApplyJoinTableReq is finished");
            //}
        } catch (Exception e) {
            ack.setResult(0);
            ctx.writeAndFlush(new JoloRoom_ApplyChangeTableAck_40002(ack.build(), header));
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoloRoom_ApplyChangeTableReq_40002 req_40002 = (JoloRoom_ApplyChangeTableReq_40002) o;
        return Objects.equals(req, req_40002.req);
    }

    @Override
    public int hashCode() {

        return Objects.hash(req);
    }
}
