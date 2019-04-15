package com.jule.domino.notice;

import com.jule.core.utils.xml.LogConfigUtils;
import com.jule.domino.notice.config.Config;
import com.jule.domino.notice.network.IOServer;
import com.jule.domino.notice.service.thread.UpdateGateSvrListThread;


public class Main {

    public static void main(String[] args) {
        LogConfigUtils.initLogConfig();
        Config.load(args[0]);

        //启动线程：定时轮询维护最新的GateSvr列表
        UpdateGateSvrListThread svrListThread = new UpdateGateSvrListThread();
        Thread tSvrListRun = new Thread(svrListThread);
        tSvrListRun.start();
        //网关先连接
        IOServer.connect();

    }

}