package com.jule.domino.auth.loginprocess.impl;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.MessageLite;
import com.jule.core.common.log.LoggerUtils;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.auth.config.Config;
import com.jule.domino.auth.dao.DBUtil;
import com.jule.domino.auth.loginprocess.ChannelId;
import com.jule.domino.auth.loginprocess.ILoginProcess;
import com.jule.domino.auth.service.LogService;
import com.jule.domino.auth.utils.CheckUtils;
import com.jule.domino.auth.utils.HuaweiLoginVerify;
import com.jule.domino.auth.utils.HuaweiParams;
import com.jule.domino.base.bean.ItemConfigBean;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.log.service.LogReasons;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * 华为渠道登录
 *
 * @author
 *
 * @since 2018年7月23日11:20:59
 *
 */
@ChannelId(name = "huawei")
public class HuaweiLogin implements ILoginProcess {

    private static final Logger logger = LoggerFactory.getLogger(HuaweiLogin.class);

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    @Override
    public MessageLite process(JoloAuth.JoloCommon_LoginReq req) throws Exception {
        logger.info("华为渠道登录信息，msg={}",req.toString());
        HuaweiParams params = gson.fromJson(req.getToken(), HuaweiParams.class);
        if (params == null){
            logger.error("华为登录参数解析失败");
            return JoloAuth.JoloCommon_LoginAck.newBuilder()
                    .setUserId("")
                    .setMoney(0)
                    .setIcoUrl("")
                    .setDefaultIco("")
                    .setNickName("")
                    .setResult(0)
                    .setVerify("")
                    .setChannelId("")
                    .setResultMsg(ErrorCodeEnum.GATE_600001_6.getCode()).build();
        }

        if (!HuaweiLoginVerify.callGameService(makMap(params), Config.HUAWEI_SIGN_PRIVATE_KEY)){
            logger.error("华为登录验证失败");
            return JoloAuth.JoloCommon_LoginAck.newBuilder()
                    .setUserId("")
                    .setMoney(0)
                    .setIcoUrl("")
                    .setDefaultIco("")
                    .setNickName("")
                    .setResult(0)
                    .setVerify("")
                    .setChannelId("")
                    .setResultMsg(ErrorCodeEnum.GATE_600001_7.getCode()).build();
        }
        logger.info("华为登录验证成功");

        String userId = params.getPlayerId();
        User user = null;
        if (!Strings.isNullOrEmpty(userId)) {
            user = DBUtil.selectUserByOpenId(userId);
            if (user == null) {
                user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId, User.class);
                if (user == null) {
                    user = DBUtil.selectByPrimaryKey(userId);
                }
            }
        }
        if (user == null || StringUtils.isEmpty(userId)) {
            //创建账号
            user = new User();
            long newId = System.nanoTime() / 1000;
            user.setId(newId + "");
            user.setNick_name(params.getDisplayName());
            user.setIco_url("");
            user.setUser_defined_head("");
            user.setMoney(Config.FACEBOOK_INIT_MONEY);
            int count = createUser(req, user);

            LoggerUtils.tableLog.info("1save userInfo->" + user.toString());
            StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
            logger.info("created user " + userId);

            //发送日志
            LogService.OBJ.sendMoneyLog(user,0, user.getMoney(),user.getMoney(), LogReasons.CommonLogReason.CREATE_ROLE);

            return JoloAuth.JoloCommon_LoginAck.newBuilder()
                    .setIsNew(true)
                    .setUserId(user.getId())
                    .setMoney(user.getMoney())
                    .setIcoUrl(user.getIco_url())
                    .setNickName(user.getNick_name())
                    .setDefaultIco(user.getUser_defined_head())

                    .setVerify(encry_RC4(user.getId()))
					.setChannelId(user.getChannel_id())
                    .addAllServerinfo(getServerinfo())
                    .setResult(count == 1 ? 1 : 0).build();
        }

        //已经存在
        JoloAuth.JoloCommon_LoginAck.Builder builder = JoloAuth.JoloCommon_LoginAck.newBuilder();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        if (!fmt.format(user.getLast_login()).toString().equals(fmt.format(new Date()).toString())) {
            user.setMoney(user.getMoney() + Config.FIRSTLANDING);
            builder.setFirstLanding(Config.FIRSTLANDING);
        }

        //检测花钱购买的头像
        if(StringUtils.isNotEmpty(user.getUser_defined_head())) {
            ItemConfigBean itemConfigBean = CheckUtils.checkHead(user.getUser_defined_head());
            if (itemConfigBean != null) {
                boolean b = CheckUtils.expireHead(user.getId(), itemConfigBean.getId());
                if (b) {
                    user.setUser_defined_head(randomIcon());
                }
            }
        }
        user.setNick_name(params.getDisplayName());
        user.setClient_version(req.getClientVersion());
        user.setPackage_name(req.getPackName());
        user.setLast_login(new Date());
        user.setDown_platform(req.getDownPlatform());
        DBUtil.updateByPrimaryKey(user);

        /**保存玩家信息到缓存*/
        LoggerUtils.tableLog.info("save userInfo->" + user.toString());
        StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);

        //发送登录日志
        LogService.OBJ.sendUserLoginLog(user);
        return builder
                .setUserId(user.getId())
                .setMoney(user.getMoney())
                .setIcoUrl(user.getIco_url())
                .setDefaultIco(user.getUser_defined_head() == null ? "" : user.getUser_defined_head())
                .setNickName(user.getNick_name())
                .setVerify(encry_RC4(user.getId()))
				.setChannelId(user.getChannel_id())
                .addAllServerinfo(getServerinfo())
                .setResult(1).build();
    }

    private static Map<String , String> makMap (HuaweiParams params){
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("method", "external.hms.gs.checkPlayerSign");
        requestParams.put("appId", Config.HUAWEI_LOGIN_APPID);
        requestParams.put("cpId", Config.HUAWEI_LOGIN_CPID);
        requestParams.put("ts", params.getTs());
        requestParams.put("playerId", params.getPlayerId());
        requestParams.put("playerLevel", params.getPlayerLevel());
        requestParams.put("playerSSign", params.getGameAuthSign());
        return requestParams;
    }

}

