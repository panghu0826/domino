package com.jule.domino.room.config;

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

    @Property(key = "room.bind.ip", defaultValue = "0.0.0.0")
    public static String ROOM_BIND_IP;

    @Property(key = "room.bind.port", defaultValue = "8080")
    public static int ROOM_BIND_PORT;

    @Property(key = "table.playerLimit", defaultValue = "50")
    public static int TABLE_PLAYER_LIMIT;

    @Property(key = "game.ids", defaultValue = "0")
    public static String GAME_IDS;

    @Property(key = "add.desk.nil.seat", defaultValue = "6")
    public static int ROOM_ADD_DESK_NIL_SEAT;

    @Property(key = "remove.desk.nil.seat", defaultValue = "11")
    public static int ROOM_REMOVE_DESK_NIL_SEAT;

    @Property(key = "room.init.desk.num", defaultValue = "2")
    public static int ROOM_INIT_DESK_NUM;

    @Property(key = "room.assigned.together", defaultValue = "true")
    public static boolean ROOM_ASSIGNED_TOGETHER;

    @Property(key = "load.db.config.hour", defaultValue = "5")
    public static int loadDBConfigHour;
    /**
     * Load configs from files.
     */
    public static void load(String dir) {
        try {
            Properties[] props = PropertiesUtils.loadAllFromDirectory("./config/network");

            ConfigurableProcessor.process(JedisConfig.class, props);
            log.info("Loading: database.properties");
            ConfigurableProcessor.process(DatabaseConfig.class, props);
            log.info("Loading: threadpool.properties");
            ConfigurableProcessor.process(ThreadConfig.class, props);

            Properties[] props2 = PropertiesUtils.loadAllFromDirectory("./config/network/" + dir + "/");
            log.info("Loading: room.properties");
            ConfigurableProcessor.process(Config.class, props2);
        } catch (Exception e) {
            log.error("Can't load room configuration", e);
            throw new Error("Can't load room configuration", e);
        }
    }
}
