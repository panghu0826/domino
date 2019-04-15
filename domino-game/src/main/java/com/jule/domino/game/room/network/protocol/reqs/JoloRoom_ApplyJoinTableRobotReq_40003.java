package com.jule.domino.game.room.network.protocol.reqs;

import JoloProtobuf.RoomSvr.JoloRoom;
import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.enums.RoleType;
import com.jule.domino.base.model.RoomTableRelationModel;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.RoomConfigModel;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.room.network.protocol.acks.JoloRoom_ApplyJoinTableAck_40001;
import com.jule.domino.game.room.service.TableService;
import com.jule.domino.game.room.service.UtilsService;
import com.jule.domino.game.service.holder.RoomConfigHolder;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoloRoom_ApplyJoinTableRobotReq_40003 extends ClientReq {
    private final static Logger logger = LoggerFactory.getLogger(JoloRoom_ApplyJoinTableRobotReq_40003.class);

    private JoloRoom.JoloRoom_ApplyJoinTableRobotReq req;

    public JoloRoom_ApplyJoinTableRobotReq_40003(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf byteBuf) throws Exception {
        byte[] playLoad = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(playLoad);
        req = JoloRoom.JoloRoom_ApplyJoinTableRobotReq.parseFrom(playLoad);
        RabbitMqSender.me.producer(functionId,req.toString());
    }

    @Override
    public void processImpl() throws Exception {
        logger.debug("收到消息-> " + functionId + " reqNum-> " + header.reqNum + " " + req.toString());
        try {
            String userId = req.getUserId();
            String gameId = req.getGameId();
            String roomId = req.getRoomId();
            String tableId = req.getTableId();
            //通过roomId和tableId 查找对就的桌子
            AbstractTable table = TableService.getInstance().getTable(header.gameId+"",roomId, gameId);
            //如果内存没有 去缓存看看
            if (table == null) {
                RoomTableRelationModel ret = null;
                ret = StoredObjManager.getStoredObjInMap(RoomTableRelationModel.class,
                        RedisConst.TABLE_INSTANCE.getProfix() + gameId + roomId,
                        RedisConst.TABLE_INSTANCE.getField() + tableId);
                if (ret == null) {
                    logger.error("JoloRoom_ApplyJoinTableRobotReq_40003 找不到指定的桌子。room:" + roomId + ",tableId:" + tableId);
                    return;
                }
                table = TableService.getInstance().addExitTable(""+header.gameId,ret.getRoomId(), ret.getTableId());
            }
            //用户信息：现在从数据库获取，以后可优化成从缓存获取
            User user = DBUtil.selectByPrimaryKey(userId);
            if (user == null) {
                logger.error("can't found user uiserId->" + userId);
                return;
            }
            if (!user.getChannel_id().equals(RoleType.ROBOT.getTypeName())) {
                logger.error("this account is not robot，userId " + userId);
                return;
            }
            JoloRoom.JoloRoom_ApplyJoinTableAck.Builder ack = JoloRoom.JoloRoom_ApplyJoinTableAck.newBuilder();
            //初始化ack信息
            ack.setUserId(userId);
            ack.setGameId(gameId);
            ack.setRoomId(roomId);
            ack.setTableId(tableId);
            //断线重连如果有座位信息需要在这里设置上
            //从缓存里查找信息ID
            ack.setSeatId("");
            ack.setResult(1);
            //如果参数中没有指定roomId，那么由系统自动分配一个房间(分配规则以入场积分限制为准)
            //暂时不用做任何判断 未来根据需求改
            //创建player对象
            PlayerInfo player = new PlayerInfo(roomId, tableId, user.getId(), user.getNick_name(), user.getIco_url());
            //将玩家加入到table信息中（完成玩家的入桌状态）
            table.joinRoom(player);
            RoomConfigModel roomConfig = RoomConfigHolder.getInstance().getRoomConfig(roomId);
            if (roomConfig == null) {
                ack.setResult(0);
                ack.setResultMsg(ErrorCodeEnum.ROOM_40001_1.getCode());
                ctx.writeAndFlush(new JoloRoom_ApplyJoinTableAck_40001(ack.build(), header));
                return;
            }

            ack.setBootAmount(roomConfig.getAnte());
            String gameSvrId = UtilsService.getInstance().getGameSvr(table,gameId);
            if (!StringUtil.isNullOrEmpty(gameSvrId)) {
                ack.setJoinGameSvrId(gameSvrId);//没有时前端提示 "服务器爆满"
            }

            //缓存玩法
            JedisPoolWrap.getInstance().set(GameConst.CACHE_PLAY_TYPE + userId, gameId, -1);
            ctx.writeAndFlush(new JoloRoom_ApplyJoinTableAck_40001(ack.build(), header));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
