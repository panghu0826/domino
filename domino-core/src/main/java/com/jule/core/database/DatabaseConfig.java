package com.jule.core.database;

import com.jule.core.configuration.Property;

/**
 * This class holds all configuration of database
 * 
 * @author SoulKeeper
 */
public class DatabaseConfig {

	/**
	 * Default database url.
	 */
	@Property(key = "database.url", defaultValue = "jdbc:mysql://localhost:3306/db")
	public static String DATABASE_URL;

	/**
	 * Default database user
	 */
	@Property(key = "database.user", defaultValue = "root")
	public static String		DATABASE_USER;

	/**
	 * Default database password
	 */
	@Property(key = "database.password", defaultValue = "root")
	public static String		DATABASE_PASSWORD;

	/**
	 * Minimum amount of connections that are always active
	 */
	@Property(key = "database.connections.min", defaultValue = "1")
	public static int			DATABASE_CONNECTIONS_MIN;

	/**
	 * Maximum amount of connections that are allowed to use
	 */
	@Property(key = "database.connections.max", defaultValue = "5")
	public static int			DATABASE_CONNECTIONS_MAX;

}
