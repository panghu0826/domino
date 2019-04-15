package com.jule.core.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.Set;

/**
 * Redis Pool connection factory
 *
 * @author : xujian
 */
public class JedisFactory {
    private static final Logger logger = LoggerFactory.getLogger(JedisFactory.class);
    private static JedisSentinelPool jedisSentinelPool;
    private static JedisPool jedisPool;

     static {
         try {
             if (JedisConfig.REDIS_OPEN_SENTINEL) {
                 initSentinel();
             } else {
                 init();
             }
         } catch (Exception e) {
             logger.error(e.getMessage(), e);
         }

     }

    public static synchronized void init() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(JedisConfig.REDIS_MAX_TOTAL);
        jedisPoolConfig.setMaxIdle(JedisConfig.REDIS_MAX_IDLE);

        if (!JedisConfig.REDIS_PASSWORD.equalsIgnoreCase("undefined")) {
            jedisPool = new JedisPool(jedisPoolConfig, JedisConfig.REDIS_IP, JedisConfig.REDIS_PORT, 0, JedisConfig.REDIS_PASSWORD);
        } else {
            jedisPool = new JedisPool(JedisConfig.REDIS_IP, JedisConfig.REDIS_PORT);
        }
    }

    public static synchronized void initSentinel() throws Exception {
        if (JedisConfig.DB_ID > 0) {
            throw new Exception("JedisConfig.DB_ID configuration error DBID:" + JedisConfig.DB_ID);
        }
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(JedisConfig.REDIS_MAX_TOTAL);
        jedisPoolConfig.setMaxIdle(JedisConfig.REDIS_MAX_IDLE);
        String[] ipAndPorts = JedisConfig.REDIS_SENTINEL_ADDRESS.split(";");
        Set<String> sentinels = new HashSet<>();
        for (String address : ipAndPorts) {
            sentinels.add(address);
        }
        if (sentinels.size() <= 0) {
            throw new Exception("sentinels is null ");
        }
        if (!JedisConfig.REDIS_PASSWORD.equalsIgnoreCase("undefined")) {
            jedisSentinelPool = new JedisSentinelPool(JedisConfig.REDIS_MASTER_NAME, sentinels, JedisConfig.REDIS_PASSWORD);
        } else {
            jedisSentinelPool = new JedisSentinelPool(JedisConfig.REDIS_MASTER_NAME, sentinels);
        }
    }

    /**
     * 获得模块下的Jedis连接池。随后就可以用pool.getResource()获取Jedis实例，并要在final段returnResource()
     *
     * @return
     */
    public static JedisPool getPool() {
        return jedisPool;
    }

    public static Jedis getJedis() {
        Jedis jedis = null;
        jedis = jedisPool == null ? jedisSentinelPool == null ? null : jedisSentinelPool.getResource() : jedisPool.getResource();
        if (jedis == null) {
            logger.error("Error accessing Jedis ");
        }
        return jedis;
    }

}
