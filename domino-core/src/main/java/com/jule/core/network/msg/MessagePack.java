package com.jule.core.network.msg;

import lombok.Getter;
import lombok.Setter;

/**
 * 消息封包
 * @author
 * @since 2018/11/19 17:32
 */
@Setter@Getter
public class MessagePack {

    //消息头
    private int length;

    private int functionId;

    private int gameId;

    private int gameSvrId;

    private boolean isAsync;

    private int reqNum;

    private long channelId = 0;

    //消息体
    private byte[] body;

    /**
     * 默认构造
     */
    public MessagePack() {
    }

    /**
     * Game全参构造
     * @param functionId
     * @param gameId
     * @param gameSvrId
     * @param isAsync
     * @param reqNum
     * @param body
     */
    public MessagePack(int functionId, int gameId, int gameSvrId, boolean isAsync, int reqNum, long channelId, byte[] body) {
        int len = body == null ? 0 : body.length;
        this.length = len + 28;
        this.functionId = functionId;
        this.gameId = gameId;
        this.gameSvrId = gameSvrId;
        this.isAsync = isAsync;
        this.reqNum = reqNum;
        this.channelId = channelId;
        this.body = body;
    }

    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("length=").append(length).append(",");
        buffer.append("functionId=").append(functionId).append(",");
        buffer.append("gameId=").append(gameId).append(",");
        buffer.append("gameSvrId=").append(gameSvrId).append(",");
        buffer.append("isAsync=").append(isAsync).append(",");
        buffer.append("reqNum=").append(reqNum).append(",");
        buffer.append("body=").append(body);

        return buffer.toString();
    }
}
