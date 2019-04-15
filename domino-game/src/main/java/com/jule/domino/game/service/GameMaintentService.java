package com.jule.domino.game.service;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 游戏维护
 * @author
 * @since 2019/2/20 13:25
 */
public class GameMaintentService {

    public static final GameMaintentService OBJ = new GameMaintentService();

    //游戏开关 - 默认是关闭的，能正常游戏，如果开启，则游戏无法正常开局
    private static AtomicBoolean switched = new AtomicBoolean(false);

    /**
     * 开启维护墙
     */
    public void turnOn(){
        switched.set(true);
    }

    /**
     * 关闭维护墙
     */
    public void turnOff(){
        switched.set(false);
    }

    /**
     * 维护状态
     * true 维护中  false 正常
     */
    public boolean isDefense(){
        return switched.get();
    }

}
