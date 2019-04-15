package com.jule.domino.game.vavle.notice.protocol;

import com.google.protobuf.MessageLite;

public class SingleMsgReq extends NoticeReq {
    public static final int FUNCTION_ID = 10000;

    /**
     * @param messageLite
     */
    public SingleMsgReq(int gameId,MessageLite messageLite) {
        super(gameId,FUNCTION_ID, messageLite);
    }
}
