package com.jule.domino.game.gw.netty;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 网关控制器消息处理器
 *
 * @author
 *
 * @since 2018/11/22 16:08
 */

@Getter@Setter
public abstract class AbstractGwcHander {

    protected static final Logger log = LoggerFactory.getLogger(AbstractGwcHander.class);

    //接收消息号
    private int msgId;

    //回复消息号
    private int reMsgId;

    /**
     * 消息处理
     * @param ctx
     * @param msg
     * @throws Exception
     */
    public abstract void process(ChannelHandlerContext ctx, GwcMsg msg) throws Exception;

    /**
     * 发送消息
     * @param ctx
     * @param body
     */
    protected void sendMsg(ChannelHandlerContext ctx, byte[] body){
        GwcMsgSerivce.OBJ.encode(ctx, reMsgId, body);
    }

    /**
     * 无响应构造器
     * @param msgId
     */
    public AbstractGwcHander(int msgId) {
        this.msgId = msgId;
        //注册消息处理器
        GwcMsgSerivce.OBJ.regHandler(msgId, this);
    }

    /**
     * 有响应构造
     * @param msgId
     * @param reMsgId
     */
    public AbstractGwcHander(int msgId, int reMsgId) {
        this.msgId = msgId;
        this.reMsgId = reMsgId;
        //注册消息处理器
        GwcMsgSerivce.OBJ.regHandler(msgId, this);
    }
}
