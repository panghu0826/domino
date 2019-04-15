package com.jule.domino.game.model.eenum;

public enum NextBetPlayerReasonEnum {
    FOLD(1,"fold"),
    SIDE_SHOW(2,"side_show"),
    SIDE_SHOW_CHOOSE_USER(3,"side_show_choose_user"),
    SIDE_SHOW_ALLOW(4,"side_show_allow"),
    DEALER(5,"dealer"),
    GAME_LOGIC_START(6,"game_logic_start"),
    GAME_LOGIC_BET(7,"game_logic_bet"),
    GAME_LOGIC_PLAYER_DATA_SETTLEMENT(8,"game_logic_player_data_settlement"),
    ;
    private int index;
    private String reason;
    NextBetPlayerReasonEnum(int index,String reason){
        this.index = index;
        this.reason = reason;
    }

    public int getIndex() {
        return index;
    }

    public String getReason() {
        return reason;
    }
}
