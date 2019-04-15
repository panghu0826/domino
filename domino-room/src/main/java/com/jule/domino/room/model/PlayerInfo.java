package com.jule.domino.room.model;

import com.jule.domino.base.enums.PlayerStateEnum;

import java.util.Arrays;

/**
 * 玩家信息
 * 描述：存储玩家在玩牌过程中的信息，如果一个玩家同时存在于两个桌子，那么将有两个相同playerId 的PlayerInfo，用tableId 进行区分存在于哪个桌子。
 */
public class PlayerInfo {
    private String playerId;//玩家id 和userI都一样
    private String roomId; //房间ID
    private String tableId;//用户桌子Id
    private String nickName;//显示的昵称
    private String icon; //玩家头像URL
    private boolean isOffLine = false;//是否已经掉线
    private int seatNum; //玩家座位号
    private long playScoreStore;//玩家牌桌上积分的携带量

    //region 玩家在每一局的玩牌信息，每一局开始时重置掉
    private int isBlind; //是否盲牌
    private int isDealer; //是否庄家
    private int isCurrActive; //是否当前行动玩家
    private long alreadyBetScore4Round; //当前轮次已经下注的积分量 (非累积值，每轮次重新计算)
    private PlayerStateEnum state;//玩家状态 0/旁观 1/入座 2/游戏中 3/已下注 4/已开牌
    private int[] handCards = null;//牌数组的下标
    private int isCanSideShow; //玩家是否可以与某个用户比边牌
    private int isCanShow; //玩家是否可以比牌结算
    private long totalAlreadyBetScore4Hand; //当前牌局累积已下注金额，方便后续结算时判断用户输赢
    private long winLoseScore4Hand; //当前牌局输赢积分数量
    //endregion

    //region 此部分用于统计玩家玩牌时长，输赢积分
    private long joinTableTime; //加入牌桌时间
    private long leaveTableTime; //离开牌桌时间
    private long sitDownTime; //入座时间
    private long standUpTime; //站起时间
    private int totalPlayMinutes; //合计的玩牌时间（分钟数）
    private long totalWinLoseScore; //合计的输赢积分数
    //endregion

    public PlayerInfo(String roomId, String tableId, String playerId, String nickName, String icon) {
        this.roomId = roomId;
        this.tableId = tableId;
        this.playerId = playerId;
        this.nickName = nickName;
        this.icon = icon;
        this.isBlind = 1;
    }

    /**
     * 重置游戏中信息
     */
    public void resetGameingInfo(){
        this.isBlind = 1;
        this.isDealer = 0;
        this.isCurrActive = 0;
        this.alreadyBetScore4Round = 0;
        this.state = PlayerStateEnum.game_ready;
        this.handCards = null;
        this.isCanSideShow = 0;
        this.isCanShow = 0;
        this.totalAlreadyBetScore4Hand = 0;
        this.winLoseScore4Hand = 0;
    }

    //region only getter methods
    public String getPlayerId() {
        return playerId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getTableId() {
        return tableId;
    }

    public String getIcon() {
        return icon;
    }

    public String getNickName() {
        return nickName;
    }
    //endregion

    //region getter/setter methods
    public boolean isOffLine() {
        return isOffLine;
    }

    public void setOffLine(boolean offLine) {
        isOffLine = offLine;
    }

    public int getSeatNum() {
        return seatNum;
    }

    public void setSeatNum(int seatNum) {
        this.seatNum = seatNum;
    }

    public long getPlayScoreStore() {
        return playScoreStore;
    }

    public void setPlayScoreStore(long playScoreStore) {
        this.playScoreStore = playScoreStore;
    }

    public PlayerStateEnum getState() {
        return state;
    }

    public void setState(PlayerStateEnum state) {
        this.state = state;
    }

    public long getJoinTableTime() {
        return joinTableTime;
    }

    public void setJoinTableTime(long joinTableTime) {
        this.joinTableTime = joinTableTime;
    }

    public long getLeaveTableTime() {
        return leaveTableTime;
    }

    public void setLeaveTableTime(long leaveTableTime) {
        this.leaveTableTime = leaveTableTime;
    }

    public long getSitDownTime() {
        return sitDownTime;
    }

    public void setSitDownTime(long sitDownTime) {
        this.sitDownTime = sitDownTime;
    }

    public long getStandUpTime() {
        return standUpTime;
    }

    public void setStandUpTime(long standUpTime) {
        this.standUpTime = standUpTime;
    }

    public int getTotalPlayMinutes() {
        return totalPlayMinutes;
    }

    public void setTotalPlayMinutes(int totalPlayMinutes) {
        this.totalPlayMinutes = totalPlayMinutes;
    }

    public long getTotalWinLoseScore() {
        return totalWinLoseScore;
    }

    public void setTotalWinLoseScore(long totalWinLoseScore) {
        this.totalWinLoseScore = totalWinLoseScore;
    }

    public int getIsBlind() {
        return isBlind;
    }

    public void setIsBlind(int isBlind) {
        this.isBlind = isBlind;
    }

    public int getIsDealer() {
        return isDealer;
    }

    public void setIsDealer(int isDealer) {
        this.isDealer = isDealer;
    }

    public int getIsCurrActive() {
        return isCurrActive;
    }

    public void setIsCurrActive(int isCurrActive) {
        this.isCurrActive = isCurrActive;
    }

    public int getIsCanSideShow() {
        return isCanSideShow;
    }

    public void setIsCanSideShow(int isCanSideShow) {
        this.isCanSideShow = isCanSideShow;
    }

    public int getIsCanShow() {
        return isCanShow;
    }

    public void setIsCanShow(int isCanShow) {
        this.isCanShow = isCanShow;
    }

    public void setHandCards(int[] handCards){
        this.handCards = handCards;
    }

    public int[] getHandCards() {
        return handCards;
    }

    public long getAlreadyBetScore4Round() {
        return alreadyBetScore4Round;
    }

    public void setAlreadyBetScore4Round(long alreadyBetScore4Round) {
        this.alreadyBetScore4Round = alreadyBetScore4Round;
    }

    public long getTotalAlreadyBetScore4Hand() {
        return totalAlreadyBetScore4Hand;
    }

    public void setTotalAlreadyBetScore4Hand(long totalAlreadyBetScore4Hand) {
        this.totalAlreadyBetScore4Hand = totalAlreadyBetScore4Hand;
    }

    public long getWinLoseScore4Hand() {
        return winLoseScore4Hand;
    }

    public void setWinLoseScore4Hand(long winLoseScore4Hand) {
        this.winLoseScore4Hand = winLoseScore4Hand;
    }

    //endregion

    @Override
    public String toString() {
        return "Player{" +
                "playerId='" + playerId + '\'' +
                ", tableId='" + tableId + '\'' +
                ", nickName='" + nickName + '\'' +
                ", icon='" + icon + '\'' +
                ", isOffLine=" + isOffLine +
                ", seatNum=" + seatNum +
                ", playScoreStore=" + playScoreStore +
                ", isBlind=" + isBlind +
                ", alreadyBetScore=" + alreadyBetScore4Round +
                ", state=" + state +
                ", handCards=" + Arrays.toString(handCards) +
                ", joinTableTime=" + joinTableTime +
                ", leaveTableTime=" + leaveTableTime +
                ", sitDownTime=" + sitDownTime +
                ", standUpTime=" + standUpTime +
                ", totalPlayMinutes=" + totalPlayMinutes +
                ", totalWinLoseScore=" + totalWinLoseScore +
                '}';
    }
}
