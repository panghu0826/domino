package com.jule.domino.game.vavle.notice.protocol;

import com.google.protobuf.MessageLite;

public class MuliMsgReq extends NoticeReq {
    public static final int FUNCTION_ID = 10001;

    /**
     * @param messageLite
     */
    public MuliMsgReq(int gameId,MessageLite messageLite) {
        super(gameId,FUNCTION_ID, messageLite);
    }
}
