package com.jule.robot.config;

import com.jule.core.configuration.ConfigurableProcessor;
import com.jule.core.configuration.Property;
import com.jule.core.database.DatabaseConfig;
import com.jule.core.jedis.JedisConfig;
import com.jule.core.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author SmartGuo
 */
public class Config {

	protected static final Logger log = LoggerFactory.getLogger(Config.class);

	@Property(key = "robot.bind.port", defaultValue = "9009")
	public static int ROBOT_BIND_PORT;

	@Property(key = "gate.svr.uri", defaultValue = "")
	public static String GATE_SVR_URI;

	@Property(key = "robot.desk.robot.max.num", defaultValue = "0")
	public static int ROBOT_DESK_ROBOT_NUM;

	@Property(key = "robot.desk.player.min.num", defaultValue = "0")
	public static int ROBOT_DESK_PLAYER_NUM;

	@Property(key = "robot.total.robot.num", defaultValue = "3000")
	public static int ROBOT_TOTAL_ROBOT_NUM;

	@Property(key = "check.thread.interval.sec", defaultValue = "3")
	public static int CHECK_THREAD_INTERVAL_SEC;

	@Property(key = "test.type.is.local", defaultValue = "0")
	public static int TEST_TYPE_IS_LOCAL;

	@Property(key = "test.type.is.stress", defaultValue = "0")
	public static int TEST_TYPE_IS_STRESS;

	@Property(key = "robot.join.null.table", defaultValue = "0")
	public static int ROBOT_JOIN_NULL_TABLE;

	@Property(key = "robot.join.null.max.ante", defaultValue = "20")
	public static int ROBOT_JOIN_NULL_MAX_ANTE;

	/**日志服*/
	@Property(key = "app.log.url", defaultValue = "http://10.0.0.92:59001")
	public static String LOG_URL;
	@Property(key = "tomail.list", defaultValue = "guoxu@joloplay.com")
	public static String TO_MAIL_LIST;

	@Property(key = "robot.reward.croupier", defaultValue = "10")
	public static int ROBOT_REWARD_CROUPIER;
	@Property(key = "robot.give.gift", defaultValue = "5")
	public static int ROBOT_GIVE_GIFT;
	@Property(key = "robot.give.gift.win.bootamount", defaultValue = "20")
	public static int ROBOT_GIVE_GIFT_WIN_BOOTAMOUNT;

	//机器人连接最长的存活时间，超过此时间，断开机器人连接
	@Property(key = "robot.timout.minute", defaultValue = "60")
	public static int ROBOT_TIMEOUT_MINUTE;

	@Property(key = "game.encryption.key", defaultValue = "")
	public static String GAME_ENCRYPE_KEY;

	@Property(key = "game.encryption.isOpen", defaultValue = "false")
	public static boolean GAME_ENCRYPE_ISOPEN;

	/**
	 * Load configs from files.
	 */
	public static void load() {
		try {

			Properties myProps = null;
			try {
				log.info("Loading: mycs.properties");
				myProps = PropertiesUtils.load("./config/mycs.properties");
			}
			catch (Exception e) {
				log.info("No override properties found");
			}

			Properties[] props = PropertiesUtils.loadAllFromDirectory("./config/network");
			PropertiesUtils.overrideProperties(props, myProps);

			log.info("Loading: robot.properties");
			ConfigurableProcessor.process(Config.class, props);
			log.info("Loading: redis.properties");
			ConfigurableProcessor.process(JedisConfig.class, props);
			log.info("Loading: database.properties");
			ConfigurableProcessor.process(DatabaseConfig.class, props);
		}
		catch (Exception e) {
			log.error("Can't load room configuration", e);
			throw new Error("Can't load room configuration", e);
		}
	}
}
