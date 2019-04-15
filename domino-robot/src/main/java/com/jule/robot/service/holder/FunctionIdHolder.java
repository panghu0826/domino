package com.jule.robot.service.holder;

import com.jule.robot.config.Config;

public class FunctionIdHolder {
    public final static int GAME_ID_TeenPatti_Normal = 91001001;

    //public final static String GATE_AUTH_SVR_URI = Config.GATE_AUTH_SVR_URI;
    public final static String GATE_SVR_URI = Config.GATE_SVR_URI;

    public final static int GATE_REQ_loginUser = 600001;
    public final static int GATE_ACK_loginUser = 600001 | 0x08000000;

    public final static int Room_REQ_ApplyJoinTable_NoRoomId = 40001; //压测时使用
    public final static int Room_REQ_ApplyJoinTable = 40003;
    public final static int Room_ACK_ApplyJoinTable = 40001 | 0x08000000;

    public final static int Game_REQ_ApplyJoinTable = 50000;
    public final static int Game_REQ_ApplySitDown = 50001;
    public final static int Game_REQ_ApplyLeave = 50003;
    public final static int Game_REQ_ApplyBet = 50005;

    public final static int Game_REQ_OtherPlayerInfoReq = 50014; //查看其他玩家信息
    public final static int Game_REQ_ReconnectReq = 50016; //玩家重连
    public final static int Game_REQ_ReadyReq = 50018; //玩家准备


    public final static int Game_REQ_RewardCroupier = 50050;
    public final static int Game_REQ_GiftsList = 50051;
    public final static int Game_REQ_GiveGifts = 50052;


    public final static int Game_ACK_ApplyJoinTable = Game_REQ_ApplyJoinTable | 0x08000000;
    public final static int Game_ACK_ApplySitDown = Game_REQ_ApplySitDown | 0x08000000;
    public final static int Game_ACK_ApplyLeave = Game_REQ_ApplyLeave | 0x08000000;
    public final static int Game_ACK_ApplyBet = Game_REQ_ApplyBet | 0x08000000;

    public final static int Game_ACK_OtherPlayerInfoReq = Game_REQ_OtherPlayerInfoReq | 0x08000000;
    public final static int Game_ACK_ReconnectReq = Game_REQ_ReconnectReq | 0x08000000;
    public final static int Game_ACK_ReadyReq = Game_REQ_ReadyReq | 0x08000000;


    //region Notice
    public final static int Game_Notice_SiteDown = 51001;
    public final static int Game_Notice_StandUp = 51002;
    public final static int Game_Notice_BuyIn = 51003;
    public final static int Game_Notice_GameStart = 51004;
    public final static int Game_Notice_GiveCardRound_Start = 51005;
    public final static int Game_Notice_BetRound_WaitBet = 51006;
    public final static int Game_Notice_BetRound_DoBet = 51007;
    public final static int Game_Notice_SettleRound_SettleResult = 51013;
    public final static int Game_Notice_SettleRound_History = 51014;

    public final static int Game_Notice_leaveReq = 51023;  //广播：离桌
    public final static int Game_Notice_CardTypeReq = 51028; //广播：牌型
    public final static int Game_Notice_ReadyReq = 51029; //广播：玩家准备
    public final static int Game_Notice_FixDealerReq = 51031; //广播：定庄动画
    public final static int Game_Notice_BetInfoReq = 51032; //广播：下注前信息
    public final static int Game_Notice_HandCardsListReq = 51033; //广播：玩家手牌信息
    public final static int Game_Notice_RobDealerResultReq = 51034; //广播：抢庄结果
    public final static int Game_Notice_BetResultReq = 51035; //广播：下注结果
    public final static int Game_Notice_ShowHandCardsReq = 51036; //广播：玩家手中牌型
    public final static int Game_Notice_BetMultipleInfoReq = 51038; //广播：下注倍数

    public final static int Game_Notice_RewardCroupier = 51050; //打赏荷官
    public final static int Game_Notice_GiveGift = 51051; //用户送礼物
    public final static int Game_Notice_ChoosePlayType = 51025; //等待用户选择玩法
    //endregion

    public static String GetFunctionName(int functionId){
        switch (functionId) {
            case GATE_REQ_loginUser:
                return "GATE_REQ_登录用户";
            case GATE_ACK_loginUser:
                return "GATE_ACK_登录用户";

            case Room_REQ_ApplyJoinTable:
                return "Room_REQ_入桌";
            case Room_ACK_ApplyJoinTable:
                return "Room_ACK_入桌";

            //region REQ
            case Game_REQ_ApplyJoinTable:
                return "Game_REQ_入桌";
            case Game_REQ_ApplySitDown:
                return "Game_REQ_坐下";
            case Game_REQ_ApplyLeave:
                return "Game_REQ_离开";
            case Game_REQ_ApplyBet:
                return "Game_REQ_下注";
            case Game_REQ_OtherPlayerInfoReq:
                return "Game_REQ_查看其他玩家信息";
            case Game_REQ_ReconnectReq:
                return "Game_REQ_玩家重连";
            case Game_REQ_ReadyReq:
                return "Game_REQ_玩家已准备";
            case Game_REQ_RewardCroupier:
                return "Game_REQ_打赏";
            case Game_REQ_GiveGifts:
                return "Game_REQ_赠送礼物";
            //endregion

            //region ACK
            case Game_ACK_ApplyJoinTable:
                return "Game_ACK_入桌";
            case Game_ACK_ApplySitDown:
                return "Game_ACK_坐下";
            case Game_ACK_ApplyLeave:
                return "Game_ACK_离开";
            case Game_ACK_ApplyBet:
                return "Game_ACK_下注";
            case Game_ACK_OtherPlayerInfoReq:
                return "Game_ACK_查看玩家信息";
            case Game_ACK_ReconnectReq:
                return "Game_ACK_玩家重连";
            case Game_ACK_ReadyReq:
                return "Game_ACK_玩家已准备";


            //region NOTICE
            case Game_Notice_SiteDown:
                return "Game_Notice_坐下";
            case Game_Notice_StandUp:
                return "Game_Notice_站起";
            case Game_Notice_BuyIn:
                return "Game_Notice_买入";
            case Game_Notice_GameStart:
                return "Game_Notice_游戏开始";
            case Game_Notice_GiveCardRound_Start:
                return "Game_Notice_发牌轮_开始";
            case Game_Notice_BetRound_WaitBet:
                return "Game_Notice_下注轮_等待下注";
            case Game_Notice_BetRound_DoBet:
                return "Game_Notice_下注轮_玩家下注";
            case Game_Notice_leaveReq:
                return "Game_Notice_离桌";
            case Game_Notice_CardTypeReq:
                return "Game_Notice_牌型";
            case Game_Notice_ReadyReq:
                return "Game_Notice_玩家已准备";
            case Game_Notice_FixDealerReq:
                return "Game_Notice_定庄动画";
            case Game_Notice_BetInfoReq:
                return "Game_Notice_下注前信息";
            case Game_Notice_HandCardsListReq:
                return "Game_Notice_玩家手牌信息";
            case Game_Notice_RobDealerResultReq:
                return "Game_Notice_抢庄结果";
            case Game_Notice_BetResultReq:
                return "Game_Notice_下注结果";
            case Game_Notice_ShowHandCardsReq:
                return "Game_Notice_玩家展示手上牌型";
            case Game_Notice_BetMultipleInfoReq:
                return "Game_Notice_下注倍数";
            case Game_Notice_SettleRound_SettleResult:
                return "Game_Notice_结算结果";
            case Game_Notice_SettleRound_History:
                return "Game_Notice_结算历史";
            case Game_Notice_RewardCroupier:
                return "Game_Notice_打赏";
            case Game_Notice_GiveGift:
                return "Game_Notice_赠送礼物";
            case Game_Notice_ChoosePlayType:
                return "Game_Notice_ChoosePlayType";
            //endregion
        }
        return "UNKNOW_FunctionId = "+functionId;
    }
}
