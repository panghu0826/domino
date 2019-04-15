package com.jule.domino.game.config;

import com.jule.core.configuration.ConfigurableProcessor;
import com.jule.core.configuration.Property;
import com.jule.core.configuration.ThreadConfig;
import com.jule.core.database.DatabaseConfig;
import com.jule.core.jedis.JedisConfig;
import com.jule.core.utils.NetworkUtils;
import com.jule.core.utils.PropertiesUtils;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.game.log.config.RabbitmqConfig;
import lombok.extern.slf4j.Slf4j;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * @author xujian 2017-12-14
 */
@Slf4j
public class Config {

    @Property(key = "blind.show.roundcount", defaultValue = "1")
    public static int BLIND_SHOW_ROUNDCOUNT;

    @Property(key = "game.bind.ip", defaultValue = "192.168.0.14")
    public static String BIND_IP;

    @Property(key = "game.bind.port", defaultValue = "8080")
    public static int BIND_PORT;

    @Property(key = "notice.forward.port", defaultValue = "localhost:8501")
    public static InetSocketAddress NOTICE_FORWARD_PORT;

    @Property(key = "game.playerLimit", defaultValue = "3000")
    public static int PLAYER_LIMIT;

    @Property(key = "game.ids", defaultValue = "10001")
    public static String GAME_IDS;

    @Property(key = "restful.ip", defaultValue = "192.168.0.14")
    public static  String REST_IP ;

    @Property(key = "restful.port", defaultValue = "8082")
    public static  int REST_PORT ;

    /**日志服*/
    @Property(key = "app.log.url", defaultValue = "http://10.0.0.92:59002")
    public static String LOG_URL;

    /**选择玩法倒计时*/
    @Property(key = "choose.playtype.cd", defaultValue = "10")
    public static int CHOOSE_PLAYTYPE_CD;

    @Property(key = "app.mode.test", defaultValue = "true")
    public static boolean MODE_TEST;
    @Property(key = "load.db.config.hour", defaultValue = "5")
    public static int loadDBConfigHour;

    @Property(key = "game.alarm", defaultValue = "false")
    public static boolean GAME_ALARM;

    @Property(key = "item.server.url", defaultValue = "http://10.0.0.93:8888")
    public static String ITEM_SERVER_URL;

    @Property(key = "app.pay.channel", defaultValue = "googleplay")
    public static String DEFUALT_PAY_CHANNEL;

    @Property(key = "item.server.can_unlock_item", defaultValue = "false")
    public static boolean CAN_UNLOCK_ITEM;

    /**********************大厅接入参数**************************/
    @Property(key = "game.id", defaultValue = "1001")
    public static int GAME_ID;

    @Property(key = "game.serverId", defaultValue = "1")
    public static String GAME_SERID;

    @Property(key = "game.maxLoad", defaultValue = "500")
    public static int GAME_MAXLOAD;

    @Property(key = "game.gateurl", defaultValue = "ws://192.168.0.14:56001/gate")
    public static String GAME_GATEURL;

    @Property(key = "game.gwcurl", defaultValue = "192.168.0.14:54001")
    public static String GAME_GWCURL;

    @Property(key = "game.reporturl", defaultValue = "")
    public static String GAME_REPORTURL;

    @Property(key = "game.accounturl", defaultValue = "")
    public static String GAME_ACCOUNTURL;

    @Property(key = "game.encryption.key", defaultValue = "")
    public static String GAME_ENCRYPE_KEY;

    @Property(key = "game.encryption.isOpen", defaultValue = "false")
    public static boolean GAME_ENCRYPE_ISOPEN;

    /**********************gate参数**************************/

    @Property(key = "gateserver.bind.port", defaultValue = "8080")
    public static int GATE_BIND_PORT;

    @Property(key = "noticeserver.bind.port", defaultValue = "8090")
    public static int NOTICESERVER_BIND_PORT;

    @Property(key = "room.forward.port", defaultValue = "localhost:8501")
    public static InetSocketAddress ROOM_FORWARD_PORT;

    @Property(key = "enable.ssl", defaultValue = "false")
    public static boolean ENABLE_SSL;

    @Property(key = "gateserver.islocal", defaultValue = "true")
    public static boolean GATESERVER_ISLOCAL;

    @Property(key = "ssl.fileurl", defaultValue = "teenpatti-test.joloplay.net.jks")
    public static String SSL_FILE;

    @Property(key = "ssl.filekey", defaultValue = "36xd684s9b8a7c")
    public static String SSL_KEY;

    @Property(key = "gateserver.msgreversal", defaultValue = "false")
    public static boolean GATE_MSG_REVERSAL;


    /**********************room参数**************************/
    @Property(key = "room.bind.port", defaultValue = "8080")
    public static int ROOM_BIND_PORT;

    @Property(key = "table.playerLimit", defaultValue = "50")
    public static int TABLE_PLAYER_LIMIT;

    @Property(key = "add.desk.nil.seat", defaultValue = "6")
    public static int ROOM_ADD_DESK_NIL_SEAT;

    @Property(key = "remove.desk.nil.seat", defaultValue = "11")
    public static int ROOM_REMOVE_DESK_NIL_SEAT;

    @Property(key = "room.init.desk.num", defaultValue = "2")
    public static int ROOM_INIT_DESK_NUM;

    @Property(key = "room.assigned.together", defaultValue = "true")
    public static boolean ROOM_ASSIGNED_TOGETHER;

    @Property(key = "guest.init.money", defaultValue = "100")
    public static long GUEST_INIT_MONEY;

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
            ConfigurableProcessor.process(Config.class, PropertiesUtils.loadAllFromDirectory("./config/network/" + dir + "/"));
            log.info("Loading: redis.properties");
            ConfigurableProcessor.process(JedisConfig.class, props);
            log.info("Loading: database.properties");
            ConfigurableProcessor.process(DatabaseConfig.class, props);
            log.info("Loading: signreach.properties");
            ConfigurableProcessor.process(RabbitmqConfig.class, props);
            log.info("Loading: threadpool.properties");
            ConfigurableProcessor.process(ThreadConfig.class, props);
        } catch (Exception e) {
            log.error("Can't load Game configuration", e);
            throw new Error("Can't load Game configuration", e);
        }
    }
}
