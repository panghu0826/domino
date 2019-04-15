package com.jule.db.cache;

import com.google.common.collect.Maps;
import com.jule.core.jedis.JedisConfig;
import com.jule.core.common.log.LoggerUtils;
import org.msgpack.MessagePack;

import java.util.Map;


/**
 * 缓存使用工具
 *
 * @author ran
 */
public class CommMem {
	/** 单例对象 */
	public static final CommMem OBJ = new CommMem();
	/** 缓存对象 **/
	private ICache cache = null;
	/** msgPack实例对象 **/
	private MessagePack msgpack = new MessagePack();
	/** bean注册池子 **/
	private static Map<Class<?>,Integer> msgRegister_pool = Maps.newConcurrentMap();
	
	/** 缓存对象key前缀 **/
	public final static String MEM_ID_PRE = "mem_";
	public final static String DB_KEY_PRE = "db_";
	private final static String MEM_KEY_SPLIT = "_";
	
	
	private CommMem() 
	{
		
	}
	
	/**
	 * 初始化memcache配置信息
	 */
	public void initMemConfig()
	{
		if(this.cache != null){
			LoggerUtils.error.error("ICache already init");
			return;
		}

		CacheConfig config = new CacheConfig();
		config.setPoolMaxIdle(JedisConfig.REDIS_MAX_IDLE);
		config.setPoolMaxTotal(JedisConfig.REDIS_MAX_TOTAL);
		config.setPoolMsaxWait(100000);

		config.setIp(JedisConfig.REDIS_IP);
		config.setPort(JedisConfig.REDIS_PORT);
		if (!JedisConfig.REDIS_PASSWORD.equalsIgnoreCase("undefined")){
			config.setRedisPwd(JedisConfig.REDIS_PASSWORD);
		}
		config.setRedisDbIndex(JedisConfig.DB_ID);
		
		cache = CachedFactory.createServer(CachedFactory.REDIS);
		cache.init(config);
		
		//设置类加载器
	    msgpack.setClassLoader(Thread.currentThread().getContextClassLoader());
	}
	
	public ICache getCache(){
		//容错处理，有时报缓存cache为空
		if(this.cache == null){
			initMemConfig();
		}
		
		return this.cache;
	}
	
	/**
	 * 设置数据实体转为二进制
	 * @param entity
	 * @return
	 */
	public <TEntity> byte[] transEntity2MsgPack(TEntity entity){
		// 序列化
        byte[] bytes = null;
		try {
			bytes = msgpack.write(entity);
		} catch (Exception e) {
			LoggerUtils.error.error("redis write error:", e);
		}
		return bytes;
	}
	
	/**
	 * 获取对应的数据实体
	 * @param bytes
	 * @param clazz
	 * @return
	 */
	public <TEntity> TEntity transMsgPack2Entity(byte[] bytes,Class<TEntity> clazz,byte[] key){
		TEntity back = null;
		if(bytes == null)return back;
		try {
			back = msgpack.read(bytes,clazz);
		} catch (Exception e) {
			getCache().clearByKey(key);
			back = null;
			LoggerUtils.error.error("redis write error:", e);
		}
		return back;
	}
	
	/**
	 * 注册msgPack类
	 * @param clazz
	 */
	public void msgPackRegister(Class<?> clazz){
		// 序列化
		if(msgRegister_pool.containsKey(clazz)){
			return;
		}
		msgpack.register(clazz);
		msgRegister_pool.put(clazz,1);
	}
	
	/**
	 * 数据库二级缓存唯一key
	 * @param id
	 * @param table
	 * @return
	 */
	public String getDBUniqueKey(Object id,String table){
		return DB_KEY_PRE+table+MEM_KEY_SPLIT+id;
	}
	
}
