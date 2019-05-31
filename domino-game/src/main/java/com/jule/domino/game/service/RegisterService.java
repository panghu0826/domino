package com.jule.domino.game.service;

import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;
import com.jule.core.service.CronTaskManager;
import com.jule.core.service.ThreadPoolManager;
import com.jule.domino.game.config.Config;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.GameSvrRelationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.zip.CRC32;

/**
 * @author xujian 2017-12-15
 * 向gate注册服务器信息并定时推送负载//或者后续通过直连推送
 */
public class RegisterService {
    private final static Logger logger = LoggerFactory.getLogger(RegisterService.class);
    public static volatile AtomicInteger ON_LINE_CONUNT = new AtomicInteger(0);

    public static int GAME_SERVER_ID;
    public static String ADDRESS;

    private static class SingletonHolder {
        protected static final RegisterService instance = new RegisterService();
    }

    public static final RegisterService getInstance() {
        return SingletonHolder.instance;
    }

    public RegisterService() {
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
        }
        ADDRESS = Config.BIND_IP + ":" + Config.BIND_PORT;
        GAME_SERVER_ID = makeGameSvrId(ADDRESS);
    }

//    /**
// * 向redis 注册服务器
// */
//        public void onServiceStartUp() {
//            JSONObject jsonObject = new JSONObject().accumulate("serverId", GAME_SERVER_ID).accumulate("ip", ip).accumulate("port", Config.BIND_PORT).accumulate("onlineNum", 0);
//            if (JedisPoolWrap.getInstance().hSet(SERVER_KEY, Config.SUB_GAME_ID + "_" + GAME_SERVER_ID, jsonObject.toString())) {
//            logger.debug("gameSvrId:------------------------"+SERVER_KEY+":--:"+Config.SUB_GAME_ID + "_" + GAME_SERVER_ID+":--:"+jsonObject.toString());
//                logger.info("Register SubGameSvrId->" + GAME_SERVER_ID + ",addr->" + ADDRESS + " success");
//            } else {
//                logger.info("Register SubGameSvrId->" + GAME_SERVER_ID + ",addr->" + ADDRESS + " fail");
//            }
//
//        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
//
//            int count = TableService.getInstance().getOnLineNum();
//            JSONObject _jsonObject = new JSONObject().accumulate("serverId", GAME_SERVER_ID).accumulate("ip", ip).accumulate("port", Config.BIND_PORT).accumulate("onlineNum", count);
//            JedisPoolWrap.getInstance().hSet(SERVER_KEY, Config.SUB_GAME_ID + "_" + GAME_SERVER_ID, _jsonObject.toString());
//
//        }, 0, 5 * 1000, TimeUnit.MILLISECONDS);
//    }


    /**
     * 向redis 注册服务器
     */
    public void onServiceStartUp() {
        try {
            String[] gameIds = Config.GAME_IDS.split(":");
            GameSvrRelationModel relationModel = new GameSvrRelationModel(ADDRESS, "" + GAME_SERVER_ID);
            logger.debug("------------------"+GAME_SERVER_ID);
            logger.debug("==================="+relationModel.getGameSvrId());
            for (String gameId : gameIds) {
                if (StoredObjManager.sadd(RedisConst.GAME_SVR_LIST.getProfix() + gameId, relationModel)) {
                    StoredObjManager.hset(RedisConst.GAME_SVR_EXPIRE.getProfix()+gameId,
                            RedisConst.GAME_SVR_EXPIRE.getField()+ADDRESS+ GAME_SERVER_ID,
                            System.currentTimeMillis()+"");
                    //if (JedisPoolWrap.getInstance().hSet(GameConst.SERVER_KEY + gameId, ADDRESS, GAME_SERVER_ID + "")) {
                    logger.info("向redis 注册服务器Register GameSvrId->" + GAME_SERVER_ID + ",Addr->" + ADDRESS + " Success");
                } else {
                    logger.info("向redis 注册服务器Register GameSvrId->" + GAME_SERVER_ID + ",Addr->" + ADDRESS + " Fail");
                }

                Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                    int count = PlayerService.getPLAYER_MAP().size();
                    //logger.info( "current num is "+ count+" small Config.PlayerLimit "+(count < Config.PLAYER_LIMIT));
                    if (count < Config.PLAYER_LIMIT) {
                        JedisPoolWrap.getInstance().zadd(GameConst.GAME_STATE_ +  gameId, count < 0 ? 0 : count, GAME_SERVER_ID + "", 0);
                    } else {
                        JedisPoolWrap.getInstance().zdel(GameConst.GAME_STATE_ + gameId, GAME_SERVER_ID + "");
                        logger.info("del game_server_id:" + GAME_SERVER_ID);
                    }
                }, 0, 5 * 1000, TimeUnit.MILLISECONDS);

                //每5秒修改一下最后修改时间
                Consumer<Object> callReqIsHasAck  = obj -> {
                    ThreadPoolManager.getInstance().addTask(new Runnable() {
                        @Override
                        public void run() {
                            StoredObjManager.hset(RedisConst.GAME_SVR_EXPIRE.getProfix()+gameId,
                                    RedisConst.GAME_SVR_EXPIRE.getField()+ADDRESS+ GAME_SERVER_ID,
                                    System.currentTimeMillis()+"");
//                            logger.info("serID:"+RegisterService.GAME_SERVER_ID+"setLastUpdateTime(),gameId:"+gameId);
                        }
                    });
                };
                CronTaskManager.CountDownTask taskConfig = new CronTaskManager.CountDownTask(0, callReqIsHasAck);
                CronTaskManager.getInstance().addCountDownJob(taskConfig.getName(), "CheckReqIsHasAck", taskConfig, "0/5 * * * * ?");
            }

            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                int count = PlayerService.getPLAYER_MAP().size();
                logger.info( "60 second: current player num is "+ count);
                LogService.OBJ.sendOnlineNumber(Config.BIND_IP,count);
                StoredObjManager.hset(RedisConst.GAME_SVR_TOTAL_USER.getProfix() ,
                        RedisConst.GAME_SVR_TOTAL_USER.getField()+ Config.BIND_IP, "" + count);
            }, 0, 60 * 1000, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    /**
     * 生成一个为一个服务器id
     *
     * @return
     */
    private int makeGameSvrId(String address) {
//        CRC32 crc32 = new CRC32();
//        crc32.update(address.getBytes());
//        long ret = Math.abs(crc32.getValue() & 0xFFFFFFFF);
//        //强转会变为负数
//        return (int) ret;
        return 88888;
    }
}
