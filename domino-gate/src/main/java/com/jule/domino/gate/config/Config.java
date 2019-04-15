package com.jule.domino.gate.config;

import com.jule.core.configuration.ConfigurableProcessor;
import com.jule.core.configuration.Property;
import com.jule.core.configuration.ThreadConfig;
import com.jule.core.database.DatabaseConfig;
import com.jule.core.jedis.JedisConfig;
import com.jule.core.utils.NetworkUtils;
import com.jule.core.utils.PropertiesUtils;
import com.jule.domino.base.enums.GameConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * @author xujian
 */
public class Config {

    protected static final Logger log = LoggerFactory.getLogger(Config.class);
    @Property(key = "gateserver.bind.ip", defaultValue = "192.168.0.14")
    public static String BIND_IP;

    @Property(key = "gateserver.bind.port", defaultValue = "8080")
    public static int BIND_PORT;

    @Property(key = "noticeserver.bind.port", defaultValue = "8090")
    public static int NOTICESERVER_BIND_PORT;

    @Property(key = "room.forward.port", defaultValue = "localhost:8501")
    public static InetSocketAddress ROOM_FORWARD_PORT;

    @Property(key = "game.ids", defaultValue = "0")
    public static String GAME_IDS;

    @Property(key = "enable.ssl", defaultValue = "true")
    public static boolean ENABLE_SSL;

    @Property(key = "login.url", defaultValue = "http://192.168.0.14:8006/api/account/login")
    public static String LOGIN_URL;

    @Property(key = "max.online", defaultValue = "2000")
    public static int MAX_ONLINE;

    @Property(key = "gateserver.islocal", defaultValue = "true")
    public static boolean GATESERVER_ISLOCAL;

    @Property(key = "ssl.fileurl", defaultValue = "teenpatti-test.joloplay.net.jks")
    public static String SSL_FILE;

    @Property(key = "ssl.filekey", defaultValue = "36xd684s9b8a7c")
    public static String SSL_KEY;

    /**
     * Load configs from files.
     */
    public static void load(String dir) {
        try {
            Set<String> ips= NetworkUtils.getLoalhostIP();
            Iterator<String> it = ips.iterator();
            while (it.hasNext()) {
                String str = it.next();
                if(str.startsWith("10.0.0")){
                    GameConst.localHost = str;
                    System.out.println(str);
                    break;
                }
            }
            Properties[] props = PropertiesUtils.loadAllFromDirectory("./config");

            log.info("Loading: redis.properties");
            ConfigurableProcessor.process(JedisConfig.class, props);
            log.info("Loading: database.properties");
            ConfigurableProcessor.process(DatabaseConfig.class, props);
            log.info("Loading: threadpool.properties");
            ConfigurableProcessor.process(ThreadConfig.class, props);

            Properties[] props2 = PropertiesUtils.loadAllFromDirectory("./config/" + dir + "/");
            log.info("Loading: gateserver.properties");
            ConfigurableProcessor.process(Config.class, props2);

        } catch (Exception e) {
            log.error("Can't load gateserver configuration", e);
            throw new Error("Can't load chatserver configuration", e);
        }
    }
}
