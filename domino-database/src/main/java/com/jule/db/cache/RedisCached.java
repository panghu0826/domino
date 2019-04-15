package com.jule.db.cache;

import com.jule.core.common.log.LoggerUtils;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * redis缓存实现类
 * @author ran
 */
public class RedisCached implements ICache{
	/** 缓存池对象,线程安全的，使用静态化存储 **/
	private static JedisPool pool = null;
	/** 缓存配置 **/
	private CacheConfig cacheConfig = null;

	public void init(CacheConfig config){
		cacheConfig = config;
		JedisPoolConfig poolConfig = new JedisPoolConfig();  
		poolConfig.setMaxTotal(cacheConfig.getPoolMaxTotal());
		poolConfig.setMaxIdle(cacheConfig.getPoolMaxIdle());
		poolConfig.setMaxWaitMillis(cacheConfig.getPoolMsaxWait());
		//创建池子对象
	    pool = new JedisPool(poolConfig, cacheConfig.getIp(),cacheConfig.getPort()); 
	}
	
	public Jedis  getJedis() {
		// 从池中获取一个Jedis对象
		Jedis jedis = pool.getResource();
		if (cacheConfig.getRedisPwd() != null
				&& !"".equals(cacheConfig.getRedisPwd())) {
			jedis.auth(cacheConfig.getRedisPwd());
		}
		jedis.select(cacheConfig.getRedisDbIndex());
		return jedis;
	}
	
	@Override
	public void zadd(String key, Map<String, Double> scoreMembers){
		if(StringUtils.isEmpty(key)){
			LoggerUtils.error.error("key is null");
			return;
		}
		if(scoreMembers==null || scoreMembers.isEmpty()){
			LoggerUtils.error.error("scoreMembers is null");
			return;
		}
		Jedis jedis=getJedis();
		try {
			jedis.zadd(key, scoreMembers);
		} catch (Exception e) {
			LoggerUtils.error.error("redis zadd error:", e);
		}finally{
        	jedis.close();
        }
	}
	
	@Override
	public Double zincrby(String key, double score, String member){
		Double res = -1d;
		if(StringUtils.isEmpty(key) || StringUtils.isEmpty(member)){
			LoggerUtils.error.error("key is null or member");
			return res;
		}
		
		Jedis jedis=getJedis();
		try {
			res = jedis.zincrby(key, score, member);
		} catch (Exception e) {
			LoggerUtils.error.error("redis zincrby error:", e);
		}finally{
        	jedis.close();
        }
		
		return res;
	}
	
	@Override
	public long zcard(String key){
		if(StringUtils.isEmpty(key)){
			LoggerUtils.error.error("key is null");
			return 0;
		}
		Long count = 0l;
		Jedis jedis=getJedis();
		try {
			count = jedis.zcard(key);
		} catch (Exception e) {
			LoggerUtils.error.error("redis zcard error:", e);
		}finally{
        	jedis.close();
        }
		
		return count;
	}
	
	@Override
	public long zcount(String key,double min, double max){
		if(StringUtils.isEmpty(key)){
			LoggerUtils.error.error("key is null");
			return 0;
		}
		Long count = 0l;
		Jedis jedis=getJedis();
		try {
			count = jedis.zcount(key, min, max);
		} catch (Exception e) {
			LoggerUtils.error.error("redis zcount error:", e);
		}finally{
        	jedis.close();
        }
		
		return count;
	}
	
	@Override
	public Set<String> zrange(String key, long start, long end){
		if(StringUtils.isEmpty(key)){
			LoggerUtils.error.error("key is null");
			return null;
		}
		Set<String> set = null;
		
		Jedis jedis=getJedis();
		try {
			set = jedis.zrange(key, start, end);
		} catch (Exception e) {
			LoggerUtils.error.error("redis zrange error:", e);
		}finally{
        	jedis.close();
        }
		
		return set;
	}
	
	@Override
	public Set<String> zrangeByScore(final String key, final double min, final double max,
		      final int offset, final int count){
		if(StringUtils.isEmpty(key)){
			LoggerUtils.error.error("key is null");
			return null;
		}
		Set<String> set = null;
		
		Jedis jedis=getJedis();
		try {
			set = jedis.zrangeByScore(key, min, max, offset, count);
		} catch (Exception e) {
			LoggerUtils.error.error("redis zrangeByScore error:", e);
		}finally{
        	jedis.close();
        }
		
		return set;
	}
	
	@Override
	public Set<String> zrevrange(String key, long start, long end){
		if(StringUtils.isEmpty(key)){
			LoggerUtils.error.error("key is null");
			return null;
		}
		Set<String> set = null;
		
		Jedis jedis=getJedis();
		try {
			set = jedis.zrevrange(key, start, end);
		} catch (Exception e) {
			LoggerUtils.error.error("redis zrevrange error:", e);
		}finally{
        	jedis.close();
        }
		
		return set;
	}
	
	@Override
	public Double zscore(String key, String member){
		if(StringUtils.isEmpty(key)){
			LoggerUtils.error.error("key is null");
			return null;
		}
		Double score = null;
		
		Jedis jedis=getJedis();
		try {
			score = jedis.zscore(key, member);
		} catch (Exception e) {
			LoggerUtils.error.error("redis zscore error:", e);
		}finally{
        	jedis.close();
        }
		
		return score;
	}
	
	@Override
	public void zrem(String key,String... members){
		if(StringUtils.isEmpty(key)){
			LoggerUtils.error.error("key is null");
			return;
		}
		if(members == null){
			LoggerUtils.error.error("members is null");
			return;
		}
		
		Jedis jedis=getJedis();
		try {
			jedis.zrem(key, members);
		} catch (Exception e) {
			LoggerUtils.error.error("redis zrem error:", e);
		}finally{
        	jedis.close();
        }
	}
	
	@Override
	public Long zrank(String key,String member){
		if(StringUtils.isEmpty(key)){
			LoggerUtils.error.error("key is null");
			return null;
		}
		if(StringUtils.isEmpty(member)){
			LoggerUtils.error.error("member is null");
			return null;
		}
		
		Long rank = null;
		Jedis jedis=getJedis();
		try {
			rank = jedis.zrank(key, member);
		} catch (Exception e) {
			LoggerUtils.error.error("redis zrem error:", e);
		}finally{
        	jedis.close();
        }
		
		return rank;
	}
	
	@Override
	public Long zrevrank(String key,String member){
		if(StringUtils.isEmpty(key)){
			LoggerUtils.error.error("key is null");
			return null;
		}
		if(StringUtils.isEmpty(member)){
			LoggerUtils.error.error("member is null");
			return null;
		}
		
		Long rank = null;
		Jedis jedis=getJedis();
		try {
			rank = jedis.zrevrank(key, member);
		} catch (Exception e) {
			LoggerUtils.error.error("redis zrevrank error:", e);
		}finally{
        	jedis.close();
        }
		
		return rank;
	}
	
	@Override
	public void set(String key, String value){
		if(StringUtils.isEmpty(key)){
			LoggerUtils.error.error("key is null");
			return;
		}
		if(StringUtils.isEmpty(value)){
			LoggerUtils.error.error("value is null");
			return;
		}
		Jedis jedis=getJedis();
		try {
			jedis.set(key.getBytes("UTF-8"), value.getBytes("UTF-8"));
		} catch (Exception e) {
			LoggerUtils.error.error("redis setex error:", e);
		}finally{
        	jedis.close();
        }
	}
    
    /**
	 * 插入指定key的某一个值，可以设置过期时间
	 * @param key
	 * @param seconds 秒
	 * @param value
	 */
	public void setex(String key,int seconds,String value){
		if(key == null || "".equals(key)){
			LoggerUtils.error.error("key is null");
			return;
		}
		if(value == null || "".equals(value)){
			LoggerUtils.error.error("value is null");
			return;
		}
		
		Jedis jedis=getJedis();
		try {
			jedis.setex(key.getBytes("UTF-8"), seconds, value.getBytes("UTF-8"));
		} catch (Exception e) {
			LoggerUtils.error.error("redis setex error:", e);
		}finally{
        	jedis.close();
        }
	}
	
	/**
	 * 指定key设置过期时间
	 * @param key
	 * @param seconds 秒
	 */
	public void expire(String key,int seconds){
		if(key == null || "".equals(key)){
			LoggerUtils.error.error("key is null");
			return;
		}
		
		Jedis jedis=getJedis();
		try {
			jedis.expire(key.getBytes("UTF-8"), seconds);
		} catch (Exception e) {
			LoggerUtils.error.error("redis expire error:", e);
		}finally{
			jedis.close();
		}
	}
	
	/**
	 * 通过key获取字符串
	 * @param key
	 * @return
	 */
	public String getStr(String key){
		if(key == null || "".equals(key)){
			LoggerUtils.error.error("key is null");
			return null;
		}
		
		Jedis jedis=getJedis();
		byte[] bytes;
		try {
			bytes = jedis.get(key.getBytes("UTF-8"));
			if(bytes == null || bytes.length == 0){
				return null;
			}
			return new String(bytes,"UTF-8");
		} catch (Exception e) {
			LoggerUtils.error.error("getStr error:", e);
		}finally {
			jedis.close();
		}
		
		return null;
	}
	
	/**
	 * 插入指定key的某一个值，可以设置过期时间
	 * @param key
	 * @param field
	 * @param info
	 */
	public <TEntity> void setexMemInfo(String key,int seconds,TEntity info){
		CommMem.OBJ.msgPackRegister(info.getClass());
		byte[] bytes = CommMem.OBJ.transEntity2MsgPack(info);
		Jedis jedis=getJedis();
		try {
			jedis.setex(key.getBytes("UTF-8"),seconds, bytes);
		} catch (Exception e) {
			LoggerUtils.error.error("redis write error:", e);
		}finally{
        	jedis.close();
        }
	}
	
	/**
	 * 插入指定key的某一个值，如果该值存在就替换值
	 * @param key
	 * @param field
	 * @param info
	 */
	public <TEntity> void setMemInfo(String key,String field,TEntity info){
		CommMem.OBJ.msgPackRegister(info.getClass());
		byte[] bytes = CommMem.OBJ.transEntity2MsgPack(info);
		Jedis jedis=getJedis();
		try {
			jedis.hset(key.getBytes("UTF-8"),field.getBytes("UTF-8"), bytes);
		} catch (Exception e) {
			LoggerUtils.error.error("redis write error:", e);
		}finally{
        	jedis.close();
        }
	}
	
	/**
	 * 插入指定key的某一个值，如果该值存在就替换值
	 * @param key
	 * @param field
	 * @param info
	 * @param ...clazzs 实体中包含的所有class
	 */
	public <TEntity> void setMemInfo(String key, String field,
			TEntity info, Class<?>... clazzs) {
		//添加其他类型
		for(Class<?> c:clazzs){
			CommMem.OBJ.msgPackRegister(c);
		}
		CommMem.OBJ.msgPackRegister(info.getClass());
		
		byte[] bytes = CommMem.OBJ.transEntity2MsgPack(info);
		Jedis jedis=getJedis();
		try {
			jedis.hset(key.getBytes("UTF-8"),field.getBytes("UTF-8"), bytes);
		} catch (Exception e) {
			LoggerUtils.error.error("redis write error:", e);
		}finally{
        	jedis.close();
        }
	}
	/**
	 * 从左入队
	 * @param key
	 * @param info
	 */
	public <TEntity> void lpushMemInfo(String key,TEntity info){
		CommMem.OBJ.msgPackRegister(info.getClass());
		byte[] bytes = CommMem.OBJ.transEntity2MsgPack(info);
		Jedis jedis=getJedis();
		try {
			jedis.lpush(key.getBytes("UTF-8"),bytes);
		} catch (Exception e) {
			LoggerUtils.error.error("redis lpush error:", e);
		}finally{
        	jedis.close();
        }
	}
	/**
	 * 堵塞从右出队
	 * @param key
	 * @param timeout
	 * @param clazz
	 */
	public <TEntity> TEntity brpopMemInfo(String key, int timeout, Class<TEntity> clazz){
		TEntity back=null;
		Jedis jedis=getJedis();
		try {
			CommMem.OBJ.msgPackRegister(clazz);
			//return both the unblocking key and the popped value
			List<byte[]> bytes = jedis.brpop(timeout,key.getBytes("UTF-8"));
			if(null == bytes){
				return null;
			}
			back = CommMem.OBJ.transMsgPack2Entity(bytes.get(1),clazz,key.getBytes("UTF-8"));
		} catch (Exception e) {
			LoggerUtils.error.error("redis brpop error:", e);
		}finally{
			jedis.close();
		}
		return back;
	}
	/**
	 * 从右出队
	 * @param key
	 * @param timeout
	 * @param clazz
	 */
	public <TEntity> TEntity rpopMemInfo(String key, Class<TEntity> clazz){
		TEntity back=null;
		Jedis jedis=getJedis();
		try {
			CommMem.OBJ.msgPackRegister(clazz);
			//return both the unblocking key and the popped value
			byte[] bytes = jedis.rpop(key.getBytes("UTF-8"));
			if(null == bytes || 0 == bytes.length){
				return null;
			}
			back = CommMem.OBJ.transMsgPack2Entity(bytes,clazz,key.getBytes("UTF-8"));
		} catch (Exception e) {
			LoggerUtils.error.error("redis rpop error:", e);
		}finally{
			jedis.close();
		}
		return back;
	}
	/**
	 * 返回一个指定key的对象
	 * @param key
	 * @param field
	 * @param clazz
	 * @return
	 */
	public <TEntity> TEntity getMemInfo(String key,String field,Class<TEntity> clazz){
		TEntity back=null;
		Jedis jedis=getJedis();
		try {
			CommMem.OBJ.msgPackRegister(clazz);
			byte[] bytes=jedis.hget(key.getBytes("UTF-8"),field.getBytes("UTF-8"));
			back = CommMem.OBJ.transMsgPack2Entity(bytes,clazz,key.getBytes("UTF-8"));
		} catch (Exception e) {
			LoggerUtils.error.error("redis write error:", e);
		}finally{
        	jedis.close();
        }
		
		return back;
	}
	
	/**
	 * 返回一个指定key的对象
	 * @param key
	 * @param field
	 * @param clazz
	 * @param ...clazzs 实体中包含的所有class
	 * @return
	 */
	public <TEntity> TEntity getMemInfo(String key, String field,
			Class<TEntity> clazz, Class<?>... clazzs) {
		TEntity back=null;
		Jedis jedis=getJedis();
		try {
			//添加其他类型
			for(Class<?> c:clazzs){
				CommMem.OBJ.msgPackRegister(c);
			}
			CommMem.OBJ.msgPackRegister(clazz);
			
			byte[] bytes=jedis.hget(key.getBytes("UTF-8"),field.getBytes("UTF-8"));
			back = CommMem.OBJ.transMsgPack2Entity(bytes,clazz,key.getBytes("UTF-8"));
		} catch (Exception e) {
			LoggerUtils.error.error("redis write error:", e);
		}finally{
        	jedis.close();
        }
		
		return back;
	}
	
	/**
	 * 获取一个指定key的对象
	 * @param key
	 * @param field
	 */
	public <TEntity> TEntity getMemInfo(String key,Class<TEntity> clazz){
		TEntity back=null;
		Jedis jedis=getJedis();
		try {
			CommMem.OBJ.msgPackRegister(clazz);
			byte[] bytes=jedis.get(key.getBytes("UTF-8"));
			back = CommMem.OBJ.transMsgPack2Entity(bytes,clazz,key.getBytes("UTF-8"));
		} catch (Exception e) {
			LoggerUtils.error.error("redis get error:", e);
		}finally{
        	jedis.close();
        }
		
		return back;
	}
	
	/**
	 * 设置list列表
	 * @param key
	 * @param memInfos
	 * @param clazz
	 */
	public <TEntity> void setMemList(String key, List<TEntity> memInfos, Class<TEntity> clazz, int seconds) {
		if(StringUtils.isEmpty(key) || memInfos == null 
				|| clazz == null){
			LoggerUtils.error.error("redis list param is null");
			return;
		}
		
		Jedis jedis=getJedis();
		try {
			CommMem.OBJ.msgPackRegister(clazz);
			// 先清除之前的
			jedis.del(key.getBytes("UTF-8"));
			memInfos.forEach(v->{
				byte[] bytes = CommMem.OBJ.transEntity2MsgPack(v);
				try {
					jedis.rpush(key.getBytes("UTF-8"), bytes);
				} catch (Exception e) {
					LoggerUtils.error.error("redis list write error:", e);
				}
			});
			if(seconds>0){
				jedis.expire(key.getBytes("UTF-8"), seconds);
			}
		}catch(Exception e){
			LoggerUtils.error.error("redis set list error:", e);
		}finally{
        	jedis.close();
        }
	}
	
	/**
	 * 设置list列表,追加
	 * @param key
	 * @param memInfos
	 * @param clazz
	 * @param seconds -1永久
	 */
	public <TEntity> void putMemList(String key, List<TEntity> memInfos, Class<TEntity> clazz, int seconds) {
		if(StringUtils.isEmpty(key) || memInfos == null 
				|| clazz == null){
			LoggerUtils.error.error("redis list param is null");
			return;
		}
		
		Jedis jedis=getJedis();
		try {
			CommMem.OBJ.msgPackRegister(clazz);
			memInfos.forEach(v->{
				byte[] bytes = CommMem.OBJ.transEntity2MsgPack(v);
				try {
					jedis.rpush(key.getBytes("UTF-8"), bytes);
				} catch (Exception e) {
					LoggerUtils.error.error("redis list write error:", e);
				}
			});
			if(seconds>0){
				jedis.expire(key.getBytes("UTF-8"), seconds);
			}
		}catch(Exception e){
			LoggerUtils.error.error("redis set list error:", e);
		}finally{
			jedis.close();
		}
	}
	
	/**
	 * 设置list列表,头插入
	 * @param key
	 * @param memInfos
	 * @param clazz
	 * @param seconds -1永久
	 * @param limit -1不限制长度
	 */
	public <TEntity> void putLMemList(String key, List<TEntity> memInfos, Class<TEntity> clazz, int seconds,int limit) {
		if(StringUtils.isEmpty(key) || memInfos == null 
				|| clazz == null){
			LoggerUtils.error.error("redis list param is null");
			return;
		}
		
		Jedis jedis=getJedis();
		try {
			CommMem.OBJ.msgPackRegister(clazz);
			memInfos.forEach(v->{
				byte[] bytes = CommMem.OBJ.transEntity2MsgPack(v);
				try {
					jedis.lpush(key.getBytes("UTF-8"), bytes);
				} catch (Exception e) {
					LoggerUtils.error.error("redis list write error:", e);
				}
			});
			if(seconds > 0){
				jedis.expire(key.getBytes("UTF-8"), seconds);
			}
			if(limit > 0){
				jedis.ltrim(key.getBytes("UTF-8"), 0, limit);
			}
		}catch(Exception e){
			LoggerUtils.error.error("redis set list error:", e);
		}finally{
			jedis.close();
		}
	}
	
	@Override
	public void listTrim(String key, int start, int end) {
		if(StringUtils.isEmpty(key)){
			LoggerUtils.error.error("redis list param is null");
			return;
		}
		
		Jedis jedis=getJedis();
		try {
			jedis.ltrim(key.getBytes("UTF-8"), start, end);
		}catch(Exception e){
			LoggerUtils.error.error("redis listTrim error:", e);
		}finally{
			jedis.close();
		}
	}
	
	/**
	 * 设置list列表
	 * @param key
	 * @param memInfos
	 * @param clazz
	 */
	public void blpopMemList(String key, int seconds) {
		Jedis jedis=getJedis();
		try {
			jedis.blpop(seconds, key.getBytes("UTF-8"));
		}catch(Exception e){
			LoggerUtils.error.error("redis blpop list error:", e);
		}finally{
        	jedis.close();
        }
	}
	
	/**
	 * 设置到缓存中一个列表到集合中
	 * @param key
	 * @param list
	 * @param clazz
	 */
	public <TEntity> void setMemMap(String key,Map<String, TEntity> memInfos,Class<TEntity> clazz){
		Jedis jedis=getJedis();
		try {
			CommMem.OBJ.msgPackRegister(clazz);
			// 先清除之前的
			jedis.del(key.getBytes("UTF-8"));
			memInfos.forEach((k,v)->{
				byte[] bytes = CommMem.OBJ.transEntity2MsgPack(v);
				try {
					jedis.hset(key.getBytes("UTF-8"),k.toString().getBytes("UTF-8"), bytes);
				} catch (Exception e) {
					LoggerUtils.error.error("redis map write error:", e);
				}
			});
		}catch(Exception e){
			LoggerUtils.error.error("redis set map error:", e);
		}finally{
			jedis.close();
        }
	}
	
	/**
	 * 设置到缓存中一个列表到集合中,追加
	 * @param key
	 * @param list
	 * @param clazz
	 */
	public <TEntity> void putMemMap(String key,Map<String, TEntity> memInfos,Class<TEntity> clazz){
		Jedis jedis=getJedis();
		try {
			CommMem.OBJ.msgPackRegister(clazz);
			memInfos.forEach((k,v)->{
				byte[] bytes = CommMem.OBJ.transEntity2MsgPack(v);
				try {
					jedis.hset(key.getBytes("UTF-8"),k.toString().getBytes("UTF-8"), bytes);
				} catch (Exception e) {
					LoggerUtils.error.error("redis map write error:", e);
				}
			});
		}catch(Exception e){
			LoggerUtils.error.error("redis set map error:", e);
		}finally{
			jedis.close();
        }
	}
	
	/**
	 * 返回缓存中一个list列表到集合中
	 * @param key
	 * @param clazz
	 * @return
	 */
	public <TEntity> List<TEntity> getMemList(String key,Class<TEntity> clazz){
		List<TEntity> memInfos=Collections.synchronizedList(new ArrayList<TEntity>());
		Jedis jedis=getJedis();
		try {
			List<byte[]> bytes = jedis.lrange(key.getBytes("UTF-8"), 0, -1);
			CommMem.OBJ.msgPackRegister(clazz);
			bytes.forEach(v->{
				try {
					TEntity back = CommMem.OBJ.transMsgPack2Entity(v,clazz,key.getBytes("UTF-8"));
					if(back != null){
						memInfos.add(back);
					}
				} catch (Exception e) {
					try {
						CommMem.OBJ.getCache().clearByKey(key.getBytes("UTF-8"));
					} catch (Exception e1) {
						LoggerUtils.error.error("redis map read clear error:", e1);
					}
					LoggerUtils.error.error("redis map read inner error:", e);
				}
			});
		}catch (Exception e) {
			LoggerUtils.error.error("redis map keys error:", e);
		}finally{
        	jedis.close();
        }
		return memInfos;
	}
	
	@Override
	public long getListLength(String key) {
		long len = 0;
		if(StringUtils.isEmpty(key)){
			return len;
		}
		
		Jedis jedis=getJedis();
		try {
			len = jedis.llen(key.getBytes("UTF-8"));
		}catch (Exception e) {
			LoggerUtils.error.error("redis getListLength error:", e);
		}finally{
        	jedis.close();
        }
		return len;
	}
	
	/**
	 * 返回缓存中一个列表到集合中
	 * @param key
	 * @param clazz
	 * @return
	 */
	public <TEntity> Map<String, TEntity> getMemMap(String key,Class<TEntity> clazz){
		Map<String, TEntity> memInfos=new ConcurrentHashMap<String, TEntity>();
		Jedis jedis=getJedis();
		try {
			Map<byte[],byte[]> bytes=jedis.hgetAll(key.getBytes("UTF-8"));
			CommMem.OBJ.msgPackRegister(clazz);
			bytes.forEach((k,v)->{
				String tmpkey=new String(k);
				try {
					TEntity back = CommMem.OBJ.transMsgPack2Entity(v,clazz,key.getBytes("UTF-8"));
					if(back!=null){
						memInfos.put(tmpkey, back);
					}
				} catch (Exception e) {
					try {
						CommMem.OBJ.getCache().clearByKey(key.getBytes("UTF-8"));
					} catch (Exception e1) {
						LoggerUtils.error.error("redis map read clear error:", e1);
					}
					LoggerUtils.error.error("redis map read error:", e);
				}
			});
		}catch (Exception e) {
			LoggerUtils.error.error("redis map keys error:", e);
		}finally{
			jedis.close();
        }
		return memInfos;
	}
	
	/**
	 * 删除一个指定key的对象
	 * @param key
	 * @param field
	 */
	public void removeMemInfo(String key,String field){
		Jedis jedis=getJedis();
		try {
			jedis.hdel(key.getBytes("UTF-8"),field.getBytes("UTF-8"));
		} catch (Exception e) {
			LoggerUtils.error.error("redis removeMemInfo error:", e);
		}finally{
        	jedis.close();
        }
	}
	
	/**
	 * 删除一个指定key的对象
	 * @param key
	 */
	public void removeCacheByKey(String key) {
		Jedis jedis=getJedis();
		try {
			jedis.del(key.getBytes("UTF-8"));
		} catch (Exception e) {
			LoggerUtils.error.error("redis removeMemKey error:", e);
		}finally{
        	jedis.close();
        }
	}
	
	/**
	 * 删除一个指定key列表的对象
	 * @param key
	 */
	public void removeCacheByKeyList(List<String> keyList) {
		if(keyList == null || keyList.isEmpty()){
			return;
		}
		
		Jedis jedis=getJedis();
		try {
			byte[][] keysArr = new byte[keyList.size()][];
			int i = 0;
			for(String key : keyList){
				if(StringUtils.isEmpty(key)){
					continue;
				}
				keysArr[i] = key.getBytes("UTF-8");
				i++;
			}
			
			jedis.del(keysArr);
		} catch (Exception e) {
			LoggerUtils.error.error("redis removeMemKey error:", e);
		}finally{
        	jedis.close();
        }
	}
	
	/**
	 * 删除一个指定key的对象
	 * @param key
	 */
	public void removeSortSetByKey(String key) {
		Jedis jedis=getJedis();
		try {
			jedis.del(key);
		} catch (Exception e) {
			LoggerUtils.error.error("redis removeSortSetByKey error:", e);
		}finally{
        	jedis.close();
        }
	}
	
	/**
	 * 清除指定key的数据库层缓存
	 * @param key
	 */
	public void clearByKey(byte[] key) {
		if(key == null || key.length == 0){
			LoggerUtils.serverLogger.info("key is null or lentgh is 0");
			return;
		}
		
		Jedis jedis=getJedis();
		try {
			jedis.del(key);
		} catch (Exception e) {
			LoggerUtils.error.error("redis clearDbMemKey error:", e);
		}finally{
			jedis.close();
		}
	}

	@Override
    public Set<byte[]> getKeys(String pattern){
    	if(StringUtils.isEmpty(pattern)){
			LoggerUtils.serverLogger.info("pattern is null");
			return null;
		}
		
		Jedis jedis=getJedis();
		try {
			return jedis.keys(pattern.getBytes("UTF-8"));
		} catch (Exception e) {
			LoggerUtils.error.error("redis clearDbMemKey error:", e);
		}finally{
			jedis.close();
		}
		
		return null;
    }

	@Override
    public boolean acquireLock(String key, String param, int expireTime){
	    Jedis jedis = getJedis();
	    try {
	    	String result = jedis.set(key, param == null ? String.valueOf(System.currentTimeMillis()) : param, "NX", "EX", expireTime);
        	if ("OK".equalsIgnoreCase(result)) {
        		return true;
        	}
        } catch (Exception e) {
	        LoggerUtils.error.error(String.format("%s acquire lock failed", key), e);
        } finally {
        	jedis.close();
        }
	    
	    return false;
    }

	@Override
    public void releaseLock(String key){
	    Jedis jedis = getJedis();
	    try {
	        jedis.del(key);
        } catch (Exception e) {
	        LoggerUtils.error.error(String.format("% release lock failed", key));
        } finally {
        	jedis.close();
        }
    }

	@Override
	public long incr(String key) {
		if(key == null || "".equals(key)){
			LoggerUtils.error.error("key is null");
			return 0;
		}
		
		Jedis jedis=getJedis();
		try {
			return jedis.incr(key.getBytes("UTF-8"));
		} catch (Exception e) {
			LoggerUtils.error.error("incr error:", e);
			return 0;
		}finally {
			jedis.close();
		}
	}
	
	@Override
	public long incrBy(String key,long integer) {
		if(key == null || "".equals(key)){
			LoggerUtils.error.error("key is null");
			return 0;
		}
		
		Jedis jedis=getJedis();
		try {
			return jedis.incrBy(key.getBytes("UTF-8"), integer);
		} catch (Exception e) {
			LoggerUtils.error.error("incrBy error:", e);
			return 0;
		}finally {
			jedis.close();
		}
	}
	
	@Override
	public long decr(String key) {
		if(key == null || "".equals(key)){
			LoggerUtils.error.error("key is null");
			return 0;
		}
		
		Jedis jedis=getJedis();
		try {
			return jedis.decr(key.getBytes("UTF-8"));
		} catch (Exception e) {
			LoggerUtils.error.error("decr error:", e);
			return 0;
		}finally {
			jedis.close();
		}
	}
	
	@Override
	public long decrBy(String key,long integer) {
		if(key == null || "".equals(key)){
			LoggerUtils.error.error("key is null");
			return 0;
		}
		
		Jedis jedis=getJedis();
		try {
			return jedis.decrBy(key.getBytes("UTF-8"), integer);
		} catch (Exception e) {
			LoggerUtils.error.error("getStr error:", e);
			return 0;
		}finally {
			jedis.close();
		}
	}
}
