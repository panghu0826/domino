package com.jule.core.utils.xml;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.LoggerFactory;

public class LogConfigUtils {
    public static void initLogConfig() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.reset();
            configurator.doConfigure("./config/logback.xml");
        } catch (JoranException je) {
            throw new RuntimeException("Failed to configure loggers, shutting down...", je);
        }
    }
}
