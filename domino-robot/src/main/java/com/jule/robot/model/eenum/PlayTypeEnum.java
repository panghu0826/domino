package com.jule.robot.model.eenum;

public enum PlayTypeEnum {
    classic(1,91001001), //经典
    Joker(2,91001002),//固定有混牌
    hukam(3,91001003),//随机牌当混
    changecard(4,91001004),//换牌当混
    Blind(5,91001005),//盲牌
    Dealer(6,91001006);//自定义玩法



    private final int value;

    private final int gameId;

    PlayTypeEnum(int value , int gameId) {
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
}
