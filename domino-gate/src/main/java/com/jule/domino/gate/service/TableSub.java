package com.jule.domino.gate.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.jule.core.jedis.JedisPoolWrap;
import com.jule.domino.base.enums.RedisChannel;
import com.jule.domino.gate.vavle.net.ChannelManageCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

public class TableSub extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(TableSub.class);

    @Override
    public void run() {
        final JedisPubSub jedisPubSub = new JedisPubSub() {
            public void onMessage(String channel, String message) {
                try {
                    logger.info(String.format("receive redis published message, channel %s, message %s", channel, message));

                    //重复登录
                    if (channel.equals(RedisChannel.REPEAT_LOGIN_CHANNEL.getChannelName())) {
                        JSONObject jsonObject = JSONObject.parseObject(message);
                        String userId = jsonObject.containsKey("userId") ? jsonObject.get("userId").toString() : "";
                        String gateSvr = jsonObject.containsKey("gateSvr") ? jsonObject.get("gateSvr").toString() : "";
                        if (Strings.isNullOrEmpty(userId) || Strings.isNullOrEmpty(gateSvr)) {
                            return;
                        }
                        if(gateSvr.equals(RegisteService.ADDRESS)){
                            return;
                        }
                        ChannelManageCenter.getInstance().sub(userId);
                    }
                } catch (Exception e) {
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

        //订阅渠道消息
        JedisPoolWrap.getInstance().subscribe(
                jedisPubSub,
                RedisChannel.REPEAT_LOGIN_CHANNEL.getChannelName()
        );
    }
}
