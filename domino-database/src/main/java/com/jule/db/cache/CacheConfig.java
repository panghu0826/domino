package com.jule.db.cache;


/**
 * 缓存配置信息
 *
 * @author ran
 */
public class CacheConfig{
	private int poolMaxTotal;
	private int poolMaxIdle;
	private int poolMsaxWait;
	private String ip;
	private int port;
	private String redisPwd;
	private int redisDbIndex=0;

	public int getPoolMaxTotal() {
		return poolMaxTotal;
	}

	public void setPoolMaxTotal( int poolMaxTotal ) {
		this.poolMaxTotal = poolMaxTotal;
	}

	public int getPoolMaxIdle() {
		return poolMaxIdle;
	}

	public void setPoolMaxIdle( int poolMaxIdle ) {
		this.poolMaxIdle = poolMaxIdle;
	}

	public int getPoolMsaxWait() {
		return poolMsaxWait;
	}

	public void setPoolMsaxWait( int poolMsaxWait ) {
		this.poolMsaxWait = poolMsaxWait;
	}

	public String getIp() {
		return ip;
	}

	public void setIp( String ip ) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort( int port ) {
		this.port = port;
	}

	public String getRedisPwd() {
		return redisPwd;
	}

	public void setRedisPwd( String redisPwd ) {
		this.redisPwd = redisPwd;
	}

	public int getRedisDbIndex() {
		return redisDbIndex;
	}

	public void setRedisDbIndex( int redisDbIndex ) {
		this.redisDbIndex = redisDbIndex;
	}
}
