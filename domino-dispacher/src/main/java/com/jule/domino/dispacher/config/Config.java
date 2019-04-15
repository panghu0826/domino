package com.jule.domino.dispacher.config;

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

    @Property(key = "dispacherserver.bind.ip", defaultValue = "0.0.0.0")
    public static String BIND_IP;

    @Property(key = "dispacherserver.server.ip", defaultValue = "13.251.82.92")
    public static String SERVER_IP;

    @Property(key = "dispacherserver.bind.port", defaultValue = "8080")
    public static int BIND_PORT;

    @Property(key = "enable.ssl", defaultValue = "true")
    public static boolean ENABLE_SSL;

    @Property(key = "login.url", defaultValue = "http://192.168.0.14:8006/api/account/login")
    public static String LOGIN_URL;

    @Property(key = "product.url", defaultValue = "http://192.168.0.14:8006/api/product/product")
    public static String PRODUCT_URL;

    @Property(key = "order.url", defaultValue = "http://192.168.0.14:8006/api/product/order")
    public static String ORDER_URL;
    @Property(key = "google.pay.url", defaultValue = "http://192.168.0.14:8006/api/pay/syncGooglePlay")
    public static String GOOGLEPLAY_VERIFY_URL;

    @Property(key = "ssl.fileurl", defaultValue = "teenpatti-test.joloplay.net.jks")
    public static String SSL_FILE;

    @Property(key = "ssl.filekey", defaultValue = "36xd684s9b8a7c")
    public static String SSL_KEY;

    @Property(key = "app.log.url", defaultValue = "http://10.0.0.92:59002")
    public static String LOG_URL;

    @Property(key = "app.ad.pk", defaultValue = "")
    public static String AD_PUBLIC_KEY;

    @Property(key = "item.server.url", defaultValue = "http://10.0.0.93:8888")
    public static String ITEM_SERVER_URL;

    @Property(key = "game.id", defaultValue = "1001")
    public static int GAME_ID;

    @Property(key = "item.server.can_unlock_item", defaultValue = "false")
    public static boolean CAN_UNLOCK_ITEM;

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

            Properties[] props = PropertiesUtils.loadAllFromDirectory("./config");
            PropertiesUtils.overrideProperties(props, myProps);

            log.info("Loading: gateserver.properties");
            ConfigurableProcessor.process(Config.class, props);
            log.info("Loading: redis.properties");
            ConfigurableProcessor.process(JedisConfig.class, props);
            log.info("Loading: database.properties");
            ConfigurableProcessor.process(DatabaseConfig.class, props);
            log.info("Loading: threadpool.properties");
            ConfigurableProcessor.process(ThreadConfig.class, props);
        } catch (Exception e) {
            log.error("Can't load gateserver configuration", e);
            throw new Error("Can't load chatserver configuration", e);
        }
    }
}
