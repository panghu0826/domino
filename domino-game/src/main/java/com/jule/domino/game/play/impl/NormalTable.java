package com.jule.domino.game.play.impl;


import com.jule.domino.game.model.CardConstent;
import com.jule.domino.game.play.AbstractTable;

public class NormalTable extends AbstractTable {
    public NormalTable(String gameId, String roomId, String tableId) {
        super(gameId, roomId, tableId);
    }

    @Override
    public int giveCardCounts() {
        return CardConstent.HAND_CARDS;
    }
}
