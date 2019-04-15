package com.jule.core.config;

import com.jule.core.configuration.ConfigurableProcessor;
import com.jule.core.configuration.Property;
import com.jule.core.database.DatabaseConfig;
import com.jule.core.jedis.JedisConfig;
import com.jule.core.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * @author yanbin.liang 2018-3-29
 */
public class Config {

    protected static final Logger log = LoggerFactory.getLogger(Config.class);

    @Property(key = "game.bind.port", defaultValue = "8080")
    public static int BIND_PORT;

    @Property(key = "sub.game.id", defaultValue = "0")
    public static int SUB_GAME_ID;

    @Property(key = "notice.forward.port", defaultValue = "localhost:8501")
    public static InetSocketAddress NOTICE_FORWARD_PORT;

    @Property(key = "game.table.playerLimit", defaultValue = "50")
    public static int PLAYER_LIMIT;

    @Property(key = "game.id", defaultValue = "10001")
    public static String GAME_ID;

    /**
     * Load configs from files.
     */
    public static void load() {
        try {

            Properties myProps = null;
            try {
                log.info("Loading: mycs.properties");
                myProps = PropertiesUtils.load("./config/mycs.properties");
            } catch (Exception e) {
                log.info("No override properties found");
            }

            Properties[] props = PropertiesUtils.loadAllFromDirectory("./config/network");
            PropertiesUtils.overrideProperties(props, myProps);

            log.info("Loading: game.properties");
            ConfigurableProcessor.process(Config.class, props);
            log.info("Loading: redis.properties");
            ConfigurableProcessor.process(JedisConfig.class, props);
            log.info("Loading: database.properties");
            ConfigurableProcessor.process(DatabaseConfig.class, props);
            log.info("Loading: signreach.properties");
//            ConfigurableProcessor.process(SignreachConfig.class, props);
        } catch (Exception e) {
            log.error("Can't load Game configuration", e);
            throw new Error("Can't load Game configuration", e);
        }
    }
}
