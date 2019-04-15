package com.jule.domino.auth;


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.jule.domino.auth.config.Config;
import com.jule.domino.auth.loginprocess.ILoginProcess;
import com.jule.domino.auth.loginprocess.LoginProcessManager;
import com.jule.domino.auth.network.HttpIoServer;
import com.jule.domino.auth.service.DispacherService;
import com.jule.domino.auth.service.LogService;
import com.jule.domino.auth.service.ProductionService;
import com.jule.domino.base.service.ItemServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    public static void main(String[] args) {
        initLogConfig();
        Config.load();
        ItemServer.OBJ.init(Config.ITEM_SERVER_URL,Config.GAME_ID);
        LoginProcessManager.getInstance();
        DispacherService.getInstance();
        ProductionService.getInstance();
        HttpIoServer.accept();
        ILoginProcess.init();
        //注册日志服务
        LogService.OBJ.init(Config.LOG_URL);
    }

    public static void initLogConfig() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.reset();
            configurator.doConfigure("config/logback.xml");
        } catch (JoranException je) {
            throw new RuntimeException("Failed to configure loggers, shutting down...", je);
        }
    }
}
