package com.jule.domino.base.enums;

public enum RedisChannel {
    JOIN_TABLE_CHANNEL(0,"JOIN_TABLE_CHANNEL"),
    STAND_UP_TABLE_CHANNEL(1,"STAND_UP_TABLE_CHANNEL"),
    GAME_GM_NOTICE_CHANNEL(2,"GAME_GM_NOTICE_CHANNEL"),
    LEAVE_TABLE_CHANNEL(3,"LEAVE_TABLE_CHANNEL"),
    CREATE_NEW_TABLE_CHANNEL(4,"CREATE_NEW_TABLE_CHANNEL"),
    CHANGE_TABLE_CHANNEL(5,"CHANGE_TABLE_CHANNEL"),
    CHANGE_TABLE_CHANNEL_RESULT(6,"CHANGE_TABLE_CHANNEL_RESULT"),
    PAY_NOTICE(7,"PAY_NOTICE"),
    REPEAT_LOGIN_CHANNEL(8,"REPEAT_LOGIN_CHANNEL"),
    MAIL_NEW_CONFIG(9,"MAIL_NEW_CONFIG"),
    MAIL_ATTACHMENT(10,"MAIL_ATTACHMENT"),
    MAIL_NOTICE_NEW_MAIL(11,"MAIL_NOTICE_NEW_MAIL"),
    Add_NOTICE(12,"AD_NOTICE"),
    DESTROY_TABLE_CHANNEL(13,"DESTROY_TABLE_CHANNEL"),
    ;
    private int index;
    private String channelName;
    RedisChannel(int index, String channelName){
        setIndex( index );
        setChannelName(channelName);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

}
