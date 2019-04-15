package com.jule.domino.room.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.jule.core.jedis.JedisPoolWrap;
import com.jule.domino.base.enums.RedisChannel;
import com.jule.domino.room.model.TableInfo;
import com.jule.domino.room.service.holder.CommonConfigHolder;
import com.jule.domino.room.service.holder.RoomConfigHolder;
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

                    //站起
                    if (channel.equals(RedisChannel.STAND_UP_TABLE_CHANNEL.getChannelName())) {
                        JSONObject jsonObject = JSONObject.parseObject(message);
                        String gameId = jsonObject.containsKey("gameId") ? jsonObject.get("gameId").toString() : "";
                        String roomId = jsonObject.containsKey("roomId") ? jsonObject.get("roomId").toString() : "";
                        String tableId = jsonObject.containsKey("tableId") ? jsonObject.get("tableId").toString() : "";
                        if (Strings.isNullOrEmpty(gameId) || Strings.isNullOrEmpty(roomId) || Strings.isNullOrEmpty(tableId)) {
                            return;
                        }
                        //TableService
                        TableService.getInstance().addRoomCanJoinTable(gameId, roomId, tableId);
                    }
                    //GM
                    if (channel.equals(RedisChannel.GAME_GM_NOTICE_CHANNEL.getChannelName())) {
                        logger.info("订阅游戏后台操作 " + message);
                        switch(message){
                            case "2":
                                CommonConfigHolder.getInstance().init();
                                break;
                            case "3":
                                RoomConfigHolder.getInstance().init();
                                TableService.getInstance().init();
                                break;
                                default:
                                    break;
                        }
                    }
                    //离桌
                    if (channel.equals(RedisChannel.LEAVE_TABLE_CHANNEL.getChannelName())) {
                        JSONObject jsonObject = JSONObject.parseObject(message);
                        String gameId = jsonObject.containsKey("gameId") ? jsonObject.get("gameId").toString() : "";
                        String roomId = jsonObject.containsKey("roomId") ? jsonObject.get("roomId").toString() : "";
                        String tableId = jsonObject.containsKey("tableId") ? jsonObject.get("tableId").toString() : "";
                        String userId = jsonObject.containsKey("userId") ? jsonObject.get("userId").toString() : "";
                        if (Strings.isNullOrEmpty(gameId) || Strings.isNullOrEmpty(roomId) ||
                                Strings.isNullOrEmpty(tableId) || Strings.isNullOrEmpty(userId)) {
                            return;
                        }
                        TableInfo tableInfo = TableService.getInstance().getTable(gameId, roomId, tableId);
                        logger.info("leave desk tableInfo is null ?" + (tableInfo == null));
                        if (tableInfo != null) {
                            tableInfo.returnLobby(userId);
                        }
                        TableService.getInstance().destroyTable(gameId, roomId, tableId);
                    }
                    //新桌子
                    if (channel.equals(RedisChannel.CREATE_NEW_TABLE_CHANNEL.getChannelName())) {
                        JSONObject jsonObject = JSONObject.parseObject(message);
                        String gameId = jsonObject.containsKey("gameId") ? jsonObject.get("gameId").toString() : "";
                        String roomId = jsonObject.containsKey("roomId") ? jsonObject.get("roomId").toString() : "";
                        String tableId = jsonObject.containsKey("tableId") ? jsonObject.get("tableId").toString() : "";
                        if (Strings.isNullOrEmpty(gameId) || Strings.isNullOrEmpty(roomId) ||
                                Strings.isNullOrEmpty(tableId)) {
                            return;
                        }
                        TableService.getInstance().addExitTable(gameId, roomId, tableId);
                    }
                    //销毁桌子
                    if (channel.equals(RedisChannel.DESTROY_TABLE_CHANNEL.getChannelName())) {
                        JSONObject jsonObject = JSONObject.parseObject(message);
                        String gameId = jsonObject.containsKey("gameId") ? jsonObject.get("gameId").toString() : "";
                        String roomId = jsonObject.containsKey("roomId") ? jsonObject.get("roomId").toString() : "";
                        String tableId = jsonObject.containsKey("tableId") ? jsonObject.get("tableId").toString() : "";
                        if (Strings.isNullOrEmpty(gameId) || Strings.isNullOrEmpty(roomId) ||
                                Strings.isNullOrEmpty(tableId)) {
                            return;
                        }
                        TableService.getInstance().directDestroyTable(gameId, roomId, tableId);
                    }
                    //离桌成功
                    if (channel.equals(RedisChannel.CHANGE_TABLE_CHANNEL_RESULT.getChannelName())) {
                        JSONObject jsonObject = JSONObject.parseObject(message);
                        String gameId = jsonObject.containsKey("gameId") ? jsonObject.get("gameId").toString() : "";
                        String roomId = jsonObject.containsKey("roomId") ? jsonObject.get("roomId").toString() : "";
                        String tableId = jsonObject.containsKey("tableId") ? jsonObject.get("tableId").toString() : "";
                        String userId = jsonObject.containsKey("userId") ? jsonObject.get("userId").toString() : "";
                        String hashcode = jsonObject.containsKey("hashcode") ? jsonObject.get("hashcode").toString() : "";
                        if (Strings.isNullOrEmpty(gameId) || Strings.isNullOrEmpty(roomId) ||
                                Strings.isNullOrEmpty(tableId) || Strings.isNullOrEmpty(userId)
                                || Strings.isNullOrEmpty(hashcode)) {
                            return;
                        }
                        ChangeTableService.getInstance().ReqHandler(hashcode);
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
                RedisChannel.GAME_GM_NOTICE_CHANNEL.getChannelName(), RedisChannel.STAND_UP_TABLE_CHANNEL.getChannelName(),
                RedisChannel.LEAVE_TABLE_CHANNEL.getChannelName(), RedisChannel.CREATE_NEW_TABLE_CHANNEL.getChannelName(),
                RedisChannel.CHANGE_TABLE_CHANNEL_RESULT.getChannelName(),RedisChannel.DESTROY_TABLE_CHANNEL.getChannelName()
        );
    }
}
