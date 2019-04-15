package com.jule.domino.auth.config;

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
 * @author xujian 2017-12-14
 */
public class Config {

    protected static final Logger log = LoggerFactory.getLogger(Config.class);

    @Property(key = "auth.bind.ip", defaultValue = "0.0.0.0")
    public static String BIND_IP;

    @Property(key = "auth.bind.port", defaultValue = "8006")
    public static int BIND_PORT;

    @Property(key = "facebook.init.money", defaultValue = "2000")
    public static long FACEBOOK_INIT_MONEY;

    @Property(key = "guest.init.money", defaultValue = "500")
    public static long GUEST_INIT_MONEY;

    @Property(key = "classic.server.ip", defaultValue = "localhost")
    public static String CLASSIC_SERVER_IP;

    @Property(key = "classic.server.port", defaultValue = "62001")
    public static int CLASSIC_SERVER_PORT;

    @Property(key = "changecard.server.ip", defaultValue = "localhost")
    public static String CHANGE_CARD_SERVER_IP;

    @Property(key = "changecard.server.port", defaultValue = "62002")
    public static int CHANGE_CARD_SERVER_PORT;

    @Property(key = "joker.server.ip", defaultValue = "localhost")
    public static String JOKER_SERVER_IP;

    @Property(key = "joker.server.port", defaultValue = "62002")
    public static int JOKER_SERVER_PORT;

    @Property(key = "hukam.server.ip", defaultValue = "localhost")
    public static String HUKAM_SERVER_IP;

    @Property(key = "hukam.server.port", defaultValue = "62002")
    public static int HUKAM_SERVER_PORT;

    @Property(key = "blind.server.ip", defaultValue = "localhost")
    public static String BLIND_SERVER_IP;

    @Property(key = "blind.server.port", defaultValue = "62002")
    public static int BLIND_SERVER_PORT;

    @Property(key = "dealer.server.ip", defaultValue = "localhost")
    public static String DEALER_SERVER_IP;

    @Property(key = "dealer.server.port", defaultValue = "62002")
    public static int DEALER_SERVER_PORT;

    @Property(key = "app.id", defaultValue = "0")
    public static String APP_ID;

    @Property(key = "app.token", defaultValue = "134739547330835|va2foJGEJ68lZcM8U7Xek3LoAgs")
    public static String APP_TOKEN;

    @Property(key = "qa", defaultValue = "true")
    public static boolean QA_MODE;

    @Property(key = "client.id", defaultValue = "")
    public static String GOOGLE_CLIENT_ID;

    @Property(key = "client.secret", defaultValue = "")
    public static String GOOGLE_CLIENT_SECRET;

    @Property(key = "client.refresh_token", defaultValue = "")
    public static String GOOGLE_CLIENT_REFRESH_TOKEN;

    @Property(key = "app.package", defaultValue = "")
    public static String GOOGLE_APP_PACKAGE;

    @Property(key = "app.id", defaultValue = "0")
    public static String GOOGLE_APP_ID;

    @Property(key = "game.currency", defaultValue = "false")
    public static boolean CURRENCY;

    @Property(key = "guest.init.firstLanding", defaultValue = "0")
    public static int FIRSTLANDING;

    @Property(key = "app.log.url", defaultValue = "http://10.0.0.92:59002")
    public static String LOG_URL;

    @Property(key = "game.proxy", defaultValue = "true")
    public static boolean PROXY;

    @Property(key = "ssl.fileurl", defaultValue = "teenpatti-test.joloplay.net.jks")
    public static String SSL_FILE;

    @Property(key = "ssl.filekey", defaultValue = "36xd684s9b8a7c")
    public static String SSL_KEY;

    @Property(key = "app.public.key", defaultValue = "")
    public static String GOOGLE_PAY_PUBLIC_KEY;

    @Property(key = "app.huawei.key", defaultValue = "")
    public static String HUAWEI_PAY_PUBLIC_KEY;

    @Property(key = "app.pay.channel", defaultValue = "googleplay")
    public static String DEFUALT_PAY_CHANNEL;

    @Property(key = "item.server.url", defaultValue = "http://10.0.0.93:8888")
    public static String ITEM_SERVER_URL;

    @Property(key = "game.id", defaultValue = "1001")
    public static int GAME_ID;

    @Property(key = "app.huawei.appid", defaultValue = "")
    public static String HUAWEI_LOGIN_APPID;

    @Property(key = "app.huawei.cpid", defaultValue = "")
    public static String HUAWEI_LOGIN_CPID;

    @Property(key = "app.huawei.login.privatekey", defaultValue = "")
    public static String HUAWEI_SIGN_PRIVATE_KEY;

    @Property(key = "app.huawei.login.publickey", defaultValue = "")
    public static String HUAWEI_SIGN_PUBLIC_KEY;


    /**
     * Load configs from files.
     */
    public static void load() {
        try {

            Properties myProps = null;

            Properties[] props = PropertiesUtils.loadAllFromDirectory("./config/network");
            PropertiesUtils.overrideProperties(props, myProps);

            log.info("Loading: auth.properties");
            ConfigurableProcessor.process(Config.class, props);
            log.info("Loading: redis.properties");
            ConfigurableProcessor.process(JedisConfig.class, props);
            log.info("Loading: database.properties");
            ConfigurableProcessor.process(DatabaseConfig.class, props);
            log.info("Loading: threadpool.properties");
            ConfigurableProcessor.process(ThreadConfig.class, props);
        } catch (Exception e) {
            log.error("Can't load Auth configuration", e);
            throw new Error("Can't load Auth configuration", e);
        }
    }
}
