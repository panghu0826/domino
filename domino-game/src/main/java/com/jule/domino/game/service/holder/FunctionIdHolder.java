package com.jule.domino.game.service.holder;

public class FunctionIdHolder {
    public final static int Game_REQ_ApplyJoinTable = 50000;
    public final static int Game_REQ_ApplySiteDown = 50001;
    public final static int Game_REQ_ApplyStandUp = 50002;
    public final static int Game_REQ_ApplyLeave = 50003;
    public final static int Game_REQ_ApplyBuyIn = 50004;
    public final static int Game_REQ_ApplyBet = 50005;

    public final static int Game_REQ_OtherUserInfo = 50014;
    public final static int Game_REQ_Reconnect = 50016;
    //准备
    public final static int Game_REQ_ReadyType = 50018;
    public final static int  Game_REQ_GameRecords = 50063;

    /**
     * 聊天
     */
    public final static int Game_REQ_CHAT = 80001;

    public final static int Game_ACK_ApplyJoinTable = Game_REQ_ApplyJoinTable | 0x08000000;
    public final static int Game_ACK_ApplySiteDown = Game_REQ_ApplySiteDown | 0x08000000;
    public final static int Game_ACK_ApplyStandUp = Game_REQ_ApplyStandUp | 0x08000000;
    public final static int Game_ACK_ApplyLeave = Game_REQ_ApplyLeave | 0x08000000;
    public final static int Game_ACK_ApplyBuyIn = Game_REQ_ApplyBuyIn | 0x08000000;
    public final static int Game_ACK_ApplyBet = Game_REQ_ApplyBet | 0x08000000;

    public final static int Game_ACK_CHAT = Game_REQ_CHAT | 0x08000000;
    public final static int Game_ACK_OtherUserInfo = Game_REQ_OtherUserInfo | 0x08000000;
    public final static int Game_ACK_Reconnect = Game_REQ_Reconnect | 0x08000000;
    public final static int Game_ACK_ReadyType = Game_REQ_ReadyType | 0x08000000;
    public final static int Game_ACK_GameRecords = Game_REQ_GameRecords | 0x08000000;

    public final static int Game_Notice_SiteDown = 51001;
    public final static int Game_Notice_StandUp = 51002;
    public final static int Game_Notice_BuyIn = 51003;
    public final static int Game_Notice_GameStart = 51004;
    public final static int Game_Notice_GiveCardRound_Start = 51005;
    public final static int Game_Notice_BetRound_WaitBet = 51006;
    public final static int Game_Notice_BetRound_DoBet = 51007;
    public final static int Game_Notice_SideShow_ApplySideShow = 51009;
    public final static int Game_Notice_SideShow_SideShowResult = 51012;
    //结算结果
    public final static int Game_Notice_SettleRound_SettleResult = 51013;
    public final static int Game_Notice_SettleRound_History = 51014;
    public final static int Game_Notice_SideShow_CompleteChooseUser = 51015;
    //
    public final static int Game_Notice2Client_leavel = 51023;
    //开牌
    public final static int Game_Notice2Client_ReadyType = 51029;
    //下注前信息
    public final static int Game_Notice2Client_HandCardListType = 51033;
    //广播下注结果
    public final static int Game_Notice2Client_BetResultType = 51035;
    //广播下注倍数
    public final static int Game_Notice2Client_BetMultipleInfoType = 51038;
    //玩家入座
    public final static int Game_Notice2Client_PlayersIn = 51039;

    //打赏荷官广播
    public final static int Game_Notice_RewardCroupier = 51050;
    //送礼物广播
    public final static int Game_Notice_GiveGift = 51051;

    public final static int Game_Notice_TaskProg = 51061;

    /**
     * 聊天广播
     */
    public final static int Game_Notice_CHAT = 80002;

    public static String GetFunctionName(int functionId) {
        switch (functionId) {
            //region REQ
            case Game_REQ_ApplyJoinTable:
                return "Game_REQ_ApplyJoinTable";
            case Game_REQ_ApplySiteDown:
                return "Game_REQ_ApplySiteDown";
            case Game_REQ_ApplyStandUp:
                return "Game_REQ_ApplyStandUp";
            case Game_REQ_ApplyLeave:
                return "Game_REQ_ApplyLeave";
            case Game_REQ_ApplyBuyIn:
                return "Game_REQ_ApplyBuyIn";
            case Game_REQ_ApplyBet:
                return "Game_REQ_ApplyBet";
            case Game_REQ_CHAT:
                return "Game_REQ_CHAT";
            //endregion

            //region ACK
            case Game_ACK_ApplyJoinTable:
                return "Game_ACK_ApplyJoinTable";
            case Game_ACK_ApplySiteDown:
                return "Game_ACK_ApplySiteDown";
            case Game_ACK_ApplyStandUp:
                return "Game_ACK_ApplyStandUp";
            case Game_ACK_ApplyLeave:
                return "Game_ACK_ApplyLeave";
            case Game_ACK_ApplyBuyIn:
                return "Game_ACK_ApplyBuyIn";
            case Game_ACK_ApplyBet:
                return "Game_ACK_ApplyBet";
            case Game_ACK_CHAT:
                return "Game_ACK_CHAT";
            //endregion

            //region NOTICE
            case Game_Notice_SiteDown:
                return "Game_Notice_SiteDown";
            case Game_Notice_StandUp:
                return "Game_Notice_StandUp";
            case Game_Notice_BuyIn:
                return "Game_Notice_BuyIn";
            case Game_Notice_GameStart:
                return "Game_Notice_GameStart";
            case Game_Notice_GiveCardRound_Start:
                return "Game_Notice_GiveCardRound_Start";
            case Game_Notice_BetRound_WaitBet:
                return "Game_Notice_BetRound_WaitBet";
            case Game_Notice_BetRound_DoBet:
                return "Game_Notice_BetRound_DoBet";
            case Game_Notice_SideShow_ApplySideShow:
                return "Game_Notice_SideShow_ApplySideShow";
            case Game_Notice_SideShow_SideShowResult:
                return "Game_Notice_SideShow_SideShowResult";
            case Game_Notice_SettleRound_SettleResult:
                return "Game_Notice_SettleRound_SettleResult";
            case Game_Notice_SettleRound_History:
                return "Game_Notice_SettleRound_History";
            case Game_Notice_SideShow_CompleteChooseUser:
                return "Game_Notice_SideShow_CompleteChooseUser";
            case Game_Notice_RewardCroupier:
                return "Game_Notice_RewardCroupier";
            case Game_Notice_GiveGift:
                return "Game_Notice_GiveGift";
            case Game_Notice_CHAT:
                return "Game_Notice_CHAT";
            //endregion
        }
        return "UNKNOW_FunctionId = " + functionId;
    }
}
