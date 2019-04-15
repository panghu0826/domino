package com.jule.core.common.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * logger 通用类
 *
 * @author ran
 */
public class LoggerUtils {
    /**
     * 连接日志
     */
    public static final Logger linkLog = getLogger("com.game.link");
    /**
     * 桌内操作日志
     */
    public static final Logger timerServiceLog = getLogger("com.game.timer_service");
    /**
     * 桌内操作日志
     */
    public static final Logger tableLog = getLogger("com.game.table");
    /**
     * Server相关的日志
     */
    public static final Logger serverLogger = getLogger("com.game.server");
    /**
     * DAO相关的日志
     */
    public static final Logger daoLogger = getLogger("com.game.dao");
    /**
     * msg相关的日志
     */
    public static final Logger msg = getLogger("com.game.msg");
    /**
     * core相关的日志
     */
    public static final Logger coreLogger = getLogger("com.game.core");
    /**
     * core相关的日志
     */
    public static final Logger error = getLogger("com.game.error");
    /**
     * robot相关的日志
     */
    public static final Logger robot = getLogger("com.game.robot");
    /**
     * 数据表日志
     */
    public static final Logger dbTableLog = getLogger("com.db.table");
    /**
     * 邮件日志
     */
    public static final Logger mailLog = getLogger("com.jule.mail");
    /**
     * 性能日志
     */
    public static final Logger performance = getLogger("com.jule.performance");

    /**
     * 类默认构造器
     */
    private LoggerUtils() {
    }

    /**
     * 获取日志对象
     *
     * @param name
     */
    private static Logger getLogger(String name) {
        // 获取日志
        return LoggerFactory.getLogger(name);
    }
}
