package com.jule.domino.base.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameConst {
    /**
     * 桌上最后1人进行结算工作
     */
    public static final int FINAL_PLAYER_NUM = 1;
    /**
     * 是否全发
     */
    public static final int IS_SEND_TO_ALL = 1;
    /**
     * 桌面最大玩家数
     */
    public static final int TABLE_MAX_PLAYER_NUM = 6;

    /**
     * 缓存的玩家玩法类型
     */
    public static final String CACHE_PLAY_TYPE = "table_cache_prefix_";

    /**
     * 缓存的玩家登录token
     */
    public static final String CACHE_USER_TOKEN = "token_cache_prefix_";

    /**
     * 缓存的服务器维护状态
     */
    public static final String CACHE_SERVER_STATE = "server_state_cache_prefix_";

    /**
     * 缓存广告配置
     */
    public static final String CACHE_AD_CONFIGS = "ad_config_prefix_";

    /**
     * 胜负标识
     */
    public static final String isWon = "1";

    public static String localHost = "";
    /**
     * 是否强制允许客户端断线后，还能连接到游戏服务器进行游戏续玩
     */
    public static final boolean mustReconnectContinueGame = true;

    //redis 排序
    public final static String GAME_STATE_ = "GAME_STATE_";
    //用户与GateSvr建立连接的映射关系KEY
    public final static String USER_LINK_INFO = "USER_LINK_INFO_";

    public static final boolean data2Redis = false;
    //Game多少秒检测不到被认为下线
    public static final int offlineGameSec = 15;
    /**
     * 任务完成标识
     */
    public static final int TaskFinished = 1;
    public static final String GOODS_GOLD = "money";
    public static final String GOODS_ITEM = "item";
    public static final int COST_TIME = 1000;
    //特殊牌型:1.对子4,2.顺子5,3.五花5,4.同花6,5.葫芦7,6.炸弹8,7.五小9,8.顺金10
    public static List<String> wanFa = Arrays.asList("1","2","3","4","5","6","7","8");
    public static boolean isTest = false;

}
