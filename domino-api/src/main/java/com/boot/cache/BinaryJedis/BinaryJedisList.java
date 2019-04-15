package com.boot.cache.BinaryJedis;

import com.boot.cache.JedisFactory;
import com.boot.cache.StoredObj;
import com.boot.cache.StoredObjCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.JedisPool;

import java.util.LinkedList;
import java.util.List;

public class BinaryJedisList extends StoredObjCodec {
    private static final Logger log = LoggerFactory.getLogger(BinaryJedisList.class);

    /**
     * 查询一组对象
     *
     * @param key
     *            该组对象的key
     * @param start
     *            起始index
     * @param end
     *            结束index
     * @return 对象list
     */
    public static <T extends StoredObj> List<T> lrange( int dbId, String key, int start, int end, Class<T> clazz) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return decode(redis.lrange(encode(key), start, end), clazz);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }
    public static String lpopString(int dbId, String key) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);

            byte[] keyBytes = encode(key);
            byte[] result = redis.lpop(keyBytes);
            if (result != null)
                return toStr(result);
            else
                return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    /**
     * @param dbId
     * @param key
     * @param len
     * @return
     */
    public static List<String> lrangeString(int dbId, String key, int len) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);

            List<String> list = new LinkedList<>();
            int size = Math.min(len, (int) llen(dbId, key));

            List<byte[]> result =  redis.lrange(encode(key), 0, size-1);
            for (byte[] bs : result) {
                list.add(toStr(bs));
            }
            return list;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }


    /**
     * @param dbId
     * @param key
     * @return
     */
    public static List<String> lrangeStringAll(int dbId, String key) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);

            List<String> list = new LinkedList<>();
            int size = (int)llen(dbId, key);

            List<byte[]> result =  redis.lrange(encode(key), 0, size);
            for (byte[] bs : result) {
                list.add(toStr(bs));
            }
            return list;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    /**
     * @param dbId
     * Class<T> clazz) {
    JedisPool pool = JedisFactory.getPool();
    BinaryJedis redis = null;
    try {
    redis = pool.getResource();
    redis.select(dbId);
    return decode(redis.lpop(encode(key)), clazz);
     * @param key
     * @return
     */
    public static <T extends StoredObj> List<T> lrangeObjAll(int dbId, String key,Class<T> clazz) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);

            List<T> list = new LinkedList<>();
            int size = (int)llen(dbId, key);

            List<byte[]> result =  redis.lrange(encode(key), 0, size);
            for (byte[] bs : result) {
                list.add( decode(bs, clazz));
            }
            return list;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    /**
     * @param dbId
     * @param key
     * @param element
     * @param <T>
     */
    public static <T extends StoredObj> void rpush(int dbId, String key, T element) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            redis.rpush(encode(key), encode(element));
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    /**
     * 添加String到list头部
     *
     * @param dbId
     *            数据库下标
     * @param key
     *            list 的key
     * @param element
     *            追加的String
     */
    public static long lpushString(int dbId, String key, String element) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return redis.lpush(encode(key), encode(element));//执行 LPUSH 命令后，列表的长度。
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
        return 0;
    }

    /**
     * 添加String 到list 尾部
     *
     * @param key
     *            list 的key
     * @param element
     *            追加的String
     */
    public static void rpushString(int dbId, String key, String element) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            redis.rpush(encode(key), encode(element));
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    /**
     * 添加对象到list头部
     *
     * @param <T>
     *            对象类型
     * @param key
     *            list 的key
     * @param element
     *            追加的对象
     *  return 执行 LPUSH 命令后，列表的长度。
     */
    public static <T extends StoredObj> long lpush(int dbId, String key, T element) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return redis.lpush(encode(key), encode(element));
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
        return 0;
    }

    /**
     * 获取list 的长度
     *
     * @param key
     *            list 的key
     * @return list的长度
     */
    public static long llen(int dbId, String key) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return redis.llen(encode(key));
        } catch (Exception e) {
            log.error(e.getMessage());
            return -1;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    public static <T extends StoredObj> Object lpop(int dbId, String key, Class<T> clazz) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return decode(redis.lpop(encode(key)), clazz);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    public static <T extends StoredObj> Object rpop(int dbId, String key, Class<T> clazz) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return decode(redis.rpop(encode(key)), clazz);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    public static <T extends StoredObj> void lset(int dbId, String key, int index, T object) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            redis.lset(encode(key), index, encode(object));
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    public static <T extends StoredObj> Object lindex(int dbId, String key, int index, Class<T> clazz) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return decode(redis.lindex(encode(key), index), clazz);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    public static <T extends StoredObj> void lrem(int dbId, String key, int count, T object) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            redis.lrem(encode(key), count, encode(object));
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }
    public static boolean ltrim(int dbId, String key, int start, int stop){
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return BinaryJedisGeneral.OK.equals(redis.ltrim(encode(key), start, stop));
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
        return  false;
    }
}