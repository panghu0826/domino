package com.jule.domino.game.api;

import com.jule.domino.game.api.impl.DataServlet;
import com.jule.domino.game.api.impl.GameServlet;
import com.jule.domino.game.api.impl.GmServlet;
import com.jule.domino.game.api.impl.ServerServlet;
import com.jule.domino.game.config.Config;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.ipfilter.IpFilterRuleHandler;
import org.jboss.netty.handler.ipfilter.IpFilterRuleList;
import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class RestfulServer {

    private final static Logger logger = LoggerFactory.getLogger(RestfulServer.class);

    public static final RestfulServer OBJ = new RestfulServer();

    /** 服务器 IP 地址 */
    private String _serverIP = "192.168.0.14";

    /** 服务器端口 */
    private int _port = 8000;

    /**
     * constructor
     */
    public RestfulServer() {
        this._serverIP = Config.REST_IP;
        this._port = Config.REST_PORT;
    }

    /**
     * Netty
     */
    private NettyJaxrsServer _netty;

    public void start(){
        if (_netty != null){
            logger.error("HTTP服务已经启动");
            return;
        }
        logger.info("HTTP服务开始初始化：ip="+_serverIP+",port="+_port);
        ResteasyDeployment deployment = new ResteasyDeployment();
        deployment.setSecurityEnabled(true);
        //注册命令
        this.registerCMDClazz(deployment);

        _netty = new NettyJaxrsServer();
        _netty.setDeployment(deployment);
        _netty.setPort(_port);
        _netty.setRootResourcePath(_serverIP);
        _netty.setSecurityDomain(null);
        _netty.setKeepAlive(true);
        _netty.start();
        _netty.getDeployment();
        logger.info("HTTP服务启动完成");
    }

    /**
     * 注册cmd类
     * @param deployment
     */
    public void registerCMDClazz(ResteasyDeployment deployment){
        if (deployment == null){
            return;
        }

        //注册handler
        List<String> resList = Arrays.asList(
                DataServlet.class.getName(),GmServlet.class.getName(), ServerServlet.class.getName(), GameServlet.class.getName()
        );
        deployment.setResourceClasses(resList);

        //注册filter
        List<String> filterList = Arrays.asList(
                RestFilter.class.getName()
        );
        deployment.setProviderClasses(filterList);


    }


    /**
     * IP 过滤
     * @param deployment
     */
    public void  ipfliter(ResteasyDeployment deployment){
        ChannelPipeline pipeline = Channels.pipeline();
        //添加IP过滤规则
        IpFilterRuleHandler rule = new IpFilterRuleHandler();
        rule.addAll(new IpFilterRuleList("+i:"+Config.REST_IP+" , -i:*"));
        pipeline.addLast("ipFilter", rule);
    }
}
