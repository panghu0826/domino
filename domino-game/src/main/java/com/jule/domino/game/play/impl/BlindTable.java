package com.jule.domino.game.play.impl;

import com.jule.domino.game.model.CardConstent;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.model.eenum.NextBetPlayerReasonEnum;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.base.enums.GameConst;

public class BlindTable extends AbstractTable {

    public BlindTable(String gameId, String roomId, String tableId) {
        super(gameId, roomId, tableId);
    }

    @Override
    public int giveCardCounts() {
        return CardConstent.BLINDS_HAND_CARDS;
    }

}
