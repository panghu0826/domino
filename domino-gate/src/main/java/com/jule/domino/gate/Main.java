package com.jule.domino.gate;

import com.google.common.base.Strings;
import com.jule.core.jedis.StoredObjManager;
import com.jule.core.service.CronTaskManager;
import com.jule.core.service.ThreadPoolManager;
import com.jule.core.utils.xml.LogConfigUtils;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.GameSvrRelationModel;
import com.jule.domino.gate.config.Config;
import com.jule.domino.gate.network.JoLoSslContext;
import com.jule.domino.gate.network.NoticeIOServer;
import com.jule.domino.gate.network.WSIOServer;
import com.jule.domino.gate.service.TableSub;
import com.jule.domino.gate.vavle.game.GameServerGroup;
import com.jule.domino.gate.vavle.room.RoomConnectPool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
public class Main {
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"all"};
        }
        try{
            LogConfigUtils.initLogConfig();

            Config.load(args[0]);//judge which game config
            log.info("init ssl context ->" + JoLoSslContext.DEFAULT.getProtocol().toString());
            log.info("enable.ssl=" + Config.ENABLE_SSL);
            WSIOServer.connect();
            NoticeIOServer.connect();

            //处理连接
            TableSub sub = new TableSub();
            sub.start();

            RoomConnectPool.getConnection();
            GameServerGroup.getInstance();
        }catch(Exception e){
            log.error(e.getMessage(),e);
        }


        //test();
    }

    private static void test(){
        //延迟但N秒内进行
        Consumer<Object> call = obj -> {
            ThreadPoolManager.getInstance().addTask(new Runnable() {
                @Override
                public void run() {
                    String[] gameIds = Config.GAME_IDS.split(":");
                    for (String gameId : gameIds) {
                        if (Strings.isNullOrEmpty(gameId)) {
                            continue;
                        }
                        Set<GameSvrRelationModel> list = StoredObjManager.smembers(RedisConst.GAME_SVR_LIST.getProfix() + gameId, GameSvrRelationModel.class);
                        if (list == null || list.size() == 0) {
                            return;
                        }
                        list.forEach(gameSvrRelationModel -> {
                            int _gameSvrId = Integer.parseInt(gameSvrRelationModel.getGameSvrId());
                            ChannelHandlerContext channelHandlerContext = GameServerGroup.getInstance().getConnect(_gameSvrId);
                            if (channelHandlerContext != null) {
                                ByteBuf byteBuf = null;
                                byteBuf = channelHandlerContext.alloc().buffer(28);
                                byteBuf.writeInt(0);
                                byteBuf.writeInt(0);
                                byteBuf.writeInt(0);
                                byteBuf.writeInt(1);
                                byteBuf.writeInt(0);
                                //临时写到预留字段里面标识是那个链接
                                byteBuf.writeLong(Long.valueOf("0"));
                                //在当前连接上记录最近交互的游戏服务器id
                                channelHandlerContext.channel().close();
                                channelHandlerContext.writeAndFlush(byteBuf);
                                log.info("gameId:{},gameSvrId:{}",gameId,_gameSvrId);
                            }
                        });

                    }
                }
            });
        };
        long t = new Date().getTime() + 1000L;
        CronTaskManager.CountDownTask task = new CronTaskManager.CountDownTask(t, call);
        CronTaskManager.getInstance().addCountDownJob(task.getName(), "IS_ALLOW", task,"0/10 * * * * ?");

    }

}
