package com.jule.core.jedis.BinaryJedis;

import com.jule.core.jedis.JedisFactory;
import com.jule.core.jedis.StoredObjCodec;
import com.jule.core.jedis.codec.StoreServiceCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class BinaryJedisZset extends StoredObjCodec {
    private static final Logger log = LoggerFactory.getLogger(BinaryJedisZset.class);

    /**
     * Redis Zscore 命令返回有序集中，成员的分数值。 如果成员元素不是有序集 key 的成员，或 key 不存在，返回 nil 。
     * @param dbId
     * @param key
     * @param member
     * @return 成员的分数值，以字符串形式表示。
     */
    public static long zscore(int dbId, String key , String member)   {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            double res = redis.zscore(encode(key), encode(member));
            return (long) res;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return 0;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    /**
     * 取得有序数组中 某个成员  排名
     * @param dbId
     * @param key
     * @param member
     * @return
     */
    public static long zrank(int dbId, String key , String member)   {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            long v = redis.zrank(encode(key), encode(member));
            //double res = redis.zscore(encode(key), encode(member));
            return v;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return 0;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    /**
     * 取得数量
     * @param dbId
     * @param key
     * @return
     */
    public static long zcard(int dbId, String key ) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            long res = redis.zcard(encode(key));;
            return res;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return -1;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    /**
     * 删除 member
     * @param dbId
     * @param key
     * @param member
     * @return
     */
    public static long zrem(int dbId, String key , String member)  {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            double res = redis.zrem(encode(key), encode(member));
            return (long) res;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return -1;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    /**
     * Redis Zadd 命令用于将一个或多个成员元素及其分数值加入到有序集当中。
     * 如果某个成员已经是有序集的成员，那么更新这个成员的分数值，并通过重新插入这个成员元素，来保证该成员在正确的位置上。
     * 分数值可以是整数值或双精度浮点数。
     * 如果有序集合 key 不存在，则创建一个空的有序集并执行 ZADD 操作。
     * 当 key 存在但不是有序集类型时，返回一个错误。
     * 注意： 在 Redis 2.4 版本以前， ZADD 每次只能添加一个元素。
     * @param dbId
     * @param key
     * @param score
     * @param value
     * @return 被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员。
     */
    public static long zadd(int dbId, String key,long score,String value) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            return redis.zadd(encode(key), score, encode(value));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return -1;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    /**
     * 取排名列表
     * @param dbId
     * @param key
     * @param start
     * @param end
     * @return
     */
    public static List<String> zrange(int dbId, String key,int start,int end) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        List<String>  tmp = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            Set<byte[]> eles = redis.zrange(encode(key), start, end);

            tmp = new ArrayList<String>(eles.size());
            for (byte[] bs : eles) {
                tmp.add(toStr(bs));
            }
            return tmp;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return null;
        } finally {
            if(redis != null) {
                redis.close();
            }
        }
    }

    //取排名列表
    public static List<String> zrangeWithScores(int dbId, String key, int start, int end) {
        JedisPool pool = JedisFactory.getPool();
        BinaryJedis redis = null;
        List<String>  tmp = null;
        try {
            redis = pool.getResource();
            redis.select(dbId);
            Set<Tuple> eles = redis.zrangeWithScores(encode(key), start, end);

            tmp = new ArrayList<>(eles.size());
            for (Tuple bs : eles) {
                tmp.add( bs.getElement() +":" + (long)bs.getScore() );
            }
            return tmp;
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
