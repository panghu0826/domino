package com.jule.domino.game.gate.login;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.google.common.base.Strings;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.GameRoomTableSeatRelationModel;
import com.jule.domino.base.platform.HallAPIService;
import com.jule.domino.base.platform.bean.PlatUserBean;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.gate.network.GateFunctionFactory;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.service.NoticePlatformSerivce;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;

/**
 * 通用登录
 * @author
 * @since 2018/11/26 19:42
 */
@Slf4j
public class Login_Guest extends AbstractLogin{

    private static final String _script = "guest";

    /**
     * 构造
     */
    public Login_Guest() {
        super(_script);
    }

    @Override
    public void process(JoloAuth.JoloCommon_LoginReq req, ChannelHandlerContext ctx, Req.ReqHeader reqHeader)  throws Exception{
        log.info("guest用戶登录操作");
        JoloAuth.JoloCommon_LoginAck.Builder builder = JoloAuth.JoloCommon_LoginAck.newBuilder();

        //大厅传入UserId
        String userId = req.getUserId();
        //大厅传入Identity
        String token = req.getToken();

        User user = null;
        if (!Strings.isNullOrEmpty(userId)){
            user = DBUtil.selectByOpenId(userId);
        }

        //用户为空
        if (user == null) {
            long newId = System.nanoTime() / 1000;
            //玩家线程绑定
            bindUser(ctx, reqHeader, String.valueOf(newId), token);
            //创建角色
            user = createUser(req,newId);
            builder.setIsNew(true).setUserId(user.getId())
                    .setMoney(user.getMoney())
                    .setIcoUrl(user.getIco_url())
                    .setNickName(user.getNick_name())
                    .setDefaultIco(user.getUser_defined_head())
                    .setVerify(req.getVerify())
                    .setChannelId(user.getChannel_id())
                    .addAllServerinfo(new ArrayList<>())
                    .setResult(1);
            GateFunctionFactory.getInstance().getResponse(reqHeader.functionId  | 0x08000000, builder.build().toByteArray()).send(ctx, reqHeader);
            log.info("guest用戶登录操作,建号完成");
            log.info("builder ack 成功->: {}",builder.toString());
            return ;
        }

        //玩家线程绑定
        bindUser(ctx, reqHeader, user.getId(), token);

//        //登录更新玩家信息、重点在货币
//        user.setMoney(userBean.getGold() * NoticePlatformSerivce.EXCHANGE_MIN);
//        user.setClient_version(req.getClientVersion());
//        user.setPackage_name(req.getPackName());
//        user.setLast_login(new Date());
//        user.setDown_platform(req.getDownPlatform());
//        user.setChannel_id(userBean.getChannel_id());
//        user.setSub_channel_id(userBean.getSub_channel_id());
//        user.setUser_defined_head(userBean.getIcon());
//        user.setIco_url(userBean.getIcon());
//        DBUtil.updateByPrimaryKey(user);

        log.info("login userInfo->" + user.toString());
        StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);

        builder.setUserId(user.getId())
                .setMoney(user.getMoney())
                .setIcoUrl(user.getIco_url())
                .setDefaultIco(user.getUser_defined_head() == null ? "" : user.getUser_defined_head())
                .setNickName(user.getNick_name())
                .setVerify(req.getVerify())
                .setChannelId(user.getChannel_id())
                .addAllServerinfo(new ArrayList<>())
                .setResult(1);

        GateFunctionFactory.getInstance().getResponse(reqHeader.functionId  | 0x08000000, builder.build().toByteArray()).send(ctx, reqHeader);
        log.info("guest用戶登录操作,完成");
        log.info("builder ack 成功->: {}",builder.toString());

        //暂时不需要断线重连
//        reconnect(user.getId(), ctx, reqHeader);
    }

    private void reconnect(String userId, ChannelHandlerContext ctx, Req.ReqHeader reqHeader){
        //首先查看自己有没有在游戏内
        GameRoomTableSeatRelationModel gameRoomTableSeatRelationModel = StoredObjManager.getStoredObjInMap(
                GameRoomTableSeatRelationModel.class,
                RedisConst.USER_TABLE_SEAT.getProfix(),
                RedisConst.USER_TABLE_SEAT.getField() + userId);

        JoloAuth.JoloCommon_InTableAck.Builder inTableAck = JoloAuth.JoloCommon_InTableAck.newBuilder();
        if (gameRoomTableSeatRelationModel != null) {
            String gameId = gameRoomTableSeatRelationModel.getGameId();
            String roomId = gameRoomTableSeatRelationModel.getRoomId();
            String tableId = gameRoomTableSeatRelationModel.getTableId();
            int seatNum = gameRoomTableSeatRelationModel.getSeat();

            log.debug("玩家user={}在游戏内,可以触发重连,game={},room={},table={}", userId, gameId, roomId, tableId);
            inTableAck.setResult(1);
            inTableAck.setUserId(userId);
            inTableAck.setGameId(gameId);
            inTableAck.setRoomId(roomId);
            inTableAck.setTableId(tableId);
            inTableAck.setSeatNum(seatNum);
        } else {
            inTableAck.setResult(0);
        }

        GateFunctionFactory.getInstance().getResponse(600011 | 0x08000000, inTableAck.build().toByteArray()).send(ctx, reqHeader);
    }
}
