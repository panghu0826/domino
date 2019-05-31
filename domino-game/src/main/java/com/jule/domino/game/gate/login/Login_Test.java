package com.jule.domino.game.gate.login;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.google.common.base.Strings;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.GameRoomTableSeatRelationModel;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.gate.network.GateFunctionFactory;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.service.LogService;
import com.jule.domino.log.service.LogReasons;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;

/**
 * 内网测试登录
 * @author
 * @since 2018/11/26 19:42
 */
@Slf4j
public class Login_Test extends AbstractLogin{

    private static final String _script = "test";

    /**
     * 构造
     */
    public Login_Test() {
        super(_script);
    }

    @Override
    public void process(JoloAuth.JoloCommon_LoginReq req, ChannelHandlerContext ctx, Req.ReqHeader reqHeader)  throws Exception{
        log.info("test用戶登录操作");
        JoloAuth.JoloCommon_LoginAck.Builder builder = JoloAuth.JoloCommon_LoginAck.newBuilder();

        //大厅传入UserId
        String userId = req.getUserId();
        //大厅传入Identity
        String token = req.getToken();

        User user = null;
        if (!Strings.isNullOrEmpty(userId)){
            user = DBUtil.selectByPrimaryKey(userId);
        }

        //用户为空
        if (user == null) {
            long newId = System.nanoTime() / 1000;
            //玩家线程绑定
            bindUser(ctx, reqHeader, String.valueOf(newId), token);

            //创建角色
            user = createTestUser(req, newId);
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
            return ;
        }

        //玩家线程绑定
        bindUser(ctx, reqHeader, user.getId(), token);

        //登录更新玩家信息、重点在货币
        user.setClient_version(req.getClientVersion());
        user.setPackage_name(req.getPackName());
        user.setLast_login(new Date());
        user.setDown_platform(req.getDownPlatform());
        DBUtil.updateByPrimaryKey(user);

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
        log.info("test用戶登录操作,完成");

        reconnect(user.getId(), ctx, reqHeader);
    }

    protected User createTestUser(JoloAuth.JoloCommon_LoginReq req, long newId){
        Date curTime = new Date();

        //创建角色
        User user = new User();
        user.setId(String.valueOf(newId));
        user.setNick_name(getNickName(user.getId()));
        user.setIco_url("txn1_png");
        user.setUser_defined_head("");
        user.setMoney(10000d);

        user.setChannel_id(_script);
        user.setClient_version(req.getClientVersion());
        user.setDevice_num(req.getDeviceNum());
        user.setPlatform(req.getPlatform());
        user.setUser_ip(req.getUserIp());
        user.setRegistration_time(curTime);
        user.setLast_login(curTime);
        user.setLast_offline(curTime);
        user.setAndroid_id(req.getUserId());
        user.setMei_code(req.getDeviceNum());
        user.setDown_platform("");
        user.setPackage_name(req.getPackName());

        //数据持久化
        DBUtil.insert(user);
        log.debug("created userInfo -> {}" , user.toString());

        StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
        log.info("created user -> {} " , newId);

        //发送日志
        LogService.OBJ.sendMoneyLog(user, 0, user.getMoney(), user.getMoney(), LogReasons.CommonLogReason.CREATE_ROLE);
        return user;
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

    /**
     * 来一个名字
     *
     * @param userId
     * @return
     */
    protected String getNickName(String userId) {
        try {
            String prefix = "G-";
            if (StringUtils.isEmpty(userId)) {
                return "";
            }

            int length = userId.length();
            if (userId.length() <= 6) {
                return prefix + userId;
            }

            String name = userId.substring(length - 6, length);
            return prefix + name;
        } catch (Exception e) {
            log.error("create name exception , " + e.getMessage());
            return ("G-" + userId).length() > 8 ? ("G-" + userId).substring(0, 8) : ("G-" + userId);
        }
    }
}
