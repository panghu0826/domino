package com.jule.domino.auth.loginprocess.impl;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.domino.auth.config.Config;
import com.jule.domino.auth.dao.DBUtil;
import com.jule.domino.auth.loginprocess.ChannelId;
import com.jule.domino.auth.loginprocess.ILoginProcess;
import com.jule.domino.auth.service.LogService;
import com.jule.domino.auth.utils.CheckUtils;
import com.jule.domino.base.bean.ItemConfigBean;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.log.service.LogReasons;
import com.google.common.base.Strings;
import com.google.protobuf.MessageLite;
import com.jule.core.jedis.StoredObjManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author xujian
 * 游客登陆
 */
@ChannelId(name = "guest")
public class GuestLogin implements ILoginProcess {
    private static final Logger logger = LoggerFactory.getLogger(GuestLogin.class);

    @Override
    public MessageLite process(JoloAuth.JoloCommon_LoginReq req) throws Exception {
        //已经存在
        JoloAuth.JoloCommon_LoginAck.Builder builder = JoloAuth.JoloCommon_LoginAck.newBuilder();
        try {
            String userId = req.getUserId();
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
                //新的游客创建新游客
                user = new User();
                long newId = System.nanoTime() / 1000;
                user.setId(newId + "");
                String nickName = getNickName(user.getId());
                user.setNick_name(nickName);
                user.setIco_url("");
                user.setUser_defined_head(randomIcon());
                user.setMoney(Config.GUEST_INIT_MONEY);
                int count = createUser(req, user);
                //User u = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), User.class);
                //if (u == null) {
                logger.info("1save userInfo->" + user.toString());
                StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
                //}
                logger.info("created user " + userId);

                //发送日志
                LogService.OBJ.sendMoneyLog(user, 0, user.getMoney(), user.getMoney(), LogReasons.CommonLogReason.CREATE_ROLE);

                return builder
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
            user.setClient_version(req.getClientVersion());
            user.setPackage_name(req.getPackName());
            user.setLast_login(new Date());
            user.setDown_platform(req.getDownPlatform());
            DBUtil.updateByPrimaryKey(user);
            /**保存玩家信息到缓存*/
            //User u = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), User.class);
            //if (u == null) {
            logger.info("3save userInfo->" + user.toString());
            StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
            //}
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
            logger.error(ex.getMessage(), ex);
        }
        return builder
                .setUserId("0")
                .setMoney(0)
                .setIcoUrl("")
                .setDefaultIco("")
                .setNickName("")
                .setVerify("")
                .setChannelId("")
                .addAllServerinfo(getServerinfo())
                .setResult(0).build();
    }

    /**
     * 来一个名字
     *
     * @param userId
     * @return
     */
    private String getNickName(String userId) {
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
            logger.error("create name exception , " + e.getMessage());
            return ("G-" + userId).length() > 8 ? ("G-" + userId).substring(0, 8) : ("G-" + userId);
        }
    }
}
