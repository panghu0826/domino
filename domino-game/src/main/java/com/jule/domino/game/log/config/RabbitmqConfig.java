package com.jule.domino.game.log.config;


import com.jule.core.configuration.Property;

/**
 * @author
 * @since 2019/1/18 16:11
 */
public class RabbitmqConfig {

    @Property(key = "rabbit.host", defaultValue = "192.168.0.14")
    public static String host;

    @Property(key = "rabbit.username", defaultValue = "1")
    public static String username;

    @Property(key = "rabbit.password", defaultValue = "1")
    public static String password;

    @Property(key = "rabbit.port", defaultValue = "0")
    public static int port;

    @Property(key = "rabbit.vhost", defaultValue = "0")
    public static String vhost;
}
