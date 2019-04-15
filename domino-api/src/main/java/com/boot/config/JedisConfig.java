package com.boot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Jedis 配置文件
 *
 * @author
 *
 * @since 2018/7/23 14:43
 *
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class JedisConfig {

    private static int database;

    private static String host;

    private static int port;

    private static String password;

    private static int timeout;

    private static int poolMaxTotal;

    private static int poolMaxIdle;

    private static int poolMaxWait;

    public static int getDatabase() {
        return database;
    }

    @Value("${spring.redis.database}")
    public void setDatabase( int database ) {
        this.database = database;
    }

    public static String getHost() {
        return host;
    }

    @Value("${spring.redis.host}")
    public void setHost( String host ) {
        this.host = host;
    }

    public static int getPort() {
        return port;
    }

    @Value("${spring.redis.port}")
    public void setPort( int port ) {
        this.port = port;
    }

    public static String getPassword() {
        return password;
    }

    @Value("${spring.redis.password}")
    public void setPassword( String password ) {
        this.password = password;
    }

    public static int getTimeout() {
        return timeout;
    }

    @Value("${spring.redis.timeout}")
    public void setTimeout( int timeout ) {
        this.timeout = timeout;
    }

    public static int getPoolMaxTotal() {
        return poolMaxTotal;
    }

    @Value("${spring.redis.pool.max-active}")
    public  void setPoolMaxTotal( int poolMaxTotal ) {
        JedisConfig.poolMaxTotal = poolMaxTotal;
    }

    public static int getPoolMaxIdle() {
        return poolMaxIdle;
    }

    @Value("${spring.redis.pool.max-idle}")
    public void setPoolMaxIdle( int poolMaxIdle ) {
        JedisConfig.poolMaxIdle = poolMaxIdle;
    }

    public static int getPoolMaxWait() {
        return poolMaxWait;
    }

    @Value("${spring.redis.pool.max-wait}")
    public void setPoolMaxWait( int poolMaxWait ) {
        JedisConfig.poolMaxWait = poolMaxWait;
    }
}
