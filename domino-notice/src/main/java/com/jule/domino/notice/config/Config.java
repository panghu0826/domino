package com.jule.domino.notice.config;

import com.jule.core.configuration.ConfigurableProcessor;
import com.jule.core.configuration.Property;
import com.jule.core.configuration.ThreadConfig;
import com.jule.core.database.DatabaseConfig;
import com.jule.core.jedis.JedisConfig;
import com.jule.core.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author xujian
 */
public class Config {

	protected static final Logger log = LoggerFactory.getLogger(Config.class);

	@Property(key = "notice.bind.ip", defaultValue = "0.0.0.0")
	public static String NOTICE_BIND_IP;

	@Property(key = "notice.bind.port", defaultValue = "8080")
	public static int NOTICE_BIND_PORT;

	@Property(key = "notice.bind.gameId", defaultValue = "62001")
	public static String NOTICE_BIND_GAMEID;

	/**
	 * Load configs from files.
	 */
	public static void load(String dir) {
		try {

			Properties[] props = PropertiesUtils.loadAllFromDirectory("./config/network");

			Properties[] props2 = PropertiesUtils.loadAllFromDirectory("./config/network/" + dir + "/");
			log.info("Loading: redis.properties");
			ConfigurableProcessor.process(JedisConfig.class, props);
			log.info("Loading: database.properties");
			ConfigurableProcessor.process(DatabaseConfig.class, props);
			log.info("Loading: threadpool.properties");
			ConfigurableProcessor.process(ThreadConfig.class, props);
			log.info("Loading: room.properties");
			ConfigurableProcessor.process(Config.class, props2);

		}
		catch (Exception e) {
			log.error("Can't load room configuration", e);
			throw new Error("Can't load room configuration", e);
		}
	}
}
