package com.jule.domino.game.service.beans;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.domino.game.model.PlayerInfo;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class NoticePlatformScoreBean {
    private PlayerInfo player;
    private String behavior;
    private double addScore;
    private boolean isAdd = false;
    private JoloGame.JoloGame_TablePlay_PlayerSettleInfo reportedWin = null;
}
