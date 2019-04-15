package com.jule.core.jedis;

import com.jule.core.jedis.BinaryJedis.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * StoredObj 对象的管理器，负责创建或获取StoredObj 的Ref 存储key 采用表名-id 存储value 采用json 格式
 */
public class StoredObjManager {
    //public static int JedisConfig.DB_ID = 1;

    //public static void initDB(int dbid) {
    //JedisConfig.DB_ID = dbid;
    //    logger.info("select DB "+ dbid);
    //}

    //String//////////////////////////////////////////////////////////////////////

    /**
     * 判断是否存在
     *
     * @param key
     * @return
     */
    public static boolean exists(String key) {
        return BinaryJedisString.exists(JedisConfig.DB_ID, key);
    }

    /**
     * 删除已存在对象
     *
     * @param key 对象的key
     */
    public static boolean deleteExistsObj(String key) {
        return BinaryJedisString.remove(JedisConfig.DB_ID, key);
    }

    /**
     * @return
     */
    public static <T extends StoredObj> T get(Class<T> c, String key) {
        return (T) BinaryJedisString.get(JedisConfig.DB_ID, key, c);
    }

    /**
     * @param key
     * @param t
     * @param <T>
     * @return
     */
    public static <T extends StoredObj> boolean set(String key, T t) {
        return BinaryJedisString.set(JedisConfig.DB_ID, key, t);
    }

    /**
     * Redis Setex 命令为指定的 key 设置值及其过期时间。如果 key 已经存在， SETEX 命令将会替换旧的值。
     *
     * @param key
     * @param value
     * @param timeOut 单位秒
     * @return
     */
    public static boolean setEx(String key, String value, int timeOut) {
        return BinaryJedisString.setExString(JedisConfig.DB_ID, key, value, timeOut);
    }

    /**
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作。
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
     * 本操作的值限制在 64 位(bit)有符号数字表示之内。
     *
     * @param key
     * @return 执行 INCR 命令之后 key 的值。
     */
    public static long incr(String key) {
        return BinaryJedisString.incrNumber(JedisConfig.DB_ID, key);
    }

    /**
     * Redis Decr 命令将 key 中储存的数字值减一。
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECR 操作。
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
     * 本操作的值限制在 64 位(bit)有符号数字表示之内。
     *
     * @param key
     * @return 执行命令之后 key 的值。
     */
    public static long decr(String key) {
        return BinaryJedisString.decr(JedisConfig.DB_ID, key);
    }

    /**
     * 将 key 的值设为 value ，当且仅当 key 不存在。
     * 若给定的 key 已经存在，则 SETNX 不做任何动作。
     * SETNX 是『SET if Not eXists』(如果不存在，则 SET)的简写。
     *
     * @param key
     * @param value
     * @return 设置成功，返回 1 。设置失败，返回 0 。
     */
    public static long setnx(String key, String value) {
        return BinaryJedisString.setnx(JedisConfig.DB_ID, key, value);
    }

    public static boolean set(String key, String value) {
        return BinaryJedisString.set(JedisConfig.DB_ID, key, value);
    }
    public static double incrByFloat(String key,double incrment){
        return BinaryJedisString.incrByFloat(JedisConfig.DB_ID,key,incrment);
    }
    public static String get(String key){
        return BinaryJedisString.getString(JedisConfig.DB_ID,key);
    }
    //end String//////////////////////////////////////////////////////////////////

    //Hash////////////////////////////////////////////////////////////////////////

    /**
     * Redis Hset 命令用于为哈希表中的字段赋值 。
     * 如果哈希表不存在，一个新的哈希表被创建并进行 HSET 操作。
     * 如果字段已经存在于哈希表中，旧值将被覆盖。
     *
     * @param key
     * @param field
     * @param <T>
     * @return 如果字段是哈希表中的一个新建字段，并且值设置成功，返回 1 。 如果哈希表中域字段已经存在且旧值已被新值覆盖，返回 0
     */
    public static <T extends StoredObj> boolean hset(String key, String field, T t) {
        return BinaryJedisHash.hset(JedisConfig.DB_ID, key, field, t) >= 0;
    }

    /**
     * Redis Hset 命令用于为哈希表中的字段赋值 。
     * 如果哈希表不存在，一个新的哈希表被创建并进行 HSET 操作。
     * 如果字段已经存在于哈希表中，旧值将被覆盖。
     *
     * @param key
     * @param field
     * @param value
     * @return 如果字段是哈希表中的一个新建字段，并且值设置成功，返回 1 。 如果哈希表中域字段已经存在且旧值已被新值覆盖，返回 0
     */
    public static boolean hset(String key, String field, String value) {
        return BinaryJedisHash.hset(JedisConfig.DB_ID, key, field, value) >= 0;
    }

    /****
     * 给map set元素
     * @param c
     * @param key
     * @return
     */
    public static <T extends StoredObj> Boolean setStoredObjInMap(T c, String key, String field) {
        Long result = BinaryJedisHash.hset(JedisConfig.DB_ID, key, field, c);
        if (result >= 0) {//如果字段是哈希表中的一个新建字段，并且值设置成功，返回 1 。 如果哈希表中域字段已经存在且旧值已被新值覆盖，返回 0
            return true;
        }
        return false;
    }

    /****
     * set map 中元素
     * @param
     * @param key
     * @return
     */
    public static <T extends StoredObj> long hset(T obj, String key) {
        return BinaryJedisHash.hset(JedisConfig.DB_ID, key, key, obj);
    }

    /****
     * remove map 中元素
     * @param key
     * @param field
     * @return
     */
    public static <T extends StoredObj> long removeObjMap(String key, String field) {
        return BinaryJedisHash.hdel(JedisConfig.DB_ID, key, field);
    }

    /****
     * 取得map 中所有元素
     * @param clazz
     * @param key
     * @return
     */
    public static <T extends StoredObj> Map<String, T> getStoredObjsInMap(Class<T> clazz, String key) {
        return BinaryJedisHash.hgetAll(JedisConfig.DB_ID, key, clazz);
    }

    /****
     * 取得map 中所有元素
     * @param clazz
     * @param key
     * @return
     */
    public synchronized static <T extends StoredObj> T getStoredObjsInMap(Class<T> clazz, String key, String field) {
        return BinaryJedisHash.hget(JedisConfig.DB_ID, key, field, clazz);
    }

    /****
     * 获取所有的value对象
     * @param clazz
     * @param key
     * @return
     */
    public synchronized static <T extends StoredObj> List<T> getStoredObject(Class<T> clazz, String key) {
        return BinaryJedisHash.hvals(JedisConfig.DB_ID, key,clazz);
    }

    /****
     * 获取所有的field对象
     * @param key
     * @return
     */
    public synchronized static Set<String> hkeys(String key) {
        return BinaryJedisHash.hkeys(JedisConfig.DB_ID, key);
    }

    /****
     * 取得map 中元素
     * @param clazz
     * @param key
     * @return
     */
    public static <T extends StoredObj> T getStoredObjInMap(Class<T> clazz, String key, String field) {
        return BinaryJedisHash.hget(JedisConfig.DB_ID, key, field, clazz);
    }

    public static String hget(String key, String field) {
        return BinaryJedisHash.hget(JedisConfig.DB_ID, key, field);
    }

    /**
     * @param key
     * @param field
     * @param clazz
     * @param <T>
     * @return 无则返回null
     */
    public static <T extends StoredObj> T hget(String key, String field, Class<T> clazz) {
        return BinaryJedisHash.hget(JedisConfig.DB_ID, key, field, clazz);
    }

    /****
     * 删除map 中元素
     * @param key
     * @param field
     * @return
     */
    public static Boolean hdel(String key, String field) {
        Long num = BinaryJedisHash.hdel(JedisConfig.DB_ID, key, field);//被成功删除字段的数量，不包括被忽略的字段。
        if (num > 0) {
            return true;
        }
        return false;
    }

    /**
     * 将哈希表 key 中的域 field 的值设置为 value ，当且仅当域 field 不存在。
     * 若域 field 已经存在，该操作无效。
     * 如果 key 不存在，一个新哈希表被创建并执行 HSETNX 命令。
     *
     * @param key
     * @param field
     * @param value
     * @return 设置成功，返回 1 。 如果给定域已经存在且没有操作被执行，返回 0 。
     */
    public static boolean hsetnx(String key, String field, String value) {
        return BinaryJedisHash.hsetnx(JedisConfig.DB_ID, key, field, value) > 0;
    }

    public static List<String> hvals(String key) {
        //logger.info("hvals key:" + key);
        return BinaryJedisHash.hvals(JedisConfig.DB_ID, key);
    }

    public static <T extends StoredObj> Map<String, T> hgetAll(String key, Class<T> clazz) {
        return BinaryJedisHash.hgetAll(JedisConfig.DB_ID, key, clazz);
    }
    //end Hash////////////////////////////////////////////////////////////////////

    //List////////////////////////////////////////////////////////////////////////
    public static List<String> lrange(String key, int len) {
        return BinaryJedisList.lrangeString(JedisConfig.DB_ID, key, len);
    }

    public static boolean lpush(String key, String value) {
        long len = BinaryJedisList.lpushString(JedisConfig.DB_ID, key, value);
        if (len > 0) {
            return true;
        }
        return false;
    }

    public static List<String> lrangeStringAll(String key) {
        List<String> list = BinaryJedisList.lrangeStringAll(JedisConfig.DB_ID, key);
        return list;
    }

    public static <T extends StoredObj> boolean lpushObj(String key, T t) {
        long len = BinaryJedisList.lpush(JedisConfig.DB_ID, key, t);
        return len > 0;
    }

    public static <T extends StoredObj> List<T> lrangeObjAll(String key, Class<T> clazz) {
        List<T> list = BinaryJedisList.lrangeObjAll(JedisConfig.DB_ID, key, clazz);
        return list;
    }

    public static boolean ltrim(String key, int start, int stop) {
        return BinaryJedisList.ltrim(JedisConfig.DB_ID, key, start, stop);
    }
    //end List////////////////////////////////////////////////////////////////////

    //Set/////////////////////////////////////////////////////////////////////////

    /**
     * @param key
     * @param t
     * @param <T>
     * @return
     */
    public static <T extends StoredObj> boolean sadd(String key, T t) {
        long len = BinaryJedisSet.sadd(JedisConfig.DB_ID, key, t);
        return len > 0;
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public static <T extends StoredObj> boolean sadd(String key, String value) {
        long len = BinaryJedisSet.sadd(JedisConfig.DB_ID, key, value);
        return len > 0;
    }

    /**
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T extends StoredObj> Set<T> smembers(String key, Class<T> clazz) {
        return BinaryJedisSet.smembers(JedisConfig.DB_ID, key, clazz);
    }

    /**
     * @param key
     * @return
     */
    public static Set<String> smember(String key) {
        return BinaryJedisSet.smembers(JedisConfig.DB_ID, key);
    }

    /**
     * @param key
     * @param t
     * @param <T>
     * @return 被成功移除的元素的数量，不包括被忽略的元素。
     */
    public static <T extends StoredObj> boolean srem(String key, T t) {
        return BinaryJedisSet.srem(JedisConfig.DB_ID, key, t) > 0;
    }
    //end Set/////////////////////////////////////////////////////////////////////


    //Zset////////////////////////////////////////////////////////////////////////

    public static void zrem(String key, String member) {
        BinaryJedisZset.zrem(JedisConfig.DB_ID, key, member);
    }

    //end Zset////////////////////////////////////////////////////////////////////

    /**
     * 订阅
     *
     * @param jedisPubSub
     * @param channel
     * @return
     */
    public static boolean subscribe(JedisPubSub jedisPubSub, String channel) {
        return BinaryJedisGeneral.subscribe(jedisPubSub, channel);
    }

    /**
     * 发布
     *
     * @param msg
     * @param channel
     * @return
     */
    public static boolean publish(String msg, String channel) {
        return BinaryJedisGeneral.publish(msg, channel);
    }

    /**
     * 清理缓存数据
     *
     * @return
     */
    public static void clearDB() {
        BinaryJedisGeneral.cleanDB(JedisConfig.DB_ID);
    }

    /**
     * 停止服务
     */
    public static void shutdown() {
        BinaryJedisGeneral.shutdown();
    }
}
