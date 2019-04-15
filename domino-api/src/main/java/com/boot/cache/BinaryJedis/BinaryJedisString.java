package com.boot.cache.BinaryJedis;

import com.boot.cache.JedisFactory;
import com.boot.cache.StoredObj;
import com.boot.cache.StoredObjCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.boot.cache.BinaryJedis.BinaryJedisGeneral.OK;


/**
 *
 */
public class BinaryJedisString extends StoredObjCodec {
    private static final Logger log = LoggerFactory.getLogger(BinaryJedisString.class);

    /**
     * 是否存在某个key的对象
     *
     * @param key
     *            查询key
     * @return 是否存在
     */
    public static boolean exists(int dbId, String key) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return redis.exists(encode(key));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return false;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }
    /**
     * 保存单条String
     *
     * @param key
     *            保存key
     * @param value
     *            保存的String对象
     */
    public static boolean set(int dbId, String key, String value) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            long st = System.currentTimeMillis();
            redis = pool.getResource();
            redis.select(dbId);
            String back = redis.set(encode(key), encode(value));
            if(System.currentTimeMillis() - st >1000){
                log.warn("redis set too long,key = "+key);
            }
            if(back.equals(OK)){
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
        return false;
    }

    /**
     * @param dbId
     * @param key
     * @return
     */
    public static String getString(int dbId, String key) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            byte [] d = redis.get(encode(key));
            return toStr(d);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return null;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }
    /**
     * 将单个对象绑定到指定key
     *
     * @param <T>
     *            对象类型
     * @param key
     *            指定key
     * @param object
     *            绑定的对象
     */
    public static <T extends StoredObj> boolean set( int dbId, String key, T object) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            long st = System.currentTimeMillis();
            redis = pool.getResource();
            redis.select(dbId);
            /*
            在 Redis 2.6.12 以前版本， SET 命令总是返回 OK 。
            从 Redis 2.6.12 版本开始， SET 在设置操作成功完成时，才返回 OK 。*/
            String back = redis.set(encode(key), encode(object));
            if(System.currentTimeMillis() - st >1000){
                log.warn("redis set too long,key = "+key);
            }
            return back.equals(OK);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        } finally {
            if(redis != null){
                redis.close();
            }
        }
        return false;
    }

    /**
     * 根据指定key 查询对象
     *
     * @param key
     *            查询key
     * @return 保存的对象 (如果key 不存在，返回null)
     */
    public static <T extends StoredObj> T get(int dbId, String key, Class<T> c) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            long st = System.currentTimeMillis();
            redis = pool.getResource();
            redis.select(dbId);
            byte[] data = redis.get(encode(key));
            if(System.currentTimeMillis() - st >1000){
                log.warn("redis set too long,key = "+key);
            }
            return decode(data, c);

        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return null;
        } finally {
            if(redis != null){
                redis.close();
            }
        }
    }


    /**
     * Redis Setex 命令为指定的 key 设置值及其过期时间。如果 key 已经存在， SETEX 命令将会替换旧的值。
     * @param dbId
     * @param key
     * @param str
     * @param timeOut 以秒为单位
     */
    public static boolean setExString(int dbId, String key, String str, int timeOut) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            String back = redis.setex(encode(key), timeOut, encode(str));//设置成功时返回 OK 。
            return back.equals(OK);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        } finally {
            if(redis != null ) {
                redis.close();
            }
        }
        return false;
    }


    /**
     *
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作。
     如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
     本操作的值限制在 64 位(bit)有符号数字表示之内。
     * @param dbId
     * @param key
     * @return 执行 INCR 命令之后 key 的值。
     */
    public static long incrNumber(int dbId, String key) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return redis.incr(encode(key));//对存储在指定key的数值执行原子的加1操作。
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return 0;
        } finally {
            if(redis != null ) {
                redis.close();
            }
        }
    }

    /**
     * Redis Decr 命令将 key 中储存的数字值减一。
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECR 操作。
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
     * 本操作的值限制在 64 位(bit)有符号数字表示之内。
     * @param dbId
     * @param key
     * @return 执行命令之后 key 的值。
     */
    public static long decr(int dbId, String key) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return redis.decr(encode(key));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return 0;
        } finally {
            if(redis != null ) {
                redis.close();
            }
        }
    }

    /**
     * 根据key 模糊查询keys
     * 时间复杂度O(n) 慎用
     * @param key
     * @return 包含key的string list
     */
    public static List<String> keys(int dbId, String key) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            Set<byte[]> keys = redis.keys(encode(key + "*"));
            if (keys != null && keys.size() > 0) {
                List<String> objs = new ArrayList<>(keys.size());
                for (byte[] bs : keys) {
                    objs.add(toStr(bs));
                }
                return objs;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        } finally {
            if(redis != null ) {
                redis.close();
            }
        }
    }

    /**
     * 根据key 模糊查询对象
     *
     * @param key
     * @return 包含对象的set
     */
    public static <T extends StoredObj> Set<T> keys(int dbId, String key, Class<T> c) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            Set<byte[]> keys = redis.keys(encode(key + "*"));
            if (keys != null && keys.size() > 0) {
                Set<T> objs = new HashSet<T>();
                for (byte[] bs : keys) {
                    objs.add(decode(redis.get(bs), c));
                }
                return objs;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        } finally {
            if(redis != null ) {
                redis.close();
            }
        }
    }
    /**
     * 移除单个缓存对象
     *
     * @param key
     */
    public static boolean remove(int dbId, String key) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            long result = redis.del(encode(key));
            if(result == 1){
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if(redis != null){
                redis.close();
            }
        }
        return false;
    }

    /**
     * 将 key 的值设为 value ，当且仅当 key 不存在。
     * 若给定的 key 已经存在，则 SETNX 不做任何动作。
     * SETNX 是『SET if Not eXists』(如果不存在，则 SET)的简写。
     * @param dbId
     * @param key
     * @param value
     * @return 设置成功，返回 1 。设置失败，返回 0 。
     */
    public static long setnx(int dbId, String key,String value){
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return redis.setnx(encode(key),encode(value));
        } catch (Exception e) {
            log.error(e.getMessage());
            return 0;
        } finally {
            if(redis != null){
                redis.close();
            }
        }
    }
}
