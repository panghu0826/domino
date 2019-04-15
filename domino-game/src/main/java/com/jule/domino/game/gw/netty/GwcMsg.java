package com.jule.domino.game.gw.netty;

import lombok.Getter;
import lombok.Setter;

/**
 * gwc消息体 <br/>
 * <br/>
 * 1、网关控制器和服务器之间交互的协议格式 <br/>
 * 2、约定使用小端模式 <br/>
 * 3、消息格式  length(4字节) cmd(4字节) data(protobuf)  <br/>
 *
 * @author
 *
 * @since 2018/11/22 15:57
 *
 */
@Setter@Getter
public class GwcMsg {

    //消息长度
    private int len;
    //消息号
    private int cmd;
    //消息体
    private byte[] body;

}
