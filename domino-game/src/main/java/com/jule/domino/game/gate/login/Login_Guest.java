package com.jule.domino.game.gate.login;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.google.common.base.Strings;
import com.jule.core.jedis.StoredObjManager;
import com.jule.core.utils.HttpsUtil;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.GameRoomTableSeatRelationModel;
import com.jule.domino.base.platform.HallAPIService;
import com.jule.domino.base.platform.bean.PlatUserBean;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.UserItemModel;
import com.jule.domino.game.gate.network.GateFunctionFactory;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.service.LogService;
import com.jule.domino.game.service.NoticePlatformSerivce;
import com.jule.domino.log.service.LogReasons;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 通用登录
 *
 * @author
 * @since 2018/11/26 19:42
 */
@Slf4j
public class Login_Guest extends AbstractLogin {

    private static final String _script = "guest";

    /**
     * 构造
     */
    public Login_Guest() {
        super(_script);
    }

    @Override
    public void process(JoloAuth.JoloCommon_LoginReq req, ChannelHandlerContext ctx, Req.ReqHeader reqHeader) throws Exception {
        log.info("guest用戶登录操作");
        JoloAuth.JoloCommon_LoginAck.Builder builder = JoloAuth.JoloCommon_LoginAck.newBuilder();

        //大厅传入UserId
        String userId = req.getUserId();
        //大厅传入Identity
        String token = req.getToken();

        User user = null;
        if (!Strings.isNullOrEmpty(userId)) {
            user = DBUtil.selectByPrimaryKey(userId);
        }

        //用户为空
        if (user == null) {
            long newId = System.nanoTime() / 100000;
            //创建角色
            user = createUser(req, newId);
            //玩家线程绑定
            bindUser(ctx, reqHeader, user.getId(), token);
            setLoginArgs(builder, user, req.getVerify());
//            builder.setIsNew(true).setUserId(user.getId())
//                    .setMoney(user.getMoney())
//                    .setIcoUrl(user.getIco_url())
//                    .setNickName(user.getNick_name())
//                    .setDefaultIco(user.getUser_defined_head())
//                    .setVerify(req.getVerify())
//                    .setChannelId(user.getChannel_id())
//                    .addAllServerinfo(new ArrayList<>())
//                    .setSpecialFunction("vip".equals(user.getDevice_num()) ? 2 : "true".equals(user.getDevice_num()) ? 1 : 0)
//                    .setFriendFunction("true".equals(user.getMei_code()) ? 1 : 0)
//                    .setResult(1);
            GateFunctionFactory.getInstance().getResponse(reqHeader.functionId | 0x08000000, builder.build().toByteArray()).send(ctx, reqHeader);
            log.info("guest用戶登录操作,建号完成");
            log.info("builder ack 成功->: {}", builder.toString());
//            reconnect(user.getId(), ctx, reqHeader);
            return;
        }

        //玩家线程绑定
        bindUser(ctx, reqHeader, user.getId(), token);

        log.info("login userInfo->" + user.toString());
        StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);

        setLoginArgs(builder, user, req.getVerify());
//        builder.setUserId(user.getId())
//                .setMoney(user.getMoney())
//                .setIcoUrl(user.getIco_url())
//                .setDefaultIco(user.getIco_url())
//                .setNickName(user.getNick_name())
//                .setVerify(req.getVerify())
//                .setChannelId(user.getChannel_id())
//                .addAllServerinfo(new ArrayList<>())
//                .setSpecialFunction("vip".equals(user.getDevice_num()) ? 2 : "true".equals(user.getDevice_num()) ? 1 : 0)
//                .setFriendFunction("true".equals(user.getMei_code()) ? 1 : 0)
//                .setResult(1);

        GateFunctionFactory.getInstance().getResponse(reqHeader.functionId | 0x08000000, builder.build().toByteArray()).send(ctx, reqHeader);
        log.info("guest用戶登录操作,完成");
        log.info("builder ack 成功->: {}", builder.toString());

        //暂时不需要断线重连
//        reconnect(user.getId(), ctx, reqHeader);
    }

    public static JoloAuth.JoloCommon_LoginAck.Builder setLoginArgs(JoloAuth.JoloCommon_LoginAck.Builder builder, User user, String verify) {
        return builder.setUserId(user.getId())
                .setMoney(user.getMoney())
                .setIcoUrl(user.getIco_url())
                .setDefaultIco(user.getIco_url())
                .setNickName(user.getNick_name())
                .setVerify(verify)
                .setChannelId(user.getChannel_id())
                .addAllServerinfo(new ArrayList<>())
                .setSpecialFunction("vip".equals(user.getDevice_num()) ? 2 : "true".equals(user.getDevice_num()) ? 1 : 0)
                .setFriendFunction("true".equals(user.getMei_code()) ? 1 : 0)
                .setNumberOfGames(user.getTotal_game_num())
                .addAllHaveItem(setItemArgs(user.getId()))
                .setResult(1);
    }

    private static List<JoloAuth.JoloCommon_HaveItem> setItemArgs(String userId) {
        List<UserItemModel> list = DBUtil.selectByUserIdItem(userId);
        List<JoloAuth.JoloCommon_HaveItem> array = new ArrayList<>();
        try {
            for (UserItemModel uim : list) {
                long currTime = System.currentTimeMillis();
                long itemTime = uim.getDueTime().getTime();
//                long permanentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2016-02-19 00:00:00").getTime();
//                boolean flags = (permanentTime == itemTime);
                if (currTime > itemTime) {
                    int in = DBUtil.deleteByItemId(uim.getItemId());
                    log.info("当前时间：{}，道具到期时间：{}，删除结果：{}",
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currTime),
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(itemTime),
                            (in == 1 ? "成功" : "失败"));
                    continue;
                }
                array.add(JoloAuth.JoloCommon_HaveItem.newBuilder()
                        .setItemId(Integer.parseInt(uim.getItemId()))
                        .setDueTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(uim.getDueTime())).build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return array;
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

    protected User createUser(JoloAuth.JoloCommon_LoginReq req, long newId) {
        Date curTime = new Date();
        //创建角色
        User user = new User();
        user.setNick_name(getNickName(String.valueOf(newId)));
//        user.setNick_name(req.getToken());
//        user.setNick_name("最是离人心");
        user.setIco_url("");
        user.setUser_defined_head("");
        user.setMoney(Config.GUEST_INIT_MONEY);
        user.setChannel_id(req.getChannelId());
        user.setSub_channel_id(newId + "");
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
//        log.debug("玩家现在的昵称：{}",user.getNick_name());
        //查询玩家（自增id）
        user = DBUtil.selectBySubChannelId(newId + "");
        log.debug("created userInfo -> {}", user.getNick_name());
        StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
        //发送日志
        LogService.OBJ.sendMoneyLog(user, 0, user.getMoney(), user.getMoney(), LogReasons.CommonLogReason.CREATE_ROLE);
        return user;
    }
}
