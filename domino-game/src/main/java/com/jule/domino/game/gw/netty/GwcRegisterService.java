package com.jule.domino.game.gw.netty;

import JoloProtobuf.GW.Gwc;
import com.jule.core.utils.Base64;
import com.jule.core.utils.HttpsUtil;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.gw.msg.HeartBeatReq_3;
import com.jule.domino.game.gw.msg.NotifyCloseReq_5;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 服务器启动时调用
 *
 * @author
 *
 * @since 2018/11/21 11:29
 */
@Slf4j
public class GwcRegisterService {

    public static final GwcRegisterService OBJ = new GwcRegisterService();

    public void checkChannel(){
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(()->check(),1,1, TimeUnit.MINUTES);
    }

    /**
     * 定时检测
     */
    public void check(){
        if (Config.GATESERVER_ISLOCAL){
            return;
        }

        try {
            Map<Long, ChannelHandlerContext> map = GwcMsgSerivce.OBJ.get_ioHandler();
            int curPoolSize = map.size();
            log.debug("连接池size = {}", curPoolSize);
            if (curPoolSize == 0){
                //无可用连接,上报,重新连接
                log.debug("连接异常重新上报");
                report();
            }
        }catch (Exception ex){
            log.error("定时check异常,ex={}",ex);
        }

    }

    public GwcRegisterService() {
        //消息处理器注册
        new HeartBeatReq_3();
        new NotifyCloseReq_5();
    }

    /**
     * 上报服务器消息
     */
    public void report(){
        try {
            Gwc.ReportServerInfo.Builder bean = Gwc.ReportServerInfo.newBuilder();
            bean.setServerType(String.valueOf(Config.GAME_ID))
                    .setServerID(Config.GAME_SERID)
                    .setServerURI(Config.GAME_GATEURL)
                    .setGwcURI(Config.GAME_GWCURL)
                    .setMax(Config.GAME_MAXLOAD);

            log.debug("上报参数{}", bean.toString());

            //消息体
            byte[] body = bean.build().toByteArray();

            //请求地址
            String url = String.format(Config.GAME_REPORTURL, Base64.encodeToString(body,false));
            log.info("url   = {}",url);

            //发送http请求
            HttpsUtil.doGet(url, false);
        }catch (Exception ex){
            log.error("上报GWC异常,ex={}",ex);
        }
    }

}

