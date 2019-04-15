package com.jule.domino.auth.loginprocess.impl;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.MessageLite;
import com.jule.domino.auth.config.Config;
import com.jule.domino.auth.dao.DBUtil;
import com.jule.domino.auth.loginprocess.ChannelId;
import com.jule.domino.auth.loginprocess.ILoginProcess;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.auth.service.LogService;
import com.jule.domino.auth.utils.CheckUtils;
import com.jule.domino.auth.utils.FaceBookApi;
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

/**
 * @author xujian
 * 脸书登陆
 */
@ChannelId(name = "facebook")
public class FaceBookLogin implements ILoginProcess {
    private static final Logger logger = LoggerFactory.getLogger(FaceBookLogin.class);

    @Override
    public MessageLite process(JoloAuth.JoloCommon_LoginReq req) throws Exception {
        String accessToken = req.getToken();

        JSONObject jsonObject = FaceBookApi.getFaceBookUserInfo(accessToken);

        if (jsonObject == null) {
            return JoloAuth.JoloCommon_LoginAck.newBuilder()
                    .setUserId("")
                    .setMoney(0)
                    .setIcoUrl("")
                    .setDefaultIco("")
                    .setNickName("")
                    .setResult(0)
                    .setVerify("")
                    .setChannelId("")
                    .setResultMsg(ErrorCodeEnum.GATE_600001_4.getCode()).build();
        }
        logger.info("jsonObject->" + jsonObject.toString());
        String uid = jsonObject.getString("id");
        String nick = jsonObject.getString("name");
        String ico = jsonObject.getJSONObject("picture").getJSONObject("data").getString("url");
        User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + uid, User.class);
        if (user == null) {
            user = DBUtil.selectByPrimaryKey(uid);
        }
        if (user == null) {
            //不存在Facebook
            user = new User();
            user.setId(uid);
            user.setNick_name(nick);
            user.setIco_url(ico);
            user.setUser_defined_head("");
            user.setMoney(Config.FACEBOOK_INIT_MONEY);
            int count = createUser(req, user);
            //User u = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), User.class);
            //if (u == null) {
            logger.info("2save userInfo->" + user.toString());
            StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
            // }

            //发送日志
            LogService.OBJ.sendMoneyLog(user, 0, user.getMoney(), user.getMoney(), LogReasons.CommonLogReason.CREATE_ROLE);

            return JoloAuth.JoloCommon_LoginAck.newBuilder()
                    .setIsNew(true)
                    .setUserId(uid)
                    .setMoney(user.getMoney())
                    .setIcoUrl(user.getIco_url())
                    .setDefaultIco(user.getUser_defined_head())
                    .setNickName(user.getNick_name())
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

        //facebook的头像，每次获取都会重新生成地址、
        //会导致上个地址失效，所以每次登陆都需要更新ico
        if (user.getIco_url().equals(user.getUser_defined_head())) {
            user.setUser_defined_head(ico);
        }
        //检测花钱购买的头像
        if (StringUtils.isNotEmpty(user.getUser_defined_head())) {
            ItemConfigBean itemConfigBean = CheckUtils.checkHead(user.getUser_defined_head());
            if (itemConfigBean != null) {
                boolean b = CheckUtils.expireHead(user.getId(), itemConfigBean.getId());
                if (b) {
                    user.setUser_defined_head(ico);
                }
            }
        }
        user.setClient_version(req.getClientVersion());
        user.setPackage_name(req.getPackName());
        user.setIco_url(ico);
        user.setLast_login(new Date());
        user.setDown_platform(req.getDownPlatform());
        DBUtil.updateByPrimaryKey(user);
        //User u = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), User.class);
        //if (u == null) {//不需要判断直接重置 2018-07-12 lyb
        logger.info("4save userInfo->" + user.toString());
        StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
        // }
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
}
