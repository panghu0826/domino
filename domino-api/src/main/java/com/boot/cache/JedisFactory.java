package com.boot.cache;

import com.boot.config.JedisConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;


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
            init();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    public static synchronized void init() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(JedisConfig.getPoolMaxTotal());
        jedisPoolConfig.setMaxIdle(JedisConfig.getPoolMaxIdle());
        jedisPoolConfig.setMaxWaitMillis(10000);
        jedisPool = new JedisPool(JedisConfig.getHost(), JedisConfig.getPort());
       /* if (!JedisConfig.getPassword().equalsIgnoreCase("undefined")) {
            jedisPool = new JedisPool(jedisPoolConfig, JedisConfig.getHost(), JedisConfig.getPort(), 0, JedisConfig.getPassword());
        } else {
            jedisPool = new JedisPool(JedisConfig.getHost(), JedisConfig.getPort());
        }*/
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
        jedis = jedisPool==null?jedisSentinelPool==null?null:jedisSentinelPool.getResource():jedisPool.getResource();
        if(jedis == null){
            logger.error("Error accessing Jedis ");
        }
        return jedis;
    }

}
