package com.jule.domino.game.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.jule.core.jedis.JedisPoolWrap;
import com.jule.domino.base.enums.RedisChannel;
import com.jule.domino.game.room.service.RoomOprService;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

@Slf4j
public class TableSub extends Thread {

    @Override
    public void run() {
        final JedisPubSub jedisPubSub = new JedisPubSub() {
            public void onMessage(String channel, String message) {
                try {
                    log.info(String.format("receive redis published message, channel %s, message %s", channel, message));

                    //站起
                    if (channel.equals(RedisChannel.CHANGE_TABLE_CHANNEL.getChannelName())) {
                        JSONObject jsonObject = JSONObject.parseObject(message);
                        String gameId = jsonObject.containsKey("gameId") ? jsonObject.get("gameId").toString() : "";
                        String roomId = jsonObject.containsKey("roomId") ? jsonObject.get("roomId").toString() : "";
                        String tableId = jsonObject.containsKey("tableId") ? jsonObject.get("tableId").toString() : "";
                        String userId = jsonObject.containsKey("userId") ? jsonObject.get("userId").toString() : "";
                        String hashcode = jsonObject.containsKey("hashcode") ? jsonObject.get("hashcode").toString() : "";
                        if (Strings.isNullOrEmpty(gameId) || Strings.isNullOrEmpty(roomId) || Strings.isNullOrEmpty(tableId)
                                || Strings.isNullOrEmpty(tableId)) {
                            return;
                        }
                        boolean b = TableSubService.leave(userId, gameId, roomId, tableId);
                        if(b){
                            //离桌子成功
                            RoomOprService.OBJ.changeTableHandler(gameId,roomId,tableId,userId,hashcode);
                        }
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                }

            }

            public void onSubscribe(String channel, int subscribedChannels) {
                log.info(String.format("subscribe redis channel success, channel %s, subscribedChannels %d",
                        channel, subscribedChannels));
            }

            public void onUnsubscribe(String channel, int subscribedChannels) {
                log.info(String.format("unsubscribe redis channel, channel %s, subscribedChannels %d",
                        channel, subscribedChannels));

            }
        };

        //订阅渠道消息
        JedisPoolWrap.getInstance().subscribe(
                jedisPubSub,
                RedisChannel.CHANGE_TABLE_CHANNEL.getChannelName()
        );
    }
}
