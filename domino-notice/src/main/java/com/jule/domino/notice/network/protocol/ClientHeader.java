package com.jule.domino.notice.network.protocol;

public final class ClientHeader {
    public final static ClientHeader DEFAULT_HEADER = new ClientHeader(0,0,0,true,0,0);


    public final int functionId;
    public final int gameId;
    public final int gameServerId;
    public final boolean isAsync;
    public final int reqNum;
    public final long channelId;

    public ClientHeader(int functionId, int gameId, int gameServerId, boolean isAsync, int reqNum, long channelId) {
        this.functionId = functionId;
        this.gameId = gameId;
        this.gameServerId = gameServerId;
        this.isAsync = isAsync;
        this.reqNum = reqNum;
        this.channelId = channelId;
    }
}
