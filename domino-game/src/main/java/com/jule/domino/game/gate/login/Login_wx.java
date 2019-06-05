package com.jule.domino.game.gate.login;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.google.common.base.Strings;
import com.jule.core.jedis.StoredObjManager;
import com.jule.core.utils.HttpsUtil;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.GameRoomTableSeatRelationModel;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.gate.network.GateFunctionFactory;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.service.LogService;
import com.jule.domino.log.service.LogReasons;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * 通用登录
 *
 * @author
 * @since 2018/11/26 19:42
 */
@Slf4j
public class Login_wx extends AbstractLogin {

    private static final String _script = "wx";

    private static final String WX_EMPOWER = "https://api.weixin.qq.com/sns/oauth2/access_token" +
            "?appid=wxaba17fce182f8efe" +
            "&secret=80508ba7e8a78b5932bb4bb856216c2b" +
            "&grant_type=authorization_code" +
            "&code=";

    private static final String WX_USERINFO = "https://api.weixin.qq.com/sns/userinfo";

    /**
     * 构造
     */
    public Login_wx() {
        super(_script);
    }

    @Override
    public void process(JoloAuth.JoloCommon_LoginReq req, ChannelHandlerContext ctx, Req.ReqHeader reqHeader) throws Exception {
        log.info("wx用戶登录操作");
        JoloAuth.JoloCommon_LoginAck.Builder builder = JoloAuth.JoloCommon_LoginAck.newBuilder();

        //大厅传入UserId
        String userId = req.getUserId();
        //大厅传入Identity
        String token = req.getToken();

        String url = WX_EMPOWER + req.getVerify();
        log.debug("发送的https请求地址：{}", url);
        String str = HttpsUtil.doGet(url, true);
        log.debug("接收到的的https请求地址：{}", str);
        JSONObject json = JSONObject.fromObject(str);
        String url2 = WX_USERINFO + "?access_token=" + json.getString("access_token") + "&openid=" + json.getString("openid") + "&lang=zh_CN";
        log.debug("发送的https请求地址：{}", url2);
        String str2 = HttpsUtil.doGet(url2, true);
        log.debug("接收到的的https请求地址：{}", str2);
        JSONObject user_wx = JSONObject.fromObject(str2);
        String nikeName = user_wx.getString("nickname");
        String icoUrl = user_wx.getString("headimgurl");
        String unionid = user_wx.getString("openid");

        User user = null;
        if (!Strings.isNullOrEmpty(unionid)) {
            user = DBUtil.selectBySubChannelId(unionid);
            if(user != null) {
                //玩家的昵称 or 头像如果有变化，则更新user表玩家的数据
                if (!nikeName.equals(user.getNick_name()) || !icoUrl.equals(user.getIco_url())) {
                    user.setNick_name(nikeName);
                    user.setIco_url(icoUrl);
                    DBUtil.updateByPrimaryKey(user);
                    user = DBUtil.selectBySubChannelId(unionid);
                }
            }
        }

        //用户为空
        if (user == null) {
            long newId = System.nanoTime() / 100000;
            //创建角色
            user = createUser(req, nikeName, icoUrl, unionid);
            //玩家线程绑定
            bindUser(ctx, reqHeader, user.getId(), token);
            Login_Guest.setLoginArgs(builder,user,req.getVerify());
//            builder.setIsNew(true).setUserId(user.getId())
//                    .setMoney(user.getMoney())
//                    .setIcoUrl(user.getIco_url())
//                    .setNickName(user.getNick_name())
//                    .setDefaultIco(user.getUser_defined_head())
//                    .setVerify(req.getVerify())
//                    .setChannelId(user.getChannel_id())
//                    .addAllServerinfo(new ArrayList<>())
//                    .setFriendFunction("true".equals(user.getMei_code()) ? 1 : 0)
//                    .setSpecialFunction("vip".equals(user.getDevice_num()) ? 2 : "true".equals(user.getDevice_num()) ? 1 : 0)
//                    .setNumberOfGames(user.getTotal_game_num())
//                    .setResult(1);
            GateFunctionFactory.getInstance().getResponse(reqHeader.functionId | 0x08000000, builder.build().toByteArray()).send(ctx, reqHeader);
            log.info("wx用戶登录操作,建号完成");
            log.info("builder ack 成功->: {}", builder.toString());
//            reconnect(user.getId(), ctx, reqHeader);
            return;
        }

        //玩家线程绑定
        bindUser(ctx, reqHeader, user.getId(), token);

        log.info("login userInfo->" + user.toString());
        StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);

        Login_Guest.setLoginArgs(builder,user,req.getVerify());
//        builder.setUserId(user.getId())
//                .setMoney(user.getMoney())
//                .setIcoUrl(user.getIco_url())
//                .setDefaultIco(user.getUser_defined_head() == null ? "" : user.getUser_defined_head())
//                .setNickName(user.getNick_name())
//                .setVerify(req.getVerify())
//                .setChannelId(user.getChannel_id())
//                .addAllServerinfo(new ArrayList<>())
//                .setFriendFunction("true".equals(user.getMei_code()) ? 1 : 0)
//                .setSpecialFunction("vip".equals(user.getDevice_num()) ? 2 : "true".equals(user.getDevice_num()) ? 1 : 0)
//                .setNumberOfGames(user.getTotal_game_num())
//                .setResult(1);

        GateFunctionFactory.getInstance().getResponse(reqHeader.functionId | 0x08000000, builder.build().toByteArray()).send(ctx, reqHeader);
        log.info("wx用戶登录操作,完成");
        log.info("builder ack 成功->: {}", builder.toString());

        //暂时不需要断线重连
//        reconnect(user.getId(), ctx, reqHeader);
    }

    private void reconnect(String userId, ChannelHandlerContext ctx, Req.ReqHeader reqHeader) {
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

    private User createUser(JoloAuth.JoloCommon_LoginReq req, String nickName, String icoUrl, String unionid) {
        Date curTime = new Date();
        //创建角色
        User user = new User();
        user.setNick_name(nickName);
        user.setIco_url(icoUrl);
        user.setUser_defined_head("");
        user.setMoney(Config.GUEST_INIT_MONEY);
        user.setChannel_id(req.getChannelId());
        user.setSub_channel_id(unionid);
        user.setClient_version(req.getClientVersion());
        user.setDevice_num(req.getDeviceNum());
        user.setPlatform(req.getPlatform());
        user.setUser_ip(req.getUserIp());
        user.setRegistration_time(curTime);
        user.setLast_login(curTime);
        user.setLast_offline(curTime);
        user.setAndroid_id(req.getUserId());
        user.setMei_code(req.getDeviceNum());
        user.setDown_platform(req.getDownPlatform());
        user.setPackage_name(req.getPackName());

        //数据持久化
        DBUtil.insert(user);

        //查询玩家（自增id）
        user = DBUtil.selectBySubChannelId(unionid);
        log.debug("created userInfo -> {}", user.toString());
        StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
        //发送日志
        LogService.OBJ.sendMoneyLog(user, 0, user.getMoney(), user.getMoney(), LogReasons.CommonLogReason.CREATE_ROLE);
        return user;
    }
}
