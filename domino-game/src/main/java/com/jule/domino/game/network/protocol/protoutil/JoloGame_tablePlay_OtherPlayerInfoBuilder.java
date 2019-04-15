package com.jule.domino.game.network.protocol.protoutil;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.model.PlayerInfo;

public class JoloGame_tablePlay_OtherPlayerInfoBuilder {
    public static JoloGame.JoloGame_TablePlay_OtherPlayerInfo.Builder getOtherPlayerInfo(PlayerInfo player) {
        JoloGame.JoloGame_TablePlay_OtherPlayerInfo.Builder otherPlayerInfo = JoloGame.JoloGame_TablePlay_OtherPlayerInfo.newBuilder();
        otherPlayerInfo.setUserId("");
        otherPlayerInfo.setChipsWon(0);
        otherPlayerInfo.setHandsTimes(0);
        otherPlayerInfo.setNickName("");
        otherPlayerInfo.setHandsWon(0);
        otherPlayerInfo.setPlayScoreStore(0);
        otherPlayerInfo.setIcon("");
        otherPlayerInfo.setBiggest(0);
        if (player != null) {
            otherPlayerInfo.setUserId(player.getPlayerId());
            otherPlayerInfo.setChipsWon(0);
            otherPlayerInfo.setHandsTimes(player.getHandsWon().getLinkedDeque().size());
            otherPlayerInfo.setNickName(player.getNickName());
            otherPlayerInfo.setHandsWon(player.getHandsWon().won());
            otherPlayerInfo.setPlayScoreStore(player.getPlayScoreStore());
            otherPlayerInfo.setIcon(player.getIcon());
            otherPlayerInfo.setBiggest(player.getBiggestChipsWon());
        }
        return otherPlayerInfo;
    }
}
