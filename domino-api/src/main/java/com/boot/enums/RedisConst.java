package com.boot.enums;

public enum RedisConst {
    /**获取一个新的tableID  String*/
    NEW_TABLE_ID(0,"NEW_TABLE_ID_KEY_ROOM_ID_",""),
    /**通过玩家找到相应的桌子和位置*/
    USER_SEAT(4,"USER_SEAT_KEY_USER_ID_",""),
    /**最高的一次胜利*/
    CHIPS_WON(5,"CHIPS_WON_USER_ID_",""),
    /**任务加载开关*/
    GAME_TASK_LOADING(100,"GAME_TASK_LOADING_KEY","GAME_TASK_LOADING_VAL"),
    /**邮件配置加载开关*/
    MAIL_CONFIG_LOADING(101,"MAIL_CONFIG_LOADING_KEY","MAIL_CONFIG_LOADING_VAL"),
    /**玩家邮件加载开关*/
    MAIL_USER_LOADING(102,"MAIL_USER_LOADING_KEY","MAIL_USER_LOADING_VAL"),
    /**邮件配置加载开关*/
    MAIL_ADD_NEW_CONFIG(103,"MAIL_ADD_NEW_CONFIG_KEY","MAIL_ADD_NEW_CONFIG_VAL"),
    /**游戏服活跃状态0:活跃1:error*/
    GAME_STATUS_ACTIVE(104,"GAME_STATUS_ACTIVE_KEY_IP_","GAME_STATUS_ACTIVE_VAL"),
    /**游戏服活跃状态0:活跃1:error*/
    GAME_STATUS_ACTIVE_SWITCH(105,"GAME_STATUS_ACTIVE_SWITCH_KEY_IP_","GAME_STATUS_ACTIVE_SWITCH_VAL"),
    //////////////////////////HASH///////////////////////
    /**获取table信息*/
    TABLE_INSTANCE(1,"TABLE_INSTANCE_KEY_ROOM_ID_","TABLE_INSTANCE_FIELD_TABLE_ID_"),
    /**获取table下的User信息*/
    TABLE_USERS(2,"TABLE_USERS_KEY_TABLE_ID_","TABLE_USERS_FIELD_USER_ID_"),
    /**table座位上有没有人(主要用途判断这个桌子是否还能加入玩家)*/
    TABLE_SEAT(3,"TABLE_SEAT_KEY_TABLE_ID_","TABLE_SEAT_FIELD_SEAT_ID_"),
    /**玩家与桌子对应关系（GameRoomSeat）*/
    USER_TABLE_SEAT(7,"USER_TABLE_SEAT_KEY","USER_TABLE_SEAT_KEY_USER_ID"),
    /**桌子对应游戏服务器*/
    TABLE_GAME_SERVER(8,"TABLE_GAME_SERVER","TABLE_GAME_SERVER__KEY_TABLE_ID_"),
    /**缓存玩家信息*/
    USER_INFO(11,"USER_INFO","USER_INFO_FIELD_USER_ID"),
    /**1正在0结束*/
    CHANGE_TABLE_STAT(12,"CHANGE_TABLE_STAT","CHANGE_TABLE_STAT_FIELD_USER_ID"),
    /**dispacher登录连接*/
    LINK_DISPACHER_STAT(13,"LINK_DISPACHER_STAT","LINK_DISPACHER_STAT_FIELD_USER_ID"),
    /**gate登录连接*/
    LINK_GATE_STAT(14,"LINK_GATE_STAT","LINK_GATE_STAT_FIELD_USER_ID"),
    /**game登录连接*/
    USER_LOGIN_GAME_URL(15,"USER_LOGIN_GAME_URL_","USER_LOGIN_GAME_URL_"),
    /**Hsetnx 消息有没有执行 唯一key*/
    HASH_SET_NX(16,"HASH_SET_NX_","REQ_EXC_FIELD_"),
    /**游戏服过期*/
    GAME_SVR_EXPIRE(17,"GAME_SVR_EXPIRE_KEY_GAME_ID_","GAME_SVR_EXPIRE_FIELD_ADDRESS_SVR_ID_"),
    /**玩家任务*/
    USER_TASK_LIST(18,"USER_TASK_LIST","USER_TASK_LIST_FIELD_USER_ID_"),
    /**任务配置-任务线*/
    TASK_CONFIG_LINE(19,"TASK_CONFIG_LINE","TASK_CONFIG_LINE_FIELD_LINE_ID_"),
    /**任务配置-任务线与任务关系(线Id+任务Id)*/
    TASK_CONFIG_RELATION(20,"TASK_CONFIG_RELATION","TASK_CONFIG_RELATION_FIELD_LINE_TASK_ID_"),
    /**任务配置-任务线与任务关系(线Id)*/
    TASK_CONFIG_RELATION_LINE(21,"TASK_CONFIG_RELATION_LINE","TASK_CONFIG_RELATION_LINE_FIELD_LINE_ID_"),
    /**任务配置-任务*/
    TASK_CONFIG_TASK(22,"TASK_CONFIG_TASK","TASK_CONFIG_TASK_FIELD_TASK_ID_"),
    /**任务配置-任务奖励*/
    TASK_CONFIG_AWARD(23,"TASK_CONFIG_AWARD","TASK_CONFIG_AWARD_FIELD_TASK_CONFIG_ID_"),
    /**任务状态*/
    USER_TASK_STAT(24,"USER_TASK_STAT","USER_TASK_STAT_FIELD_USER_ID_"),
    /**邮件服过期*/
    MAIL_SVR_EXPIRE(25,"MAIL_SVR_EXPIRE_KEY_MAIL_SVR_ID_","MAIL_SVR_EXPIRE_FIELD_ADDRESS_SVR_ID_"),
    /**邮件领取日志*/
    MAIL_USER_AWARD_LOG(26,"MAIL_USER_AWARD_LOG","MAIL_USER_AWARD_LOG_FIELD_USER_ID_"),
    /**邮件配置列表*/
    MAIL_CONFIG_LIST(27,"MAIL_CONFIG_LIST_KEY","MAIL_CONFIG_LIST_FIELD"),
    /**邮件参数配置列表*/
    MAIL_CONFIG_ARGS(28,"MAIL_CONFIG_ARGS_KEY","MAIL_CONFIG_ARGS_FIELD"),
    /**邮件附件配置列表*/
    MAIL_CONFIG_ATTACHMENT(28,"MAIL_CONFIG_ATTACHMENT_KEY","MAIL_CONFIG_ATTACHMENT_FIELD"),
    /**玩家邮件*/
    MAIL_USER(29,"MAIL_USER","MAIL_USER_FIELD_USER_ID_"),
	/**游戏服人数*/
    GAME_SVR_TOTAL_USER(30,"GAME_SVR_TOTAL_USER","TOTAL_USER_IP_"),
    /**大厅人数*/
    DISPACHER_SVR_TOTAL_USER(31,"DISPACHER_SVR_TOTAL_USER","TOTAL_USER_IP_"),
    /**游戏报警*/
    ALARM_CHECK(32,"ALARM_CHECK_KEY_","ALARM_CHECK_FIELD_"),

    //////////////////////////SET////////////////////////
    //////////////////////////LIST200///////////////////////
    HANDS_WON(6,"HANDS_WON_USER_ID_",""),
    GAME_SVR_LIST(9,"GAME_SVR_LIST",""),
    GATE_SVR_LIST(10,"GATE_SVR_LIST",""),
    MAIL_SVR_LIST(200,"MAIL_SVR_LIST",""),
            ;

    private int index;
    private String profix;
    private String field;
    private RedisConst(int index, String profix, String field){
        this.index = index;
        this.profix = profix;
        this.field = field;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getProfix() {
        return profix;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setProfix(String profix) {
        this.profix = profix;
    }

}
