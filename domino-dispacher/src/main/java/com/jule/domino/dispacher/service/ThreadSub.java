package com.jule.domino.dispacher.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.jule.core.common.log.LoggerUtils;
import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisChannel;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.dispacher.config.Config;
import com.jule.domino.dispacher.dao.DBUtil;
import com.jule.domino.log.service.LogReasons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

public class ThreadSub extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(ThreadSub.class);

    @Override
    public void run() {
        final JedisPubSub jedisPubSub = new JedisPubSub() {
            public void onMessage(String channel, String message) {
                logger.info(String.format("receive redis published message, channel %s, message %s", channel, message));
                if (channel.equals(RedisChannel.PAY_NOTICE.getChannelName())) {
                    JSONObject jsonObject = JSONObject.parseObject(message);
                    //更新消息
                    UserService.getInstance().sendPayNoticeMsg(jsonObject.getString("userId"), jsonObject.getLong("money"));
                    //弹窗消息
                    UserService.getInstance().sendPayAddNoticeMsg(jsonObject.getString("userId"), jsonObject.getLong("addmoney"));
                }
                if (channel.equals(RedisChannel.MAIL_ATTACHMENT.getChannelName())) {
                    JSONObject jsonObject = JSONObject.parseObject(message);
                    String userId = jsonObject.getString("userId");
                    long money = jsonObject.getLong("money");
                    User user = DBUtil.selectByPrimaryKey(userId);
                    if (user == null) {
                        LoggerUtils.mailLog.info("user is null userId:{}", userId);
                        return;
                    }
                    double _org = user.getMoney();

                    user.setMoney(user.getMoney() + Integer.parseInt("" + money));

                    if (DBUtil.updateByPrimaryKey(user) == 0) {
                        return;
                    }
                    //发送日志
                    LogService.OBJ.sendMoneyLog(user, _org, user.getMoney(), money, LogReasons.CommonLogReason.MAIL_REWARD);
                    /**保存玩家信息到缓存*/
                    logger.info("8save userInfo->" + user.toString());
                    StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
                    UserService.getInstance().sendPayNoticeMsg(userId, user.getMoney());
                }
                //重复登录
                if (channel.equals(RedisChannel.REPEAT_LOGIN_CHANNEL.getChannelName())) {
                    JSONObject jsonObject = JSONObject.parseObject(message);
                    String userId = jsonObject.containsKey("userId") ? jsonObject.get("userId").toString() : "";
                    String dispacherSvr = jsonObject.containsKey("dispacherSvr") ? jsonObject.get("dispacherSvr").toString() : "";
                    if (Strings.isNullOrEmpty(userId) || Strings.isNullOrEmpty(dispacherSvr)) {
                        return;
                    }
                    if (dispacherSvr.equals(Config.BIND_IP + Config.BIND_PORT)) {
                        return;
                    }

                    UserService.getInstance().onUserBreak(userId);
                }
                if(channel.equals(RedisChannel.MAIL_NOTICE_NEW_MAIL.getChannelName())){
                    UserService.getInstance().sendNewMailNoticeMsg();
                }
            }

            public void onSubscribe(String channel, int subscribedChannels) {
                logger.info(String.format("subscribe redis channel success, channel %s, subscribedChannels %d",
                        channel, subscribedChannels));
            }

            public void onUnsubscribe(String channel, int subscribedChannels) {
                logger.info(String.format("unsubscribe redis channel, channel %s, subscribedChannels %d",
                        channel, subscribedChannels));

            }
        };

        JedisPoolWrap.getInstance().subscribe(jedisPubSub, RedisChannel.PAY_NOTICE.getChannelName(),
                RedisChannel.REPEAT_LOGIN_CHANNEL.getChannelName(), RedisChannel.MAIL_ATTACHMENT.getChannelName(),
                RedisChannel.MAIL_NOTICE_NEW_MAIL.getChannelName());
    }
}
