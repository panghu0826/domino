package com.jule.core.jedis.BinaryJedis;

import com.jule.core.jedis.JedisFactory;
import com.jule.core.jedis.StoredObj;
import com.jule.core.jedis.StoredObjCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

public class BinaryJedisHash extends StoredObjCodec {
    private static final Logger log = LoggerFactory.getLogger(BinaryJedisHash.class);

    public static boolean hexists(int dbId, String key, String field) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return redis.hexists(encode(key), encode(field));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return false;
        } finally {
            if (redis != null) {
                redis.close();
            }
        }
    }

    public static long hdel(int dbId, String key, String field) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return redis.hdel(encode(key), encode(field));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return 0;
        } finally {
            if (redis != null) {
                redis.close();
            }
        }
    }

    /**
     * Redis Hset 命令用于为哈希表中的字段赋值 。
     * 如果哈希表不存在，一个新的哈希表被创建并进行 HSET 操作。
     * 如果字段已经存在于哈希表中，旧值将被覆盖。
     *
     * @param dbId
     * @param key
     * @param field
     * @param value
     * @return 如果字段是哈希表中的一个新建字段，并且值设置成功，返回 1 。 如果哈希表中域字段已经存在且旧值已被新值覆盖，返回 0
     */
    public static long hset(int dbId, String key, String field, String value) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return redis.hset(encode(key), encode(field), encode(value));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return 0;
        } finally {
            if (redis != null) {
                redis.close();
            }
        }
    }

    /**
     * Redis Hset 命令用于为哈希表中的字段赋值 。
     * 如果哈希表不存在，一个新的哈希表被创建并进行 HSET 操作。
     * 如果字段已经存在于哈希表中，旧值将被覆盖。
     *
     * @param dbId
     * @param key
     * @param field
     * @param <T>
     * @return 如果字段是哈希表中的一个新建字段，并且值设置成功，返回 1 。 如果哈希表中域字段已经存在且旧值已被新值覆盖，返回 0
     */
    public static <T extends StoredObj> long hset(int dbId, String key, String field, T object) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            long st = System.currentTimeMillis();
            redis = pool.getResource();
            redis.select(dbId);
            long result = redis.hset(encode(key), encode(field), encode(object));
            if (System.currentTimeMillis() - st > 1000) {
                log.warn("redis hset too long,key = " + key);
            }
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return 0;
        } finally {
            if (redis != null) {
                redis.close();
            }
        }
    }

    public static String hget(int dbId, String key, String field) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return toStr(redis.hget(encode(key), encode(field)));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return null;
        } finally {
            if (redis != null) {
                redis.close();
            }
        }
    }

    /**
     * @param dbId
     * @param key
     * @param field
     * @param c
     * @param <T>
     * @return 无则返回null
     */
    public static <T extends StoredObj> T hget(int dbId, String key, String field, Class<T> c) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            byte[] byteKey = encode(key);
            byte[] byteField = encode(field);

            long st = System.currentTimeMillis();
            redis = pool.getResource();
            redis.select(dbId);
            byte[] content = redis.hget(byteKey, byteField);
            if (System.currentTimeMillis() - st > 1000) {
                log.warn("redis hget too long,key = " + key);
            }
            T t = decode(content, c);

            return t;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return null;
        } finally {
            if (redis != null) {
                redis.close();
            }
        }
    }

    public static Set<String> hkeys(int dbId, String key) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        Set<String> result = new HashSet<String>();
        try {
            redis = pool.getResource();
            redis.select(dbId);
            Set<byte[]> tmp = redis.hkeys(encode(key));
            for (byte[] bs : tmp) {
                result.add(toStr(bs));
            }
            if (result.size() == 0) return null;
            return result;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        } finally {
            redis.close();
        }
    }

    public static <T extends StoredObj> List<T> hvals(int dbId, String key , Class<T> c) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        List<T> result = new ArrayList<>();
        try {
            redis = pool.getResource();
            redis.select(dbId);
            List<byte[]> tmp = redis.hvals(encode(key));
            for (byte[] bs : tmp) {
                result.add(decode(bs,c));
            }
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return null;
        } finally {
            if (redis != null) {
                redis.close();
            }
        }
    }

    public static List<String> hvals(int dbId, String key) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        List<String> result = new ArrayList<>();
        try {
            redis = pool.getResource();
            redis.select(dbId);
            List<byte[]> tmp = redis.hvals(encode(key));
            //log.info("h_value tmp size:" + tmp.size());
            for (byte[] bs : tmp) {
                result.add(toStr(bs));
                //log.info("h_value:" + tmp.toString());
            }
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return null;
        } finally {
            if (redis != null) {
                redis.close();
            }
        }
    }

    static long hlen(int dbId, String key) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return redis.hlen(encode(key));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return 0;
        } finally {
            if (redis != null) {
                redis.close();
            }
        }
    }

    static long hincrBy(int dbId, String key, String field, long num) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return redis.hincrBy(encode(key), encode(field), num);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return 0;
        } finally {
            if (redis != null) {
                redis.close();
            }
        }
    }

    public static Map<String, String> hgetAll(int dbId, String key) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        Map<String, String> result = new HashMap<>();
        try {
            redis = pool.getResource();
            redis.select(dbId);
            Map<byte[], byte[]> tmp = redis.hgetAll(encode(key));
            for (Map.Entry<byte[], byte[]> e : tmp.entrySet()) {
                result.put(toStr(e.getKey()), toStr(e.getValue()));
            }
            if (result.size() == 0) return null;
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return null;
        } finally {
            if (redis != null) {
                redis.close();
            }
        }
    }

    public static <T extends StoredObj> Map<String, T> hgetAll(int dbId, String key, Class<T> c) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        Map<String, T> result = new HashMap<>();
        try {
            redis = pool.getResource();
            redis.select(dbId);
            Map<byte[], byte[]> tmp = redis.hgetAll(encode(key));
            for (Map.Entry<byte[], byte[]> e : tmp.entrySet()) {
                result.put(toStr(e.getKey()), decode(e.getValue(), c));
            }
            if (result.size() == 0) return null;
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return null;
        } finally {
            if (redis != null) {
                redis.close();
            }
        }
    }

    /**
     * 将哈希表 key 中的域 field 的值设置为 value ，当且仅当域 field 不存在。
     * 若域 field 已经存在，该操作无效。
     * 如果 key 不存在，一个新哈希表被创建并执行 HSETNX 命令。
     *
     * @param dbId
     * @param key
     * @param field
     * @param value
     * @return 设置成功，返回 1 。 如果给定域已经存在且没有操作被执行，返回 0 。
     */
    public static long hsetnx(int dbId, String key, String field, String value) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return redis.hsetnx(encode(key), encode(field), encode(value));

        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return 0;
        } finally {
            if (redis != null) {
                redis.close();
            }
        }
    }
}
