package com.jule.domino.auth.loginprocess.impl;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.google.common.base.Strings;
import com.google.protobuf.MessageLite;

import com.jule.core.jedis.StoredObjManager;

import com.jule.core.utils.ThreadPoolManager;
import com.jule.domino.auth.config.Config;
import com.jule.domino.auth.dao.DBUtil;
import com.jule.domino.auth.loginprocess.ChannelId;
import com.jule.domino.auth.loginprocess.ILoginProcess;
import com.jule.domino.auth.service.LogService;
import com.jule.domino.auth.utils.runnable.UserRunnable;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author xujian
 * 游客登陆
 */
@ChannelId(name = "robot")
public class RobotLogin implements ILoginProcess {
    private static final Logger logger = LoggerFactory.getLogger(RobotLogin.class);

    @Override
    public MessageLite process(JoloAuth.JoloCommon_LoginReq req) throws Exception {
        //已经存在
        JoloAuth.JoloCommon_LoginAck.Builder builder = JoloAuth.JoloCommon_LoginAck.newBuilder();
        try {
            String userId = req.getUserId();
            logger.debug("机器人登录：robotID=" + userId);
            User user = null;
            if (!Strings.isNullOrEmpty(userId)) {
                user = DBUtil.selectByPrimaryKey(userId);
            }
            if (user == null || StringUtils.isEmpty(userId)) {
                logger.error(MessageFormat.format("机器人ID={0}不存在、登录失败", userId));

                return builder
                        .setUserId("")
                        .setMoney(0)
                        .setIcoUrl("")
                        .setNickName("")
                        .setDefaultIco("")
                        .setVerify("")
                        .setChannelId("")
                        .addAllServerinfo(getServerinfo())
                        .setResult(0).build();
            }


            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            Date userLast_login = user.getLast_login();
            String lastLogin = userLast_login == null ? "" : fmt.format(userLast_login).toString();
            String nowTime = fmt.format(new Date()).toString();
            if (!lastLogin.equals(nowTime)) {
                user.setMoney(user.getMoney() + Config.FIRSTLANDING);
                builder.setFirstLanding(Config.FIRSTLANDING);
            }
            user.setLast_login(new Date());
        //DBUtil.updateByPrimaryKey(user);
        ThreadPoolManager.getInstance().executeDbTask(new UserRunnable(user));
            /**保存玩家信息到缓存*/
            logger.info("5save userInfo->" + user.toString());
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
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
        }
        return builder
                .setUserId("")
                .setMoney(0)
                .setIcoUrl("")
                .setNickName("")
                .setDefaultIco("")
                .setVerify("")
                .setChannelId("")
                .addAllServerinfo(getServerinfo())
                .setResult(-1).build();
    }


}
