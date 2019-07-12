package com.jule.domino.game.play.impl;

import com.google.common.primitives.Ints;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.base.model.RoomTableRelationModel;
import com.jule.domino.game.gameUtil.NNGameLogic;
import com.jule.domino.game.model.CardConstent;
import com.jule.domino.game.model.NiuNiuPoker;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.holder.CardOfTableHolder;
import com.jule.domino.game.service.holder.CardValueHolder;
import com.jule.domino.game.service.holder.CommonConfigHolder;
import com.jule.domino.game.service.holder.RoomConfigHolder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NiuNiuTable extends AbstractTable {

    public NiuNiuTable(String gameId, String roomId, String tableId,int playerNum) {
        setPlayType(1);
        setGameType(Integer.parseInt(gameId));
        setRoomId(roomId);
        setTableId(tableId);
        roomTableRelation = new RoomTableRelationModel(gameId, roomId, tableId, TableStateEnum.IDEL.getValue());
        //初始化空座位
        for (int i = 1; i <= playerNum; i++) {
            addNullSeat(i);
        }
        log.info("新建桌子完成，tableId：{}， playerNum：{}", tableId, playerNum);
        setCommonConfig(CommonConfigHolder.getInstance().getCommonConfig(Integer.parseInt(gameId)));
        setRoomConfig(RoomConfigHolder.getInstance().getRoomConfig(roomId));
        initTableStateAttribute();
        tableStateEnum = TableStateEnum.IDEL;
        setTableStatus();
    }

    @Override
    public void setPlayerHandCards() {
        Iterator<PlayerInfo> iter = inGamePlayers.values().iterator();
        int[] maxHandCards = null;
        PlayerInfo maxCardPlayer = null;
        if (controlCardTypePlayerId != null) {//找寻桌子开启了特殊功能的玩家
            maxCardPlayer = getPlayer(controlCardTypePlayerId);
            if(maxCardPlayer.getSeatNum() > 0) {//特殊功能不能观战开启
                log.info("最大牌型发给谁：{}", maxCardPlayer.toSitDownString());
                maxHandCards = CardOfTableHolder.TakeCardOperationObj(currGameOrderId).hair_card(5);
            }
        }
        while (iter.hasNext()) {
            PlayerInfo player = iter.next();
            if (maxHandCards != null && controlCardTypePlayerId.equals(player.getPlayerId())) {
                continue;
            }
            //根据玩法的不同获取不同数量的手牌
            int[] arrHandCards = CardOfTableHolder.TakeCardOperationObj(currGameOrderId).hair_card(5);
            if (maxHandCards != null) {
                NiuNiuPoker texasA = new NiuNiuPoker(NNGameLogic.typeConversion(maxHandCards),getSpecialCardType());
                NiuNiuPoker texasB = new NiuNiuPoker(NNGameLogic.typeConversion(arrHandCards),getSpecialCardType());
                if (texasA.compare(texasB) == -1) {
                    player.setControlCards(maxHandCards);
                    maxHandCards = arrHandCards;
                } else {
                    player.setControlCards(arrHandCards);
                }
            } else {
                player.setControlCards(arrHandCards);
            }
        }
        if (maxHandCards != null) {
            maxCardPlayer.setControlCards(maxHandCards);
        }
    }

    //发牌并设置玩家牌数组
    @Override
    public void pressCard(boolean isFirstRound,boolean allCards) {
        Iterator<PlayerInfo> iter = inGamePlayers.values().iterator();
        while (iter.hasNext()) {
            PlayerInfo playerInfo = iter.next();
            //根据玩法的不同获取不同数量的手牌
            int[] arr = playerInfo.getControlCards();
            log.info("发牌前数组："+CardValueHolder.getCardValueString(arr[0])+","+
                    CardValueHolder.getCardValueString(arr[1])+","+
                    CardValueHolder.getCardValueString(arr[2])+","+
                    CardValueHolder.getCardValueString(arr[3])+","+
                    CardValueHolder.getCardValueString(arr[4]));
            if(isFirstRound){//第一次发四张牌
                playerInfo.setHandCards(new int[]{arr[0],arr[1],arr[2],arr[3]});
            }else {
                playerInfo.setHandCards(arr);
            }
        }
    }

    /**
     * 获得下一个行动的玩家
     *
     * @param preBetSeatNum
     * @return
     */
    public PlayerInfo getNextBetPlayer(int preBetSeatNum) {
        PlayerInfo nextBetPlayer = null;
        //计算下一个下注玩家的座位号
        int nextBetSeatNum = preBetSeatNum;
        while (true) {
            nextBetSeatNum += 1;
            if (nextBetSeatNum > playerNum) {
                nextBetSeatNum = 1;
            }
            if (this.getInGamePlayersBySeatNum().containsKey(nextBetSeatNum)) {
                nextBetPlayer = this.getInGamePlayers().get(nextBetSeatNum);
                break;
            }
        }
        return nextBetPlayer;
    }

    @Override
    public void initTableStateAttribute() {
        //初始化牌桌配置
        super.initTableStateAttribute();
        try {
            this.setTableAlreadyBetScore(0);
            this.equalScore.clear();//积分集合清除
            this.roundTableScore = 0;//轮次下注额重置
            firstReadyPlayer = null;
        } catch (Exception e) {
        }
    }

    public static int cardTypeMultiple(int doubleRule,int cardType) {
        switch (cardType){
            case 18:
                return 10;
            case 17:
                return 9;
            case 16:
                return 8;
            case 15:
                return 7;
            case 14:
                return 6;
            case 13:
                return 5;
            case 12:
                return 5;
            case 11:
                return 4;
            case 10:
                if(doubleRule == 1)return 4;
                if(doubleRule == 2)return 3;
            case 9:
                if(doubleRule == 1)return 3;
                if(doubleRule == 2)return 2;
            case 8:
                if(doubleRule == 1)return 2;
                if(doubleRule == 2)return 1;
            case 7:
                if(doubleRule == 1)return 2;
                if(doubleRule == 2)return 1;
        }
        return 1;
    }
}
