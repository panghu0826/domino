package com.jule.domino.game.model.eenum;

import java.util.Arrays;
import java.util.List;

public enum PlayTypeEnum {
    classic(1,71001001), //经典
    domino(10,620001),  //通比牌九
    ;

    private final int value;

    private final int gameId;

    PlayTypeEnum( int value ,int gameId) {
        this.value = value;
        this.gameId = gameId;
    }

    public int getIndex (){
        return value;
    }

    public int getValue() {
        return gameId;
    }

    public int getGameId() {
        return gameId;
    }

    public static int getGameByType(int type){
        for (PlayTypeEnum e : PlayTypeEnum.values()) {
            if (e == null) {
                continue;
            }
            if (e.getIndex() == type) {
                return e.getGameId();
            }
        }
        return 0;
    }

    public static PlayTypeEnum parseGame(int gameId){
        for (PlayTypeEnum e : PlayTypeEnum.values()) {
            if (e == null) {
                continue;
            }
            if (e.getGameId() == gameId) {
                return e;
            }
        }
        return null;
    }

    public static List<String> getPlayTypes(){
        return Arrays.asList(
                classic.getValue()+"");
    }
}
