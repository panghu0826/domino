package com.jule.domino.game.model;

import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.enums.RoleType;
import com.jule.domino.base.model.RoomTableRelationModel;
import com.jule.domino.game.service.LogService;
import com.jule.domino.game.service.MoneyService;
import com.jule.domino.game.utils.NumUtils;
import com.jule.domino.log.service.LogReasons;
import com.jule.core.jedis.StoredObjManager;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 玩家信息
 * 描述：存储玩家在玩牌过程中的信息，如果一个玩家同时存在于两个桌子，那么将有两个相同playerId 的PlayerInfo，用tableId 进行区分存在于哪个桌子。
 */
@ToString
@Getter
@Setter
public class PlayerInfo {

    private final static Logger logger = LoggerFactory.getLogger(PlayerInfo.class);

    private User user;
    private String playerId;//玩家id 和userI都一样
    private String roomId; //房间ID
    private String tableId;//用户桌子Id
    private String nickName;//显示的昵称
    private String icon; //玩家头像URL
    private boolean isOffLine = false;//是否已经掉线
    private int seatNum; //玩家座位号
    private double playScoreStore = 0;//玩家牌桌上积分的携带量

    //region 玩家在每一局的玩牌信息，每一局开始时重置掉
    private int isBlind; //是否盲牌1是0不是
    private int isDealer; //是否庄家1是0否
    private int isCurrActive; //是否当前行动玩家
    private long alreadyBetScore4Round; //当前轮次已经下注的积分量 (非累积值，每轮次重新计算)
    private PlayerStateEnum state;//玩家状态 0/旁观 1/入座 2/游戏中 3/已下注 4/已开牌
    /**
     * 牌数组的下标
     */
    private int[] handCards = null;
    //手中的牌
    private List<Byte> cards;
    private List<Byte> betMultipleAry = new ArrayList<>();
    private List<Byte> robMultipleAry = new ArrayList<>();
    /**
     * 玩家是否可以与某个用户比边牌 0/不能 1/可以和所有人比 2/只能和上家比
     */
    private int isCanSideShow;
    private int isCanShow; //玩家是否可以比牌结算
    private long totalAlreadyBetScore4Hand; //当前牌局累积已下注金额，方便后续结算时判断用户输赢
    private long winLoseScore4Hand; //当前牌局输赢积分数量
    private int mixedCard; //玩家手上有几张癞子牌
    private List<Integer> mixedCardList; //记录玩家结算后的混牌id
    //endregion

    //region 此部分用于统计玩家玩牌时长，输赢积分
    private long joinTableTime; //加入牌桌时间
    private long leaveTableTime; //离开牌桌时间
    private long sitDownTime; //入座时间
    /**
     * 站起时间
     */
    private long standUpTime = 0L;
    private int totalPlayMinutes; //合计的玩牌时间（分钟数）
    /**
     * 入桌时带入积分及在桌内购入积分总合
     */
    private double totalTakeInScore = 0;
    private double totalWinLoseScore; //合计的输赢积分数
    private long biggestChipsWon = 0;

    private WinLoseRecord handsWon = new WinLoseRecord();
    private boolean isWinner;//是否是本局赢家
    /**
     * 旁观次数累计(连续超过三局自动离桌)
     */
    private int spectatorCount;
    //玩家sideshow选择玩家超时
    @Deprecated
    private boolean sideshowOvertime;
    private RoleType roleType;
    //计算出的手牌类型
    private int type;
    // 抢庄倍数(注意结算时为0时要记得是乘1)
    private int multiple = 0;
    // 下注倍数
    private int betMultiple = 1;
    //当前局的分数
    private double curScore;


    public PlayerInfo(RoomTableRelationModel relationModel, String playerId, String nickName, String icon, RoleType roleType, User user) {
        this.roomId = relationModel.getRoomId();
        this.tableId = relationModel.getTableId();
        this.playerId = playerId;
        this.nickName = nickName;
        this.icon = icon;
        this.isBlind = 0;
        setRoleType(roleType);
        betMultipleAry.add((byte) 1);
        betMultipleAry.add((byte) 2);
        betMultipleAry.add((byte) 3);
        this.user = user;
    }

    public PlayerInfo(String roomId, String tableId, String playerId, String nickName, String icon) {
        this.roomId = roomId;
        this.tableId = tableId;
        this.playerId = playerId;
        this.nickName = nickName;
        this.icon = icon;
        this.isBlind = 0;
        setRoleType(roleType);
    }

    public void removePlayerInfo() {
        this.isBlind = 0;
        this.isDealer = 0;
        this.isCurrActive = 0;
        this.alreadyBetScore4Round = 0;
        this.state = PlayerStateEnum.spectator;
        this.handCards = null;
        this.isCanSideShow = 0;
        this.isCanShow = 0;
        this.totalAlreadyBetScore4Hand = 0;
        this.winLoseScore4Hand = 0;
        this.mixedCard = 0;
        this.isWinner = false;
        this.spectatorCount = 0;
        this.sideshowOvertime = false;
    }

    public void addCurScore(long score) {
        this.curScore += score;
    }

    /**
     * 重置游戏中信息
     */
    public void resetGameingInfo() {
        //如果玩家不是自动弃牌则重置数据
        type = 0;
        this.betMultiple = 1;
        this.multiple = 0;
        this.curScore = 0;
        this.isBlind = 0;
        this.isDealer = 0;
        this.isCurrActive = 0;
        this.alreadyBetScore4Round = 0;
        this.state = PlayerStateEnum.siteDown;
        this.handCards = null;
        this.isCanSideShow = 0;
        this.isCanShow = 0;
        this.totalAlreadyBetScore4Hand = 0;
        this.winLoseScore4Hand = 0;
        this.mixedCard = 0;
        this.isWinner = false;
        this.spectatorCount = 0;
    }

    public void sitDownAfterInit() {
        setBiggestChipsWon(0);
        setTotalWinLoseScore(0);
        setState(PlayerStateEnum.siteDown); //修改玩家状态值
        handsWon.getLinkedDeque().clear();
    }


    public String toSitDownString() {

        return "Player{" +
                "playerId='" + playerId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", tableId='" + tableId + '\'' +
                ", seatNum=" + seatNum +
                ", playScoreStore=" + playScoreStore +
                ", state=" + state +
                ", handCards=" + Arrays.toString(handCards) +
                ", sitDownTime=" + sitDownTime +
                ", standUpTime=" + standUpTime +
                ", totalPlayMinutes=" + totalPlayMinutes +
                ", totalWinLoseScore=" + totalWinLoseScore +
                '}';
    }

    /**
     * 增加筹码
     *
     * @param score     操作筹码
     * @param logReason 操作原因
     * @return
     */
    public boolean addPlayScoreStore(double score, LogReasons.CommonLogReason logReason) {
        //筹码是负数
        if (score <= 0) {
            return false;
        }

        //玩家不存在
        User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + playerId, User.class);
        if (user == null) {
            return false;
        }

        double _org = this.playScoreStore == 0 ? user.getMoney() : this.playScoreStore;
        //增加积分
        if (this.playScoreStore != 0) {
            this.playScoreStore += score;
            user.setMoney(this.playScoreStore);
        } else {
            user.setMoney(user.getMoney() + score);
        }
        logger.info("12save userInfo->" + user.toString());
        StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + playerId, user);
        //发送日志
        LogService.OBJ.sendMoneyLog(user, _org, user.getMoney(), score, logReason);

        return true;
    }

    /**
     * 扣除筹码
     *
     * @param score     操作筹码
     * @param logReason 操作原因
     * @return
     */
    public boolean minusPlayScoreStore(double score, LogReasons.CommonLogReason logReason) {
        //筹码是负数
        if (score <= 0 || this.playScoreStore < score) {
            return false;
        }

        //玩家不存在
        //User user = DBUtil.selectByPrimaryKey(playerId);
        User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + playerId, User.class);
        if (user == null) {
            return false;
        }

        double _org = this.playScoreStore;
        //扣除积分
        this.playScoreStore -= score;

        user.setMoney(this.playScoreStore);
//        StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + playerId, user);
        //更新数据库信息
        MoneyService.getInstance().updateMoney(user.getId(), this.playScoreStore);
        //发送日志
        LogService.OBJ.sendMoneyLog(user, _org, this.getPlayScoreStore(), score, logReason);
        return true;
    }

    public String playerToString() {
        return "roomId->" + roomId + ", tableId->" + tableId + ", playerId->" + playerId + ", seatNum->" + seatNum + ", playScoreStore->" + playScoreStore;
    }

    public double getPlayScoreStore() {
        return NumUtils.double2Decimal(playScoreStore);
    }
}
