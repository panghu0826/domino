package com.jule.domino.dispacher;

import com.jule.core.jedis.StoredObjManager;
import com.jule.core.utils.xml.LogConfigUtils;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.service.ItemServer;
import com.jule.domino.dispacher.config.Config;
import com.jule.domino.dispacher.network.JoLoSslContext;
import com.jule.domino.dispacher.network.WSIOServer;
import com.jule.domino.dispacher.service.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {

    public static void main(String[] args) {
        try{
            LogConfigUtils.initLogConfig();
            Config.load();
            ItemServer.OBJ.init(Config.ITEM_SERVER_URL,Config.GAME_ID);
            RoomConfigSerivce.OBJ.init();
            log.info("init ssl context ->" + JoLoSslContext.DEFAULT.getProtocol().toString());
            log.info("enable.ssl=" + Config.ENABLE_SSL);

            ThreadSub threadSub = new ThreadSub();
            threadSub.start();
            WSIOServer.connect();

            LogService.OBJ.init(Config.LOG_URL);
            VilidateService.OBJ.init();
            VersionService.OBJ.init();

            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                int count = UserService.getPLAYER_MAP().size();
                log.info("60 second: current player num is " + count);
                LogService.OBJ.sendOnlineNumber(Config.SERVER_IP , count);
                StoredObjManager.hset(RedisConst.DISPACHER_SVR_TOTAL_USER.getProfix()  ,
                        RedisConst.DISPACHER_SVR_TOTAL_USER.getField()+ Config.SERVER_IP, "" + count);
            }, 0, 60 * 1000, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

}
