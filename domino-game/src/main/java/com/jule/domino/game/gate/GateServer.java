package com.jule.domino.game.gate;

import com.jule.domino.base.platform.HallAPIService;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.gate.login.LoginService;
import com.jule.domino.game.gate.network.NoticeIOServer;
import com.jule.domino.game.gate.network.WSIOServer;
import com.jule.domino.game.gate.pool.game.GameServerGroup;
import com.jule.domino.game.gate.pool.room.RoomConnectPool;
import lombok.extern.slf4j.Slf4j;


/**
 * gate 服务
 * @author
 * @since 2018/12/4 10:25
 */
@Slf4j
public class GateServer {

    //单例
    public static final GateServer OBJ = new GateServer();

    /**
     * gate 启动
     */
    public void start(){
        try {
            //Gate 启动
            WSIOServer.connect();

            //notice连接启动
            NoticeIOServer.connect();

            //登录注册
            LoginService.OBJ.init();

            RoomConnectPool.getConnection();
            GameServerGroup.getInstance();

            HallAPIService.OBJ.bindAccount(Config.GAME_ACCOUNTURL);
        }catch (Exception ex){
            log.error("Gate 启动异常", ex);
            throw new Error("Gate 启动失败", ex);
        }
    }


}
