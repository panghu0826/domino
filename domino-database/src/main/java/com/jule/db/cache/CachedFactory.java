package com.jule.db.cache;


/**
 * 缓存工厂
 * 
 * @author ran
 */
public class CachedFactory
{
	public static final int MEMCACHEDCLIENT = 1;
	public static final int REDIS = 2;
	
	//为了防止重复创建
	private static ICache memCached;
	private static ICache redisCached;
	
	public static ICache createServer(int type)
	{
		switch(type)
		{
			case MEMCACHEDCLIENT:
				if(memCached == null)
				{
					memCached = new MemCached();
				}
				return memCached;
			case REDIS:
				if(redisCached== null)
				{
					redisCached = new RedisCached();
				}
				return redisCached;
			default:
				return null;
		}
	}
}
