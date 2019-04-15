package com.jule.db.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 缓存接口
 * @author ran
 */
public interface ICache {
	/**
	 * 初始化配置
	 * @param config
	 */
	public void init( CacheConfig config );
	
	/**
	 * 将一个或多个 member 元素及其 score 值加入到有序集 key 当中。
	 * 如果某个 member 已经是有序集的成员，那么更新这个 member 的 score 值，
	 * 并通过重新插入这个 member 元素，来保证该 member 在正确的位置上。
	 * @param key
	 * @param scoreMembers
	 */
	public void zadd( String key, Map<String, Double> scoreMembers );
	
	/**
	 * 为有序集 key 的成员 member 的 score 值加上增量 increment 。
	 * 可以通过传递一个负数值 increment ，让 score 减去相应的值，比如 ZINCRBY key -5 member ，就是让 member 的 score 值减去 5 。
	 * 当 key 不存在，或 member 不是 key 的成员时， ZINCRBY key increment member 等同于 ZADD key increment member 。
	 * 当 key 不是有序集类型时，返回一个错误。score 值可以是整数值或双精度浮点数。
	 * @param key
	 * @param score
	 * @param member
	 */
	public Double zincrby( String key, double score, String member );
	
	/**
	 * 返回有序集 key 中， score 值在 min 和 max 之间(默认包括 score 值等于 min 或 max )的成员的数量
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	public long zcount( String key, double min, double max );
	
	/**
	 * 返回有序集 key 的基数
	 * @param key
	 * @return
	 */
	public long zcard( String key );
	
	/**
	 * 返回有序集 key 中，指定区间内的成员。
	 * 其中成员的位置按 score 值递增(从小到大)来排序
	 * 下标参数 start 和 stop 都以 0 为底，也就是说，以 0 表示有序集第一个成员，以 1 表示有序集第二个成员，以此类推。
	 * 你也可以使用负数下标，以 -1 表示最后一个成员， -2 表示倒数第二个成员，以此类推。超出范围的下标并不会引起错误。
	 * 比如说，当 start 的值比有序集的最大下标还要大，或是 start > stop 时， ZRANGE 命令只是简单地返回一个空列表。
	 * 另一方面，假如 stop 参数的值比有序集的最大下标还要大，那么 Redis 将 stop 当作最大下标来处理。
	 * @param key
	 * @param start
	 * @param end
	 */
	public Set<String> zrange( String key, long start, long end );
	
	/**
	 * Return the all the elements in the sorted set at key with a score between 
	 * min and max (including elements with score equal to min or max).
	 * The elements having the same score are returned sorted lexicographically as ASCII strings
	 *  (this follows from a property of Redis sorted sets and does not involve further computation).
	 * Using the optional LIMIT it's possible to get only a range of the matching elements in an SQL-alike way.
	 *  Note that if offset is large the commands needs to traverse the list for offset elements and this adds up to the O(M) figure.
	 * The ZCOUNT command is similar to ZRANGEBYSCORE but instead of returning the actual elements in the specified interval,
	 *  it just returns the number of matching elements.
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 */
	public Set<String> zrangeByScore( final String key, final double min, final double max,
                                      final int offset, final int count );
	
	/**
	 * 返回有序集 key 中，指定区间内的成员。
	 * 其中成员的位置按 score 值递减(从大到小)来排列。
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public Set<String> zrevrange( String key, long start, long end );
	
	/**
	 * 返回有序集 key 中，成员 member 的 score 值
	 * 如果 member 元素不是有序集 key 的成员，或 key 不存在，返回 nil
	 * @param key
	 * @param member
	 * @return
	 */
	public Double zscore( String key, String member );
	
	/**
	 * 移除有序集 key 中的一个或多个成员，不存在的成员将被忽略。
	 * 当 key 存在但不是有序集类型时，返回一个错误。
	 * @param key
	 * @param members
	 */
	public void zrem( String key, String... members );
	
	/**
	 * 返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递增(从小到大)顺序排列。
	 * 排名以 0 为底，也就是说， score 值最小的成员排名为 0 。
	 * @param key
	 * @param member
	 * @return
	 */
	public Long zrank( String key, String member );
	
	/**
	 * 返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递减(从大到小)排序。
	 * 排名以 0 为底，也就是说， score 值最大的成员排名为 0 。
	 * 使用 ZRANK 命令可以获得成员按 score 值递增(从小到大)排列的排名。
	 * @param key
	 * @param member
	 * @return
	 */
	public Long zrevrank( String key, String member );
	
	/**
	 * 设置指定key的值
	 * @param key
	 * @param value
	 */
	public void set( String key, String value );
	
	/**
	 * 插入指定key的某一个值，可以设置过期时间
	 * @param key
	 * @param seconds 秒
	 * @param value
	 */
	public void setex( String key, int seconds, String value );
	
	/**
	 * 指定key设置过期时间
	 * @param key
	 * @param seconds 秒
	 */
	public void expire( String key, int seconds );
	
	/**
	 * 获取指定key某一个值
	 * @param key
	 * @return
	 */
	public String getStr( String key );
	
	/**
	 * 插入指定key的某一个值，可以设置过期时间
	 * @param key
	 * @param field
	 * @param info
	 */
	public <TEntity> void setexMemInfo( String key, int seconds, TEntity info );
	
	/**
	 * 插入指定key的某一个值，如果该值存在就替换值
	 * @param key
	 * @param field
	 * @param info
	 */
	public <TEntity> void setMemInfo( String key, String field, TEntity info );
	
	/**
	 * 插入指定key的某一个值，如果该值存在就替换值
	 * @param key
	 * @param field
	 * @param info
	 * @param ...clazzs 实体中包含的所有class
	 */
	public <TEntity> void setMemInfo( String key, String field, TEntity info, Class<?>... clazzs );
	
	/**
	 * 从左入队
	 * @param key
	 * @param info
	 */
	public <TEntity> void lpushMemInfo( String key, TEntity info );
	
	/**
	 * 堵塞从右出队
	 * @param key
	 * @param timeout
	 * @param clazz
	 */
	public <TEntity> TEntity brpopMemInfo( String key, int timeout, Class<TEntity> clazz );
	
	/**
	 * 从右出队
	 * @param key
	 * @param timeout
	 * @param clazz
	 */
	public <TEntity> TEntity rpopMemInfo( String key, Class<TEntity> clazz );
	
	/**
	 * 返回一个指定key的对象
	 * @param key
	 * @param field
	 * @param clazz
	 * @return
	 */
	public <TEntity> TEntity getMemInfo( String key, String field, Class<TEntity> clazz );
	
	/**
	 * 返回一个指定key的对象
	 * @param key
	 * @param field
	 * @param clazz
	 * @param ...clazzs 实体中包含的所有class
	 * @return
	 */
	public <TEntity> TEntity getMemInfo( String key, String field, Class<TEntity> clazz, Class<?>... clazzs );
	
	/**
	 * 获取一个指定key的对象
	 * @param key
	 * @param field
	 */
	public <TEntity> TEntity getMemInfo( String key, Class<TEntity> clazz );
	
	/**
	 * 设置list列表,会替换原有的列表
	 * @param key
	 * @param memInfos
	 * @param clazz
	 */
	public <TEntity> void setMemList( String key, List<TEntity> memInfos, Class<TEntity> clazz, int seconds );
	
	/**
	 * 设置list列表,只会原有的列表中追加数据
	 * @param key
	 * @param memInfos
	 * @param clazz
	 */
	public <TEntity> void putMemList( String key, List<TEntity> memInfos, Class<TEntity> clazz, int seconds );
	
	/**
	 * 设置list列表,只会原有的列表中头部插入数据
	 * @param key
	 * @param memInfos
	 * @param clazz
	 */
	public <TEntity> void putLMemList( String key, List<TEntity> memInfos, Class<TEntity> clazz, int seconds, int limit );
	
	/**
	 * 截取名称为key的list，保留start至end之间的元素
	 * @param key
	 * @param start
	 * @param end
	 */
	public void listTrim( String key, int start, int end );
	
	/**
	 * 设置list列表
	 * @param key
	 * @param memInfos
	 * @param clazz
	 */
	public void blpopMemList( String key, int seconds );
	
	/**
	 * 设置到缓存中一个map到集合中,会替换以前的集合
	 * @param key
	 * @param list
	 * @param clazz
	 */
	public <TEntity> void setMemMap( String key, Map<String, TEntity> memInfos, Class<TEntity> clazz );
	
	/**
	 * 设置到缓存中一个列表到集合中,会追加到集合中
	 * @param key
	 * @param list
	 * @param clazz
	 */
	public <TEntity> void putMemMap( String key, Map<String, TEntity> memInfos, Class<TEntity> clazz );
	
	/**
	 * 返回缓存中一个list列表到集合中
	 * @param key
	 * @param clazz
	 * @return
	 */
	public <TEntity> List<TEntity> getMemList( String key, Class<TEntity> clazz );
	
	/**
	 * 获取列表长度
	 * @return
	 */
	public long getListLength( String key );
	
	/**
	 * 返回缓存中一个列表到集合中
	 * @param key
	 * @param clazz
	 * @return
	 */
	public <TEntity> Map<String, TEntity> getMemMap( String key, Class<TEntity> clazz );
	
	/**
	 * 删除一个指定key的对象
	 * @param key
	 * @param field
	 */
	public void removeMemInfo( String key, String field );
	
	/**
	 * 指定key的对象
	 * @param key
	 */
	public Set<byte[]> getKeys( String pattern );
	
	/**
	 * 删除一个指定key列表的对象
	 * @param keyList
	 */
	public void removeCacheByKeyList( List<String> keyList );
	
	/**
	 * 删除一个指定key的对象
	 * @param key
	 */
	public void removeCacheByKey( String key );
	
	/**
	 * 删除一个指定key的对象
	 * @param key
	 */
	public void removeSortSetByKey( String key );
	
	/**
	 * 清除指定key的数据库层缓存
	 * @param key
	 */
	public void clearByKey( byte[] key );
	
	/**
	 * 获取锁
	 * @param clazz
	 * @return
	 */
	public boolean acquireLock( String key, String param, int expireTime );
	
	/**
	 * 释放锁
	 * @param clazz
	 */
	public void releaseLock( String key );
	
	/**
	 * 自动加一
	 * @param key
	 */
	public long incr( String key );
	
	/**
	 * 自动加某一值
	 * @param key
	 * @param integer
	 */
	public long incrBy( String key, long integer );
	
	/**
	 * 自动减一
	 * @param key
	 */
	public long decr( String key );
	
	/**
	 * 自动减某一值
	 * @param key
	 * @param integer
	 */
	public long decrBy( String key, long integer );
}
