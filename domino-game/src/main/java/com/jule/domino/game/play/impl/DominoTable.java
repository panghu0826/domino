package com.jule.domino.game.play.impl;

import com.google.common.primitives.Ints;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.base.model.RoomTableRelationModel;
import com.jule.domino.game.gameUtil.WashCard;
import com.jule.domino.game.model.CardValueModel;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.model.TexasPoker;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.holder.CardOfTableHolder;
import com.jule.domino.game.service.holder.CardValueHolder;
import com.jule.domino.game.service.holder.CommonConfigHolder;
import com.jule.domino.game.service.holder.RoomConfigHolder;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

//港式五张的桌子类
@Setter @Getter
public class DominoTable extends AbstractTable {

    public DominoTable(String gameId, String roomId, String tableId) {
        super(gameId, roomId, tableId);
    }

    public DominoTable(String gameId, String roomId, String tableId,int playerNum) {
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
                TexasPoker texasA = new TexasPoker(maxHandCards);
                TexasPoker texasB = new TexasPoker(arrHandCards);
                if (texasA.compareTo(texasB) == -1) {
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
            CardValueHolder.getCardValueString(arr[0]);
            log.info("发牌前数组："+CardValueHolder.getCardValueString(arr[0])+","+
                    CardValueHolder.getCardValueString(arr[1])+","+
                    CardValueHolder.getCardValueString(arr[2])+","+
                    CardValueHolder.getCardValueString(arr[3])+","+
                    CardValueHolder.getCardValueString(arr[4]));
            if(isFirstRound){//第一次发三张牌
                playerInfo.setHandCards(new int[]{arr[0],arr[1],arr[2]});
                playerInfo.setMingCards(new int[]{arr[2]});
            }else {
                if(allCards){//发出所有牌
                    playerInfo.setHandCards(arr);
                    playerInfo.setMingCards(new int[]{arr[2],arr[3],arr[4]});
                }else {
                    List<Integer> list = new ArrayList<>(Ints.asList(playerInfo.getHandCards()));
                    int length = playerInfo.getHandCards().length;
                    log.info("玩家当前手牌：{}", list.toString());
                    list.add(arr[length]);
                    log.info("玩家发牌后手牌：{}", list.toString());
                    playerInfo.setHandCards(list.stream().mapToInt(i -> i).toArray());
                    List<Integer> ming = new ArrayList<>(Ints.asList(playerInfo.getMingCards()));
                    ming.add(arr[length]);
                    playerInfo.setMingCards(ming.stream().mapToInt(i -> i).toArray());
                    log.info("玩家发牌后明牌：{}", ming.toString());
                }
            }
        }
    }

    //寻找第一个下注玩家
    @Override
    public void lookForFirstBetPlayer() {
        PlayerInfo firstBetPlayer = null;
        for (PlayerInfo player : inGamePlayers.values()) {
            //设置玩家明牌牌型数据
            player.setMingCardArgs(player.getMingCards());
            if (firstBetPlayer == null) {
                firstBetPlayer = player;
                continue;
            }
            if (firstBetPlayer.getMingTypeCard() < player.getMingTypeCard()) {//先比较明牌牌型 1.单排 2.对子 3.三条
                firstBetPlayer = player;
            } else if (firstBetPlayer.getMingTypeCard() == player.getMingTypeCard()) {
                CardValueModel cardA = firstBetPlayer.getMingMaxCard();
                CardValueModel cardB = player.getMingMaxCard();
                log.debug("=========当前行动玩家的昵称：{}----{}",cardA.toString(),cardB.toString());
                if (cardA.getCompareValue() < cardB.getCompareValue()) {//明牌牌型相同则比较牌面大小
                    firstBetPlayer = player;
                } else if (cardA.getCompareValue() == cardB.getCompareValue()) {//牌面大小相同则比较花色
                    if (cardA.getCardId() > cardB.getCardId()) {
                        firstBetPlayer = player;
                    }
                }
            }
        }
        if (firstBetPlayer != null) {
            currActionSeatNum = firstBetPlayer.getSeatNum();//记录当前行动者座位号
            firstGiveCardSeatNum = currActionSeatNum;
            firstBetPlayer.setState(PlayerStateEnum.beting);
            firstBetPlayer.setIsCurrActive(1);
            //记录玩家开始行动的时间
            firstBetPlayer.setStartActionTime(System.currentTimeMillis());
            log.info("当前行动玩家的昵称：{}----{}",firstBetPlayer.getNickName(),firstBetPlayer.getMingMaxCard().toString());
        } else {
            log.error("找不到第一个行动的玩家");
        }
    }

    @Override
    public void initTableStateAttribute() {
        //初始化牌桌配置
        super.initTableStateAttribute();
        try {
            this.setTableAlreadyBetScore(0);
            this.equalScore.clear();//积分集合清除
            this.roundTableScore = 0;//轮次下注额重置
        } catch (Exception e) {
        }
    }

    @Override
    public String toString() {
        return "AbstractTable{" +
                "tableId='" + tableId  +
                ", tableStateEnum=" + tableStateEnum +
                ", currGameNum=" + currGameNum +
                ", playerNum=" + playerNum +
                ", currBaseBetScore=" + currBaseBetScore +
                ", readyCd=" + readyCd +
                ", betCd=" + betCd +
                ", openCardCd=" + openCardCd +
                ", betMaxScore=" + betMaxScore +
                ", gameNum=" + gameNum +
                ", isWatch=" + isWatch +
                ", allPlayers=" + allPlayers.size() +
                ", createTableUserId=" + createTableUserId +
                ", clubId=" + clubId +
                '}';
    }
}
