package com.boot.cache.BinaryJedis;

import com.boot.cache.JedisFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

/**
 *
 */
public class BinaryJedisGeneral {
    private static final Logger log = LoggerFactory.getLogger(BinaryJedisGeneral.class);
    public static boolean running = false;
    public static final String OK = "OK";
    public static final String SPLIT = ":";

    /**
     * 停止服务
     */
    public static void shutdown() {
        log.info(" stored  service  stoped");
        running = false;
        JedisPool pool = JedisFactory.getPool();
        if (pool != null)
            pool.destroy();
        System.gc();
    }

    /**
     * 订阅消息
     */
    public static boolean subscribe(JedisPubSub jedisPubSub, String... channels) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return false;
        Jedis jedis = pool.getResource();
        try {
            jedis.subscribe(jedisPubSub, channels);
        } catch (Exception e) {
            return false;
        } finally {
            jedis.close();
        }
        return true;
    }
    /**
     * 发布消息
     */
    public static boolean publish(String msg, String channel) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return false;
        Jedis jedis = pool.getResource();
        try {
            jedis.publish(channel, msg);
        } catch (Exception e) {
            return false;
        } finally {
            jedis.close();
        }
        return true;
    }


    /**
     * 清库(只清理选中的库)
     * @param dbId
     */
    public static void cleanDB(int dbId) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            redis.flushDB();
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if(redis != null){
                redis.close();
            }
        }
    }
}
