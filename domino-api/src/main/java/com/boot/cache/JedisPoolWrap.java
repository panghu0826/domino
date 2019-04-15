package com.boot.cache;

import com.boot.config.JedisConfig;
import com.google.protobuf.MessageLite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 带有Jedis访问功能的 DAO高度统一抽象类
 *
 * @author Faustin, Xujian
 */
public class JedisPoolWrap {
    private static final Logger log = LoggerFactory.getLogger(JedisPoolWrap.class);

    private static class SingletonHolder {
        protected static final JedisPoolWrap instance = new JedisPoolWrap();
    }

    public static final JedisPoolWrap getInstance() {
        return SingletonHolder.instance;
    }

    //## ------------------------------- String类型操作封装 ------------------------------------
    public boolean set(String key, String value, int timeoutSeconds) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return false;
        Jedis jedis = pool.getResource();
        try {
            jedis.select(JedisConfig.getDatabase());
            jedis.set(key, value);
            if (timeoutSeconds > 0) jedis.expire(key, timeoutSeconds);
        } catch (Exception e) {
            return false;
        } finally {
            jedis.close();
        }
        return true;
    }

    //## ------------------------------- byts[]类型操作封装 ------------------------------------
    public boolean hSet(String key, String field, MessageLite value) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return false;
        Jedis jedis = pool.getResource();
        try {
            jedis.select(JedisConfig.getDatabase());
            jedis.hset(key.getBytes("utf-8"), field.getBytes("utf-8"), value.toByteArray());
        } catch (Exception e) {
            return false;
        } finally {
            jedis.close();
        }
        return true;
    }

    //## ------------------------------- String类型操作封装 ------------------------------------
    public boolean hSet(String key, String field, String value) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return false;
        Jedis jedis = pool.getResource();
        try {
            jedis.select(JedisConfig.getDatabase());
            jedis.hset(key, field, value);
        } catch (Exception e) {
            return false;
        } finally {
            jedis.close();
        }
        return true;
    }

    //## ------------------------------- String类型操作封装 ------------------------------------
    public byte[] hGet(String key, String field) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return null;
        byte[] ret = null;
        Jedis jedis = pool.getResource();
        try {
            jedis.select(JedisConfig.getDatabase());
            ret = jedis.hget(key.getBytes("utf-8"), field.getBytes("utf-8"));
        } catch (Exception e) {
        } finally {
            jedis.close();
        }
        return ret;
    }

    //## ------------------------------- 获取hget的String返回值 ------------------------------------
    public String hGetString(String key, String field) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return null;
        String ret = null;
        Jedis jedis = pool.getResource();
        try {
            jedis.select(JedisConfig.getDatabase());
            ret = jedis.hget(key, field);
        } catch (Exception e) {
        } finally {
            jedis.close();
        }
        return ret;
    }


    //## ------------------------------- 获取所有 ------------------------------------
    public Map<String, String> hGetAll(String key) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return null;
        Map<String, String> ret = null;
        Jedis jedis = pool.getResource();
        try {
            jedis.select(JedisConfig.getDatabase());
            ret = jedis.hgetAll(key);
        } catch (Exception e) {
        } finally {
            jedis.close();
        }
        return ret;
    }

    /**
     * 批量获取
     *
     * @param key
     * @param parma
     * @return
     */
    public List<byte[]> hMget(String key, String[] parma) {

        JedisPool pool = JedisFactory.getPool();
        if (pool == null) pool = JedisFactory.getPool();
        if (pool == null) return null;
        Jedis jedis = pool.getResource();
        List<byte[]> ret = null;
        try {
            jedis.select(JedisConfig.getDatabase());
            byte[][] p = new byte[parma.length][];
            for (int i = 0; i < parma.length; i++) {
                p[i] = parma[i].getBytes("utf-8");
            }
            ret = jedis.hmget(key.getBytes("utf-8"), p);
        } catch (Exception e) {
        } finally {
            jedis.close();
        }
        return ret;
    }

    /**
     * 批量获取
     *
     * @param key
     * @param parma
     * @return
     */
    public List<String> hMget2(String key, List<String> parma) {

        JedisPool pool = JedisFactory.getPool();
        if (pool == null) pool = JedisFactory.getPool();
        if (pool == null) return null;
        Jedis jedis = pool.getResource();
        List<String> ret = new ArrayList<>();
        try {
            jedis.select(JedisConfig.getDatabase());
            byte[][] p = new byte[parma.size()][];
            for (int i = 0; i < parma.size(); i++) {
                p[i] = parma.get(i).getBytes("utf-8");
            }
            List<byte[]> __ret = jedis.hmget(key.getBytes("utf-8"), p);
            if (__ret != null && __ret.size() > 0) {
                __ret.forEach(bytes -> ret.add(bytes == null ? null : new String(bytes, Charset.forName("utf-8"))));
            }
        } catch (Exception e) {
        } finally {
            jedis.close();
        }
        return ret;
    }

    /**
     * 基于prococbuff的封装
     *
     * @param key
     * @param value
     * @param timeoutSeconds
     * @return
     */
    public boolean set(String key, MessageLite value, int timeoutSeconds) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return false;
        Jedis jedis = pool.getResource();
        try {
            jedis.select(JedisConfig.getDatabase());
            jedis.set(key, value.toByteString().toStringUtf8());
            if (timeoutSeconds > 0) jedis.expire(key, timeoutSeconds);
        } catch (Exception e) {
            return false;
        } finally {
            jedis.close();
        }
        return true;
    }

    public boolean append(String key, String value, int timeoutSeconds) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return false;
        Jedis jedis = pool.getResource();
        try {
            jedis.select(JedisConfig.getDatabase());
            jedis.append(key, value);
            if (timeoutSeconds > 0) jedis.expire(key, timeoutSeconds);
        } catch (Exception e) {
            return false;
        } finally {
            jedis.close();
        }
        return true;
    }

    public boolean set(String key, long value, int timeoutSeconds) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return false;
        Jedis jedis = pool.getResource();
        try {
            jedis.select(JedisConfig.getDatabase());
            jedis.incrBy(key, value);
            if (timeoutSeconds > 0) jedis.expire(key, timeoutSeconds);
        } catch (Exception e) {
            return false;
        } finally {
            jedis.close();
        }
        return true;
    }

    public boolean incr(String key, long delta) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return false;
        Jedis jedis = pool.getResource();
        try {
            jedis.select(JedisConfig.getDatabase());
            jedis.incrBy(key, delta);
        } catch (Exception e) {
            return false;
        } finally {
            jedis.close();
        }
        return true;
    }

    /**
     * 批量赋值 http://redis.readthedocs.org/en/2.4/string.html#mget
     *
     * @param timeoutSeconds
     * @param keysvalues
     * @return
     */
    public boolean mset(int timeoutSeconds, String... keysvalues) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return false;
        Jedis jedis = pool.getResource();
        try {
            jedis.select(JedisConfig.getDatabase());
            jedis.mset(keysvalues);
            if (timeoutSeconds > 0) {
                int c = 0;
                for (String k : keysvalues) {
                    if ((c % 2) == 0)
                        jedis.expire(k, timeoutSeconds);
                    c++;
                }
            }
        } catch (Exception e) {
            return false;
        } finally {
            jedis.close();
        }
        return true;
    }


    public String get(String key) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) pool = JedisFactory.getPool();
        if (pool == null) return null;
        Jedis jedis = pool.getResource();
        String ret = null;
        try {
            jedis.select(JedisConfig.getDatabase());
            ret = jedis.get(key);
        } catch (Exception e) {
        } finally {
            jedis.close();
        }
        return ret;
    }

    /**
     * 一次性批量多个key读取
     *
     * @param keys
     * @return
     */
    public List<String> mget(String... keys) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) pool = JedisFactory.getPool();
        if (pool == null) return null;
        Jedis jedis = pool.getResource();
        List<String> ret = null;
        try {
            jedis.select(JedisConfig.getDatabase());
            ret = jedis.mget(keys);
        } catch (Exception e) {
        } finally {
            jedis.close();
        }
        return ret;
    }

    //## ------------------------------- 列表操作封装 ------------------------------------
    public int lpush(String key, String... values) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return -1;
        Jedis jedis = pool.getResource();
        long c = 0;
        try {
            jedis.select(JedisConfig.getDatabase());
            c = jedis.lpush(key, values);
        } catch (Exception e) {
            return -2;
        } finally {
            jedis.close();
        }
        return (int) c;
    }

    public int lpushX(String key, String values) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return -1;
        Jedis jedis = pool.getResource();
        long c = 0;
        try {
            jedis.select(JedisConfig.getDatabase());
            c = jedis.lpushx(key, values);
        } catch (Exception e) {
            return -2;
        } finally {
            jedis.close();
        }
        return (int) c;
    }

    public int rpush(String key, String... values) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return -1;
        Jedis jedis = pool.getResource();
        long c = 0;
        try {
            jedis.select(JedisConfig.getDatabase());
            c = jedis.rpush(key, values);
        } catch (Exception e) {
            return -2;
        } finally {
            jedis.close();
        }
        return (int) c;
    }

    /**
     * 从末尾删除元素
     *
     * @param key
     * @return
     */

    public String rpop(String key) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return "error";
        Jedis jedis = pool.getResource();
        String c = "";
        try {
            jedis.select(JedisConfig.getDatabase());
            c = jedis.rpop(key);
        } catch (Exception e) {
            return "error";
        } finally {
            jedis.close();
        }
        return c;
    }

    /**
     * 下标(start, end)参数start和stop都以0为底，也就是说，以0表示列表的第一个元素，以1表示列表的第二个元素，以此类推。
     * 也可以使用负数下标，以-1表示列表的最后一个元素，-2表示列表的倒数第二个元素，以此类推。
     */
    public List<String> range(String key, int start, int end) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return null;
        Jedis jedis = pool.getResource();
        List<String> ss = null;
        try {
            jedis.select(JedisConfig.getDatabase());
            ss = jedis.lrange(key, start, end);
        } catch (Exception e) {
            return null;
        } finally {
            jedis.close();
        }
        return ss;
    }


    //## ------------------------------- SortedSet 操作封装 ------------------------------------
    public int zadd(String key, long score, String value, int timeoutSeconds) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return -1;
        Jedis jedis = pool.getResource();
        long c = 0;
        try {
            jedis.select(JedisConfig.getDatabase());
            c = jedis.zadd(key, (double) score, value);
            if (timeoutSeconds > 0) expire(key, timeoutSeconds);
        } catch (Exception e) {
            return -2;
        } finally {
            jedis.close();
        }
        return (int) c;
    }

    public int zdel(String key, String value) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return -1;
        Jedis jedis = pool.getResource();
        long c = 0;
        try {
            jedis.select(JedisConfig.getDatabase());
            c = jedis.zrem(key, value);
        } catch (Exception e) {
            return -2;
        } finally {
            jedis.close();
        }
        return (int) c;
    }

    /**
     * 倒序列表
     * 下标(start, end)参数start和stop都以0为底，也就是说，以0表示列表的第一个元素，以1表示列表的第二个元素，以此类推。
     * 也可以使用负数下标，以-1表示列表的最后一个元素，-2表示列表的倒数第二个元素，以此类推。
     */
    public Set<String> zRevRange(String key, int start, int end) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return null;
        Jedis jedis = pool.getResource();
        Set<String> ss = null;
        try {
            jedis.select(JedisConfig.getDatabase());
            ss = jedis.zrevrange(key, start, end);
        } catch (Exception e) {
            return null;
        } finally {
            jedis.close();
        }
        return ss;
    }

    /**
     * 正序列表
     */
    public Set<String> zrange(String key, int start, int end) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return null;
        Jedis jedis = pool.getResource();
        Set<String> ss = null;
        try {
            jedis.select(JedisConfig.getDatabase());
            ss = jedis.zrange(key, start, end);
        } catch (Exception e) {
            return null;
        } finally {
            jedis.close();
        }
        return ss;
    }

    /**
     * 获得反向排名, 从 1 开始
     */
    public int zRevRank(String key, String value) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return -1;
        Jedis jedis = pool.getResource();
        long c = -1;
        try {
            jedis.select(JedisConfig.getDatabase());
            c = jedis.zrevrank(key, value) + 1; //redis的rank从0开始
        } catch (Exception e) {
            return -2;
        } finally {
            jedis.close();
        }
        return (int) c;
    }

    /**
     * 返回集合中score 在给定区间的元素
     */
    public Set<String> zrangebyscore(String key, double min, double max) {

        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return null;
        Jedis jedis = pool.getResource();

        Set<String> set = null;
        try {
            jedis.select(JedisConfig.getDatabase());
            set = jedis.zrangeByScore(key, min, max);
        } catch (Exception e) {
            return null;
        } finally {
            jedis.close();
        }

        return set;
    }

    public Long zscore(String key, String value) {

        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return null;
        Jedis jedis = pool.getResource();

        Double time = new Double(0.000);
        try {
            jedis.select(JedisConfig.getDatabase());
            time = jedis.zscore(key, value);
            if (time == null) return null;
        } catch (Exception e) {
            return null;
        } finally {
            jedis.close();
        }

        return new Double(time).longValue();
    }


    //## ------------------------------- 通用操作封装 ------------------------------------
    public boolean expire(String key, int timeoutSeconds) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return false;
        Jedis jedis = pool.getResource();
        try {
            jedis.select(JedisConfig.getDatabase());
            jedis.expire(key, timeoutSeconds);
        } catch (Exception e) {
            return false;
        } finally {
            jedis.close();
        }
        return true;
    }

    public boolean exist(String key) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return false;
        Jedis jedis = pool.getResource();
        try {
            jedis.select(JedisConfig.getDatabase());
            return jedis.exists(key);
        } catch (Exception e) {
            return false;
        } finally {
            jedis.close();
        }
    }

    public boolean del(String... keys) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return false;
        Jedis jedis = pool.getResource();
        try {
            jedis.select(JedisConfig.getDatabase());
            jedis.del(keys);
        } catch (Exception e) {
            return false;
        } finally {
            jedis.close();
        }
        return true;
    }

    /**
     * 订阅消息
     */
    public boolean subscribe(JedisPubSub jedisPubSub, String... channels) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return false;
        Jedis jedis = pool.getResource();
        try {
            jedis.select(JedisConfig.getDatabase());
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
    public boolean pub(String msg, String channel) {
        JedisPool pool = JedisFactory.getPool();
        if (pool == null) return false;
        Jedis jedis = pool.getResource();
        try {
            jedis.select(JedisConfig.getDatabase());
            jedis.publish(channel, msg);
        } catch (Exception e) {
            return false;
        } finally {
            jedis.close();
        }
        return true;
    }

    private JedisPoolWrap() {
    }

}
