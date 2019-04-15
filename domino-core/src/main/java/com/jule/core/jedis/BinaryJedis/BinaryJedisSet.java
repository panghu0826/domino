package com.jule.core.jedis.BinaryJedis;

import com.jule.core.jedis.JedisFactory;
import com.jule.core.jedis.StoredObj;
import com.jule.core.jedis.StoredObjCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.JedisPool;

import java.util.Set;

public class BinaryJedisSet extends StoredObjCodec {
    private static final Logger log = LoggerFactory.getLogger(BinaryJedisSet.class);

    public static <T extends StoredObj> long sadd(int dbId, String key, T object) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            long len = redis.sadd(encode(key), encode(object));
            return len;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
        return 0;
    }

    public static long sadd(int dbId, String key, String value) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            long len = redis.sadd(encode(key), encode(value));
            return len;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
        return 0;
    }

    /**
     *
     * @param <T>
     *     被成功移除的元素的数量，不包括被忽略的元素。
     * @param dbId
     * @param key
     * @param object
     */
    public static <T extends StoredObj> long srem(int dbId, String key, T object) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return redis.srem(encode(key), encode(object));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
        return 0;
    }

    public static <T extends StoredObj> void smove(int dbId, String srcKey, String destKey, T object) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            redis.smove(encode(srcKey), encode(destKey), encode(object));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    public static <T extends StoredObj> long scard(int dbId, String key) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return redis.scard(encode(key));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return -1;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    public static <T extends StoredObj> boolean sismember(int dbId, String key, T object) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return redis.sismember(encode(key), encode(object));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return false;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    public static <T extends StoredObj> Set<T> smembers(int dbId, String key, Class<T> clazz) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return decode(redis.smembers(encode(key)), clazz);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return null;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    public static Set<String> smembers(int dbId, String key) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return toStr(redis.smembers(encode(key)));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return null;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    public static <T extends StoredObj> T spop(int dbId, String key, Class<T> clazz) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return decode(redis.spop(encode(key)), clazz);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return null;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }
}
