package com.jule.core.jedis;

import com.jule.core.configuration.Property;

/**
 * This class holds all configuration of database
 * 
 * @author xujian
 */
public class JedisConfig {

	/**
	 * Default database url.
	 */
	@Property(key = "redis.ip", defaultValue = "192.168.0.14")
	public static String REDIS_IP;

	/**
	 * Default database user
	 */
	@Property(key = "redis.port", defaultValue = "6379")
	public static int REDIS_PORT;

	/**
	 * Minimum amount of connections that are always active
	 */
	@Property(key = "redis.max_total", defaultValue = "10")
	public static int			REDIS_MAX_TOTAL;

	/**
	 * Maximum amount of connections that are allowed to use
	 */
	@Property(key = "redis.max_idle", defaultValue = "5")
	public static int			REDIS_MAX_IDLE;

	@Property(key = "redis.password", defaultValue = "undefined")
	public static String		REDIS_PASSWORD;

	@Property(key = "redis.db_id", defaultValue = "1")
	public static int	DB_ID;

	@Property(key = "redis.open_sentinel", defaultValue = "false")
	public static boolean REDIS_OPEN_SENTINEL;

	@Property(key = "redis.master_name", defaultValue = "my_master")
	public static String	REDIS_MASTER_NAME;

	@Property(key = "redis.sentinel_address", defaultValue = "192.168.0.14:26379")
	public static String REDIS_SENTINEL_ADDRESS;

}
