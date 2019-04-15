package com.jule.domino.game.gate.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;

public final class JoLoSslContext {

    private final static Logger logger = LoggerFactory.getLogger(JoLoSslContext.class);
    public static final SSLContext DEFAULT = createContext();

    /**
     * @return
     */
    private static final SSLContext createContext() {
        try {
            /*KeyStore ks = KeyStore.getInstance("JKS");
            InputStream ksInputStream = new FileInputStream("./config/"+ Config.SSL_FILE);
            ks.load(ksInputStream, Config.SSL_KEY.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, Config.SSL_KEY.toCharArray());
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);
            logger.info("启动ssl->" + sslContext.toString());
            return sslContext;*/
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
        return null;
    }
}
