package com.jule.domino.game.log.producer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.log.config.RabbitmqConfig;
import com.jule.domino.game.log.msg.AbstractMqMsg;
import com.jule.domino.log.logobjs.AbstractPlayerLog;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RabbitMq producer
 *
 * @since 2019/1/20 16:50
 */
@Slf4j
public class RabbitMqSender {

    //singleton
    public static final RabbitMqSender me = new RabbitMqSender();

    //rabbitmq client
    private ConnectionFactory factory = null;

    //创建异步线程池
    private ExecutorService _exec = Executors.newFixedThreadPool(16);
    //Gson对象
    private static final Gson gson = new GsonBuilder().serializeNulls().create();
    //时间格式
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    //字符集
    private static final String CHARSET = "utf-8";
    //消息队列绑定的exchange
    private static final String EXCHANGE = String.valueOf(Config.GAME_ID);
    //消息队列routing
    private static final String ROUTING_KEY = "info."+Config.GAME_ID;

    //访问日志：access_log、错误日志：error_log、账户日志：account_log、自定义日志：custom_log
    private static final String ACCESS = "access_log";
    private static final String ACCOUNT = "account_log";

    /**
     * 初始化配置
     */
    public void init(){
        try {
            if (factory != null){
                return;
            }

            //初始化rabbitmq创建连接工厂
            factory = new ConnectionFactory();
            //设置RabbitMQ相关信息
            factory.setHost(RabbitmqConfig.host);
            factory.setUsername(RabbitmqConfig.username);
            factory.setPassword(RabbitmqConfig.password);
            factory.setPort(RabbitmqConfig.port);
            factory.setVirtualHost(RabbitmqConfig.vhost);

            log.info("rabbitmq 初始化成功.");
        }catch (Exception ex){
            log.error("mq初始化异常", ex);
        }
    }

    /**
     * 生产消息
     * @param logObj {@link AbstractPlayerLog}
     */
    public void producer(AbstractPlayerLog logObj){
        _exec.submit(()-> sendMsg(makeLogMsg(logObj)));
    }

    /**
     * 生产消息
     * @param functionId  消息ID
     * @param msg         消息体json格式
     */
    public void producer(int functionId, String msg){
        if (StringUtils.isEmpty(msg)){
            return;
        }
        _exec.submit(()-> sendMsg(makePbMsg(functionId, msg)));
    }

    /**
     * 构建pb消息对象
     * @param functionId 消息ID
     * @param msg        消息体
     * @return
     */
    private String makePbMsg(int functionId, String msg){
        try {
            //构造消息体
            AbstractMqMsg msgBean = new AbstractMqMsg();
            msgBean.setACTION(String.valueOf(functionId));
            msgBean.setLOG_TYPE(ACCOUNT);
            msgBean.setSERVER_TYPE(String.valueOf(Config.GAME_ID));
            msgBean.setSERVER_ID(Config.GAME_SERID);
            msgBean.setUID("0");
            msgBean.setMSG(msg);
            msgBean.setDATETIME(format.format(new Date()));

            //序列化Json
            return gson.toJson(msgBean);
        }catch (Exception ex ){
            log.error("发送rabbitMq日志失败",ex);
        }
        return null;
    }

    /**
     * 构建log消息对象
     * @param logObj {@link AbstractPlayerLog}
     * @return
     */
    private String makeLogMsg(AbstractPlayerLog logObj){
        try {
            if (logObj == null) return null;

            //构造消息体
            AbstractMqMsg msgBean = new AbstractMqMsg();
            msgBean.setACTION(logObj.getReason());
            msgBean.setLOG_TYPE(ACCESS);
            msgBean.setSERVER_TYPE(String.valueOf(Config.GAME_ID));
            msgBean.setSERVER_ID(Config.GAME_SERID);
            msgBean.setUID(logObj.getOpenId());
            msgBean.setMSG(gson.toJson(logObj));
            msgBean.setDATETIME(format.format(new Date()));

            //序列化Json
            String msg = gson.toJson(msgBean);
            return msg;
        }catch (Exception ex ){
            log.error("发送rabbitMq日志失败",ex);
        }
        return null;
    }

    /**
     * 发送到消息队列
     * @param msg
     */
    private void sendMsg(String msg){
        try {
            if (StringUtils.isEmpty(msg)) return;
            log.debug("发送消息到消息队列：（已弃用）");
//            sendRabbitmqMsg(msg);
        }catch (Exception ex){
            ex.printStackTrace();
            log.error("发送消息失败,msg = {},{}",ex, ex.getCause());
        }
    }

    /**
     * 发送
     * @param msg 发送对象
     * @throws Exception
     */
    private void sendRabbitmqMsg(String msg){
        Connection connection = null;
        Channel channel = null;
        try {
            connection = factory.newConnection();
            if (connection == null){
                log.error("发送消息到rabbitmq失败, connection = null");
                return;
            }

            channel = connection.createChannel();
            if (channel == null){
                log.error("发送消息到rabbitmq失败, channel = null");
                return;
            }

            channel.queueDeclare(ROUTING_KEY,true,false,false,null);
            channel.basicPublish(EXCHANGE, ROUTING_KEY, null, msg.getBytes(CHARSET));
            log.debug("消息队列生产成功rabbitmq exchange={}.routekey={}.msg = {}", EXCHANGE, ROUTING_KEY, msg);
        }catch (Exception e){
            log.error("发送到消息队列失败", e);
        }finally {
            try {
                if (channel != null){
                    channel.close();
                }

                if (connection != null) {
                    connection.close();
                }
            }catch (Exception e){
                log.error("close 失败 ", e);
            }
        }
    }

}
