package com.jule.db.entities;

import com.jule.db.dao.BaseDbBean;

import javax.persistence.*;

@Entity
@Table(name = "room_config")
public class RoomConfigModel extends BaseDbBean{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;//序列,用以数据库修改数据

    @Column(name = "room_id")
    private String roomId; //房间ID

    @Column(name = "min_join_table")
    private long minScore4JoinTable; //入桌的最小积分

    @Column
    private long ante; //底注

    @Column(name = "first_bet_score")
    private long firstBaseBetScore; //第一次下注的基础注

    @Column(name = "max_bet_score")
    private long maxBetScore; //最大下注额度，任何轮次的下注额不能大于此值

    @Column(name = "service_charge")
    private double serviceChargeRate; //服务费比率

    @Column(name = "max_blind_round")
    private int maxBlindRound; //盲牌最多可以进行的轮次数

    @Column(name = "max_pot")
    private long maxPot; //总封：底池最大的累积积分数，达到此值，全部玩家自动开牌，进行牌局结算

    @Column
    private int tip_value;//荷官打赏数量

    @Column
    private long change_fee;//换牌所需费用

    @Column
    private int change_fee_tips;//换牌所收取的服务费


    public Integer getId(){
        return id;
    }

    @Override
    public void setId( long id ) {
        this.id = Integer.valueOf(String.valueOf(id));
    }

    public void setId( int id ) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId( String roomId ) {
        this.roomId = roomId;
    }

    public long getMinScore4JoinTable() {
        return ante*4;
    }

    public void setMinScore4JoinTable( long minScore4JoinTable ) {
        this.minScore4JoinTable = minScore4JoinTable;
    }

    public long getAnte() {
        return ante;
    }

    public void setAnte( long ante ) {
        this.ante = ante;
    }

    public long getFirstBaseBetScore() {
        return firstBaseBetScore;
    }

    public void setFirstBaseBetScore( long firstBaseBetScore ) {
        this.firstBaseBetScore = firstBaseBetScore;
    }

    public long getMaxBetScore() {
        return maxBetScore;
    }

    public void setMaxBetScore( long maxBetScore ) {
        this.maxBetScore = maxBetScore;
    }

    public double getServiceChargeRate() {
        return serviceChargeRate;
    }

    public void setServiceChargeRate( double serviceChargeRate ) {
        this.serviceChargeRate = serviceChargeRate;
    }

    public int getMaxBlindRound() {
        return maxBlindRound;
    }

    public void setMaxBlindRound( int maxBlindRound ) {
        this.maxBlindRound = maxBlindRound;
    }

    public long getMaxPot() {
        return maxPot;
    }

    public void setMaxPot( long maxPot ) {
        this.maxPot = maxPot;
    }

    public int getTip_value() {
        return tip_value;
    }

    public void setTip_value( int tip_value ) {
        this.tip_value = tip_value;
    }

    public long getChange_fee() {
        return change_fee;
    }

    public void setChange_fee( long change_fee ) {
        this.change_fee = change_fee;
    }

    public int getChange_fee_tips() {
        return change_fee_tips;
    }

    public void setChange_fee_tips( int change_fee_tips ) {
        this.change_fee_tips = change_fee_tips;
    }
}
