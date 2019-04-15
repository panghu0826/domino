package com.jule.domino.game.notice;

import com.jule.domino.game.notice.network.IOServer;
import com.jule.domino.game.notice.service.GateRegisterService;
import lombok.extern.slf4j.Slf4j;

/**
 * notice服务
 * @author
 * @since 2018/12/3 11:50
 */
@Slf4j
public class NoticeServer {

    public static final NoticeServer OBJ = new NoticeServer();

    /**
     * 启动
     */
    public void start(){
        try {
            log.info("Notice服务器开始启动");
            //启动线程：定时轮询维护最新的GateSvr列表
            GateRegisterService.OBJ.init();

            //网关先连接
            IOServer.connect();
        }catch (Exception ex){
            log.error("Notice服务器失败",ex);
            throw new Error("Notice服务器失败",ex);
        }
    }

}
