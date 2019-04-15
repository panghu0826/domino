package com.jule.domino.game.play;

import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.model.eenum.NextBetPlayerReasonEnum;

import java.util.List;

public interface ITable {
    //加入桌子
    void joinTable(PlayerInfo player);

    //坐下
    boolean sitDown(int seatNum, String userId);

    //站起
    boolean standUp(Integer seatNum, String userId, String standUpType);

    //返回大厅
    void returnLobby(String userId);

    //强制返回大厅
    void returnLobby(String userId, boolean force);

    //获取玩家信息
    PlayerInfo getPlayer(String playerId);

    //手牌数量
    int giveCardCounts();

    //设置GameId
    void setGameId(String gameId);

    //获取changeCard列表
    List<Integer> getChangeCards();

    //比较手牌大小
    PlayerInfo getWinnerByCompareCards(PlayerInfo player1, PlayerInfo player2);

    //结算玩家数据
    double playerDataSettlement(PlayerInfo player);
}
