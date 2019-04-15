package com.jule.domino.game.notice.service;

import com.jule.domino.game.config.Config;
import com.jule.domino.game.notice.model.GateServerInfo;
import com.jule.domino.game.notice.pool.GateConnectPool;
import com.jule.domino.game.notice.pool.GateServerGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 维护GateSvr列表
 * @author
 * @since 2018/12/3 10:49
 */
@Slf4j
public class GateRegisterService {

    //单例
    public static final GateRegisterService OBJ = new GateRegisterService();

    /**
     * 初始化
     */
    public void init(){
        //单线程30秒轮询
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(
                ()->register(),
                0,
                30,
                TimeUnit.SECONDS);
    }

    /**
     * 注册操作
     */
    public void register(){
        try {
            String[] gameIds = Config.GAME_IDS.split(":");
            for(String gameId : gameIds){
                List<GateServerInfo> gates = JedisService.getInstance().GetGateSvrList(gameId);
                log.debug("gateSvrInfoList size = " + gates.size()+" gameId:"+gameId);
                if (gates == null || gates.size() == 0){
                    continue;
                }

                for (GateServerInfo gate : gates) {
                    boolean isHaveConnPool = GateServerGroup.getInstance().isHaveConnectPool(gate.getGateSvrId());
                    log.debug("gateSvrId = " + gate.getGateSvrId() + ", ip=" + gate.getIp() + ", port=" + gate.getPort() + ", isHasConnPool = " + isHaveConnPool);
                    if (isHaveConnPool){
                        continue;
                    }

                    //创建连接池
                    GateConnectPool connPool = new GateConnectPool(gate.getGateSvrId());
                    //注册连接池
//                    GateServerGroup.getInstance().registerGameServerGroup(connPool);
                }
            }
        } catch (Exception ex) {
            log.error("Gate发现异常, ex=" + ex.getMessage(), ex);
        }
    }

}
