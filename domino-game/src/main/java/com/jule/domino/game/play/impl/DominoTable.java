package com.jule.domino.game.play.impl;

import com.jule.domino.game.model.CardConstent;
import com.jule.domino.game.play.AbstractTable;

public class DominoTable extends AbstractTable {
    public DominoTable(String gameId, String roomId, String tableId) {
        super(gameId, roomId, tableId);
    }
    @Override
    public int giveCardCounts() {
        return CardConstent.DOMINO_HAND_CARDS;
    }
}
