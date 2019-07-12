package com.jule.domino.game.play;

import com.google.protobuf.MessageLite;
import com.jule.core.common.log.LoggerUtils;
import com.jule.core.jedis.StoredObjManager;
import com.jule.core.utils.fifo.FIFORunnableQueue;
import com.jule.domino.base.enums.*;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.model.CardConstent;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.service.holder.*;
import com.jule.domino.game.utils.log.TableLogUtil;
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
import com.jule.domino.game.vavle.notice.NoticeRPCUtil;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.model.GameRoomTableSeatRelationModel;
import com.jule.domino.base.model.RoomTableRelationModel;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.CommonConfigModel;
import com.jule.domino.game.dao.bean.RoomConfigModel;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.logic.LeaveTableLogic;
import com.jule.domino.game.service.*;
import com.jule.domino.log.service.LogReasons;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Setter @Getter
public class AbstractTable implements ITable {
    protected final static Logger log = LoggerFactory.getLogger(AbstractTable.class);

    private final FIFORunnableQueue fifoRunnableQueue = new FIFORunnableQueue<ClientReq>() {
    };
    /**
     * 玩法类型
     */
    private int playType;//此处用的是头里面的数据 定死 不能随便改
    private int gameType;//此处才是正确的游戏id
    protected String tableId;
    private String roomId;
    protected RoomTableRelationModel roomTableRelation;
    private boolean propertyChange = false;
    /**
     * 每次开局前重新加载
     */
    private CommonConfigModel commonConfig = null;
    private RoomConfigModel roomConfig = null;
    //房间所有人 key：userId
    protected final Map<String, PlayerInfo> allPlayers = new ConcurrentHashMap<>();

    private final Map<String, PlayerInfo> demoPlayers = new ConcurrentHashMap<>();
    /*
     * 已坐下的玩家列表<位置，playerInfo>
     * key：seatNum
     */
    private final Map<Integer, PlayerInfo> inGamePlayersBySeatNum = new ConcurrentHashMap<>();
    /*
     * 已坐下的玩家列表<userId,PlayerInfo>
     * key：userId
     */
    private final Map<String, PlayerInfo> inGamePlayersByPlayerId = new ConcurrentHashMap<>();
    /*
     * 游戏中的人(弃牌就移出)
     * key：seatNum
     */
    protected final Map<Integer, PlayerInfo> inGamePlayers = new ConcurrentHashMap<>();
    /*
     * 玩家每局游戏的下注额，结算清空(统计日志)
     * key：playerId
     */
    private final Map<String, Double> alreadyBet = new ConcurrentHashMap<>();

    //当前牌局单号
    protected String currGameOrderId = "";
    //死机玩家的id 用于强制提出
    private int offlineSeatNum;
    private String offlinePlayerId;
    protected TableStateEnum tableStateEnum;//牌桌状态

    //当前行动玩家座位号(需要在换桌、站起时进行处理)
    protected int currActionSeatNum;
    protected int firstGiveCardSeatNum;//第一个发牌玩家(第一轮发牌为0)
    private Set<String> openCardPlayerIds = new HashSet<>(2);
    private String currDoSideShowPlayerId; //当前比牌发起用户ID
    private String currTargetSideShowPlayerId; //当前被比牌用户ID
    private List<String> loseUserIds = new ArrayList<>();
    private ConcurrentLinkedQueue<Integer> nullSeatList = new ConcurrentLinkedQueue<>(); //可用的空座位列表
    private long lastActionTime = 0;//上次本桌操作时间
    protected String firstReadyPlayer;//第一个准备的人 非抢庄模式的庄家
    //港式五张
    protected boolean everyoneCanJoinIn;//桌子是否所有人可加入(玩家是否开启了好友功能)
    protected String createTableUserId;//创建桌子的userId
    protected String controlCardTypePlayerId;//控制牌型的玩家id
    protected String seeHandCardsPlayerId;//可看全部人底牌的玩家id
    protected Set<Integer> equalScore = new HashSet<>();//判断房间所有人下注积分是否相等
    protected int roundTableScore;//当前轮次桌内下注额(根据下注最大的人变化)
    protected long currBaseBetScore;//当前基础下注值
    protected int betMaxScore;//下注积分上限
    protected boolean isWatch;//是否可以观战
    protected int tableAlreadyBetScore; //桌子当前牌局累积已下注金额
    protected int tableTotalAlreadyBetScore; //桌子所有牌局累积已下注金额
    protected Date startTime;//牌局开始时间
    //牛牛
    protected int bankerType;//1明牌抢庄,2自由抢庄,3轮流庄,4固定庄,5牛牛上庄
    protected int wildCard;//1无,2王癞子,3,随机癞子,4王+随机癞子
    protected int doubleRule;//翻倍规则:1牛牛四倍2:牛牛三倍
    protected List<Integer> specialCardType;//特殊牌型:1.对子4,2.顺子5,3.五花5,4.同花6,5.葫芦7,6.炸弹8,7.五小9,8.顺金10
    protected int bankerCd;//抢庄cd
    protected String currDealerPlayerId;  //当前庄家id
    //公用
    protected int clubId;//当前桌子所属的俱乐部id
    protected int playerNum;//人数（房间座位数）
    protected String betMultiple;//桌子下注倍数
    protected int gameNum;//游戏局数
    protected int currGameNum; //桌子当前进行的游戏局数
    protected int readyCd;//准备cd
    protected int betCd;//下注cd（超时则弃牌）
    protected int openCardCd;//亮牌cd

    public AbstractTable() {}

    //此处为港式五张构造类
    public AbstractTable(String gameId, String roomId, String tableId, int playerNum) {
        log.error("此处子类重写了，不应该再使用了");
    }

    public AbstractTable(String gameId, String roomId, String tableId) {
        setPlayType(1);
        setRoomId(roomId);
        setTableId(tableId);
        roomTableRelation = new RoomTableRelationModel(gameId, roomId, tableId, TableStateEnum.IDEL.getValue());

        //初始化空座位
        addNullSeat(1);
        addNullSeat(2);
        addNullSeat(3);
        addNullSeat(4);
        addNullSeat(5);
        addNullSeat(6);

        setCommonConfig(CommonConfigHolder.getInstance().getCommonConfig(Integer.parseInt(gameId)));
        setRoomConfig(RoomConfigHolder.getInstance().getRoomConfig(roomId));
        initTableStateAttribute();

        tableStateEnum = TableStateEnum.IDEL;
        setTableStatus();
    }

    @Override
    public void joinTable(PlayerInfo player) {
        if (!allPlayers.containsKey(player.getPlayerId())) {
            UserTableService.getInstance().onPlayerInTable(getPlayType() + "", player, this);
            //RoomStateService.getInstance().onPlayerJoinTable(this);
            player.setState(PlayerStateEnum.spectator);
            allPlayers.put(player.getPlayerId(), player);
        }
    }

    public PlayerInfo joinRoom(PlayerInfo player) {
        String gameId = String.valueOf(this.getPlayType());
        UserTableService.getInstance().onPlayerInTable(gameId, player, this);
        RoomStateService.getInstance().onPlayerJoinTable(this);
        player.setState(PlayerStateEnum.spectator);
        StoredObjManager.hset(RedisConst.TABLE_USERS.getProfix() + gameId + getRoomId() + getTableId(),
                RedisConst.TABLE_USERS.getField() + player.getPlayerId(), "" + PlayerStateEnum.spectator.getValue());
        //设置玩家所在位置
        StoredObjManager.hset(RedisConst.USER_TABLE_SEAT.getProfix(), RedisConst.USER_TABLE_SEAT.getField() + player.getPlayerId(),
                new GameRoomTableSeatRelationModel(gameId, player.getRoomId(), player.getTableId(), 0, ""));
        demoPlayers.put(player.getPlayerId(), player);
        log.debug("房间人数：===================={} ", demoPlayers.size());
        return allPlayers.put(player.getPlayerId(), player);
    }

    @Override
    public boolean sitDown(int seatNum, String userId) {
        boolean flag = false;
        PlayerInfo player = allPlayers.get(userId);
        if (player != null && !inGamePlayersBySeatNum.containsKey(seatNum)
                && !inGamePlayersByPlayerId.containsKey(userId)) {
            removeNullSeat(seatNum);
            //玩家坐下重置状态
            player.sitDownAfterInit();
            player.setSeatNum(seatNum);
            player.setState(PlayerStateEnum.siteDown);
            inGamePlayersBySeatNum.put(seatNum, player);
            inGamePlayersByPlayerId.put(userId, player);
            allPlayers.forEach((k, v) -> {
                v.setStandUpTime(0L);
                log.debug("重置玩家standUpTime为0, userId->{}, tableId->{}, roomId->{}, gameId->{}, siteDownUserId->{}, playerState->{}",
                        v.getPlayerId(), tableId, roomId, playType, userId, v.getState());
            });
            flag = true;
            StoredObjManager.hset(RedisConst.TABLE_SEAT.getProfix() + getPlayType() + player.getRoomId() + player.getTableId(),
                    RedisConst.TABLE_SEAT.getField() + seatNum, "" + userId);
            //设置座位
            GameRoomTableSeatRelationModel gameRoomTableSeatRelationModel = StoredObjManager.getStoredObjInMap(
                    GameRoomTableSeatRelationModel.class, RedisConst.USER_TABLE_SEAT.getProfix(),
                    RedisConst.USER_TABLE_SEAT.getField() + player.getPlayerId());

            log.debug("gameRoomTableSeatRelationModel != null ---> " + (gameRoomTableSeatRelationModel != null));
            if (gameRoomTableSeatRelationModel != null) {
                gameRoomTableSeatRelationModel.setTableId(tableId);
                gameRoomTableSeatRelationModel.setSeat(seatNum);
                StoredObjManager.hset(RedisConst.USER_TABLE_SEAT.getProfix(), RedisConst.USER_TABLE_SEAT.getField() + player.getPlayerId(),
                        gameRoomTableSeatRelationModel);
            }
        }
        return flag;
    }

    /**
     * 如果执行就执行正确
     * 检查没有此玩家也为站起成功
     *
     * @param seatNum
     * @param userId
     * @param standUpType
     * @return
     */
    @Override
    public boolean standUp(Integer seatNum, String userId, String standUpType) {
        String guid = UUID.randomUUID().toString();
        boolean flag = false;
        //当牌局不是空闲或结算状态时，从游戏用户列表中移除用户，否则先不移除用户，等游戏结算进行结算时再移除
        PlayerInfo player = inGamePlayersBySeatNum.get(seatNum);
        log.debug("standUp() seatNum:{},userId:{},standUpType:{}", seatNum, userId, standUpType);
        if (player == null) {
            log.debug("inGamePlayersBySeatNum:" + TableUtil.inGamePlayersBySeatNumToString(this));
        }
        StringBuilder sb = new StringBuilder();
        log.debug("tableInfo->" + TableUtil.toStringNormal(this));
        for (PlayerInfo playerInfo : allPlayers.values()) {
            sb.append("playerInfo:" + playerInfo.toString() + "," + System.getProperty("line.separator"));
        }
        log.debug(sb.toString());

        if (null != player) {
            //判断当前座位上的人，是否是需要站起的玩家。
            if (!player.getPlayerId().equals(userId)) {
                //TODO：没想好是否还需要清除某个userId对应的缓存数据
                log.error("standUp()。seatNum:{},userId->{}, seat's UserId->{}, tableInfo:{},inGamePlayerBySeatNum->{}," +
                                "allPlayers:{},reason:{}"
                        , seatNum, userId, player.getPlayerId(), TableUtil.toStringNormal(this), TableUtil.inGamePlayersBySeatNumToString(this),
                        TableUtil.toStringAllPlayers(this), standUpType);
                standUpPatch(userId);
                //flag = judageStandup(seatNum, player, userId);
                return flag;
            }
        }


        if (player != null) {
            inGamePlayersBySeatNum.remove(seatNum);
            inGamePlayersByPlayerId.remove(player.getPlayerId());
            inGamePlayers.remove(seatNum);
            addNullSeat(seatNum);
            PlayerInfo p = allPlayers.get(userId);
            p.setState(PlayerStateEnum.spectator);
            flag = true;
            TableLogUtil.standUp(FunctionIdHolder.Game_REQ_ApplyLeave, standUpType, player.getPlayerId(), getPlayType() + "", getRoomId(),
                    getTableId(), player.getPlayScoreStore(), TableUtil.inGamePlayersBySeatNumToString(this));
            log.info("用户站起成功，从游戏用户列表中移除此用户。playerId->" + player.getPlayerId() + ", table->" + TableUtil.toStringNormal(this));

            StoredObjManager.hdel(RedisConst.TABLE_SEAT.getProfix() + getPlayType() + player.getRoomId() + player.getTableId(),
                    RedisConst.TABLE_SEAT.getField() + player.getSeatNum());

            log.info(getPlayType() + "_" + player.getRoomId() + "_" + player.getTableId());

//            log.debug("发给room的站起协议 table->" + TableUtil.toStringNormal(this));
//            RoomOprService.OBJ.standupHandler(String.valueOf(getPlayType()), player.getRoomId(), player.getTableId());

            //设置座位
            StoredObjManager.hdel(RedisConst.USER_TABLE_SEAT.getProfix(), RedisConst.USER_TABLE_SEAT.getField() + userId);
        }

//        if (inGamePlayersBySeatNum.size() == 0) {
//            lastActionTime = 0;
//        }
        if (!flag && player != null) {
            LoggerUtils.tableLog.error("stand up fail，standUpType:{},seatNum:{},userId:{},tableInfo:{},in AllPlayers:{}," +
                            "inGamePlayersBySeatNum 's playerInfo:{},inGamePlayersByPlayerId:{}", standUpType, seatNum, userId, TableUtil.toStringNormal(this),
                    allPlayers.get(userId) == null ? "NULL" : allPlayers.get(userId).playerToString()
                    , inGamePlayersBySeatNum.get(seatNum) == null ? "Null" : player.playerToString()
                    , inGamePlayersByPlayerId.get(userId) == null ? "Null" : inGamePlayersByPlayerId.get(userId).playerToString()
            );
            StoredObjManager.hsetnx(RedisConst.ALARM_CHECK.getProfix() + AlarmEnum.STAND_UP_FAILED.getType(),
                    RedisConst.ALARM_CHECK.getField() + AlarmEnum.STAND_UP_FAILED.getType(),
                    "gameId:" + getPlayType() + ",roomId:" + getRoomId() + ",tableId:" + getTableId() + ",userId:" + userId + "seatId:" + seatNum);
        }
        String res = StoredObjManager.hget(RedisConst.TABLE_USERS.getProfix() + this.getPlayType() + getRoomId() + getTableId(),
                RedisConst.TABLE_USERS.getField() + userId);
        if (StringUtils.isNotEmpty(res)) {
            StoredObjManager.hset(RedisConst.TABLE_USERS.getProfix() + this.getPlayType() + getRoomId() + getTableId(),
                    RedisConst.TABLE_USERS.getField() + userId, "" + PlayerStateEnum.spectator.getValue());
        }
        //flag = judageStandup(seatNum, player, userId);

        if (player != null && player.isOffLine() && flag) {
            LeaveTableLogic.getInstance().logic(player, this);
        }
        return flag;
    }

    /**
     * 根据数据来判定站起是否成功
     *
     * @param seatNum
     * @param player
     * @param userId
     * @return
     */
    private boolean judageStandup(int seatNum, PlayerInfo player, String userId) {
        if (player != null) {
            if (player.getPlayerId().equals(userId)) {
                log.error("judageStandUp()，seatNumPlayer's playerId equals userId ");
                return false;
            }
        }
        if (inGamePlayersByPlayerId.containsKey(userId)) {
            log.error("judageStandUp()，inGamePlayersByPlayerId contains userId ");
            return false;
        }
        PlayerInfo p = inGamePlayers.get(seatNum);
        if (p != null && p.getPlayerId().equals(userId)) {
            log.error("judageStandUp()，inGamePlayers's playerId equals userId ");
            return false;
        }
        return true;
    }

    /**
     * 站起补丁
     *
     * @param userId
     */
    public void standUpPatch(String userId) {
        inGamePlayersBySeatNum.forEach((k, v) -> {
            if (v.getPlayerId().equals(userId)) {
                inGamePlayersBySeatNum.remove(k);
                inGamePlayers.remove(k);
                return;
            }
        });
        inGamePlayersByPlayerId.remove(userId);
    }

    /**
     * 离桌
     *
     * @param userId
     */
    public void returnLobby(String userId) {
        returnLobby(userId, true);
    }

    @Override
    public void returnLobby(String userId, boolean force) {
        log.info("删除用户列表中的用户，userId->" + userId + "，删除前列表人数->" + allPlayers.size());
        PlayerInfo playerInfo = allPlayers.remove(userId);
        if (playerInfo != null && playerInfo.getSeatNum() > 0) {
            PlayerInfo inGamePlayerInfo = inGamePlayersBySeatNum.get(playerInfo.getSeatNum());
            if (null != playerInfo && null != inGamePlayerInfo) {
                if (playerInfo.getPlayerId().equals(inGamePlayerInfo.getPlayerId())) {
                    inGamePlayersBySeatNum.remove(playerInfo.getSeatNum());
                    inGamePlayers.remove(playerInfo.getSeatNum());
                    inGamePlayersByPlayerId.remove(playerInfo.getPlayerId());
                    addNullSeat(playerInfo.getSeatNum());

                    //上桌内的玩家信息删除
                    StoredObjManager.hdel(RedisConst.TABLE_SEAT.getProfix() + getPlayType() + roomId + tableId,
                            RedisConst.TABLE_SEAT.getField() + inGamePlayerInfo.getSeatNum());
                }
            }
        }

        StoredObjManager.hdel(RedisConst.TABLE_USERS.getProfix() + getPlayType() + roomId + tableId,
                RedisConst.TABLE_USERS.getField() + userId);
        //设置座位
        Boolean isDel = StoredObjManager.hdel(RedisConst.USER_TABLE_SEAT.getProfix(), RedisConst.USER_TABLE_SEAT.getField() + userId);
        log.info("gameId:{},roomId:{},tableId:{},playerId:{},del key:{},del value:{},idDel:{}",
                getPlayType(), getRoomId(), getTableId(),
                userId, RedisConst.USER_TABLE_SEAT.getProfix(),
                RedisConst.USER_TABLE_SEAT.getField() + userId, isDel
        );

//        if (force) {//强制广播
//            JoloGame.JoloGame_TablePlay_OtherPlayerInfo.Builder otherPlayerInfo =
//                    JoloGame_tablePlay_OtherPlayerInfoBuilder.getOtherPlayerInfo(playerInfo);
//            User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId, User.class);
//            //广播离桌
//            boardcastMessageSingle(userId, JoloGame.JoloGame_Notice2Client_leaveReq.newBuilder()
//                            .setRoomId(getRoomId())
//                            .setTableId(getTableId())
//                            .setUserId(userId)
//                            .setCurrStoreScore(user == null ? 0 : user.getMoney())
//                            .setOtherPlayerInfo(otherPlayerInfo)
//                            .setWinLoseScore(playerInfo == null ? 0 : playerInfo.getTotalWinLoseScore())
//                            .build(),
//                    FunctionIdHolder.Game_Notice2Client_leavel);
//        }
    }

    @Override
    public PlayerInfo getPlayer(String playerId) {
        return allPlayers.get(playerId);
    }

    @Override
    public int giveCardCounts() {
        return CardConstent.HAND_CARDS;
    }

    //region 空座位管理
    public void addNullSeat(int seatNum) {
        if (inGamePlayersBySeatNum.containsKey(seatNum)) {
            return;
        }
        if (nullSeatList.contains(seatNum)) {
            log.info("addNullSeat error");
            return;
        }
        nullSeatList.add(seatNum);
    }

    private boolean removeNullSeat(int seatNum) {
        if (null != nullSeatList && nullSeatList.size() > 0) {
            return nullSeatList.remove(seatNum);
        }
        return false;
    }

    public synchronized int getNulSeatNum() {
        int seatNum = 0;
        try {
            Random random = new Random(System.currentTimeMillis());
            if (nullSeatList.size() > 0) {
                seatNum = (int) (nullSeatList.toArray())[random.nextInt(nullSeatList.size())];
            }
            if (null != nullSeatList && nullSeatList.size() > 0) {
                nullSeatList.remove(seatNum);
            }
        } catch (Exception ex) {
            log.error("nullSeatList size: " + nullSeatList.size() + "," + ex.getMessage(), ex);
        }
        if (seatNum == 0 && nullSeatList.size() > 0) {//modify lyb 2018-07-24 随机不到若本桌还有空位则给一个
            seatNum = nullSeatList.poll();
        }
        log.info("seatNum:{},nullSeatList:{}", seatNum, nullSeatList.toArray());
        return seatNum;
    }

    @Override
    public void setGameId(String gameId) {
        setPlayType(Integer.parseInt(gameId));
    }

    @Override
    public List<Integer> getChangeCards() {
        return new ArrayList<>();
    }

    @Override
    public PlayerInfo getWinnerByCompareCards(PlayerInfo player1, PlayerInfo player2) {

        return null;
    }

    public double playerDataSettlement(PlayerInfo player) {
        return this.playerDataSettlement(player, true);
    }

    /**
     * 结算玩家数据
     *
     * @param player
     * @param isSendNotice 是否在玩家站起后，发送Notice广播 add by gx 20181009
     *                     原因：牌局结算后，解散牌桌，站起每个玩家。
     *                     为了方面客户端不处理站起Notice，因此控制可不发送站起Notice消息
     * @return
     */
    public double playerDataSettlement(PlayerInfo player, boolean isSendNotice) {
        log.info("getTotalWinLoseScore:" + player.getTotalWinLoseScore() + " getPlayScoreStore:" + player.getPlayScoreStore() + " takeInStore:" + player.getTotalTakeInScore());
        if (player.getPlayScoreStore() > 0) {
            player.setTotalWinLoseScore(player.getPlayScoreStore() - player.getTotalTakeInScore());
        }
        double currentMoney = MoneyService.getInstance().updateMoney(player.getPlayerId(), player.getPlayScoreStore());
        //发送日志
        User user = DBUtil.selectByPrimaryKey(player.getPlayerId());
        if (user != null) {
            LogService.OBJ.sendMoneyLog(user, user.getMoney(), currentMoney, user.getMoney() - currentMoney, LogReasons.CommonLogReason.GAME_SETTLE);
        }

        log.debug("plaery standup, playScore on table=>" + player.getPlayScoreStore() + ", userMoneyStore after standUp->" + currentMoney);

        player.setPlayScoreStore(0); //修改玩家桌内货币携带量为0
        player.setState(PlayerStateEnum.spectator);//设置玩家状态为旁观

        this.getInGamePlayers().remove(player.getSeatNum()); //将玩家从游戏中移除

        //RoomStateService.getInstance().onPlayerStandUp(this, player);

        try {
            //广播玩家站起消息
            log.info("玩家站起，isSendNotice->{}, userId->{}, nickName->{}, seatNum->{}, gameId->{}, roomId->{}, tableId->{}",
                    isSendNotice, player.getPlayerId(), player.getNickName(), player.getSeatNum(), this.getPlayType(), this.getRoomId(), this.getTableId());
            if (isSendNotice) {
                NoticeBroadcastMessages.playerStandUp(this, player.getPlayerId(), player.getSeatNum());
            }

            if (this.getInGamePlayersBySeatNum().size() < 2) {
                if (this.getTableStateEnum() == TableStateEnum.GAME_READY) {
                    //停止牌桌内正在进行的倒计时
                    TimerService.getInstance().delTimerTask(this.getRoomTableRelation());
                    this.setTableStateEnum(TableStateEnum.IDEL);
                    setTableStatus();
                }
            }
        } catch (Exception ex) {
            log.error("SendNotice ERROR：", ex);
        }
        return currentMoney;
    }


    public ArrayList<String> getInGamePlayerIds(String exceptUserId) {
        ArrayList<String> arrayList = new ArrayList<>();

        for (PlayerInfo player : inGamePlayersByPlayerId.values()) {
            if (!player.getPlayerId().equals(exceptUserId)) {
                arrayList.add(player.getPlayerId());
            }
        }
        return arrayList;
    }

    /**
     * 推送消息(消息体相同时)
     */
    public void boardcastMessage(String tableId, MessageLite messageLite, int functionId) {
        List<String> list = new ArrayList<>();
        for (String playerId : allPlayers.keySet()) {
            PlayerInfo player = allPlayers.get(playerId);
            if (player != null) {
//                if (!player.isOffLine()) {
                    list.add(player.getPlayerId());
//                }
            }
        }

        log.debug("message functionId:" + functionId + "push player:" + list + ",getPlayType:" + getPlayType() + ", functionId: " + functionId);
        NoticeRPCUtil.senMuliMsg(getPlayType(), tableId, list, functionId, messageLite);
//        RabbitMqSender.me.producer(functionId, messageLite.toString());
    }

    /**
     * 推送消息,仅限于sideshow比牌
     */
    public void boardcastMessage(String tableId, String playerId1, String playerId2, MessageLite messageLite,
                                 int functionId) {
        List<String> list = new ArrayList<>();
        list.add(playerId1);
        list.add(playerId2);
        NoticeRPCUtil.senMuliMsg(getPlayType(), tableId, list, functionId, messageLite);
    }

    /**
     * 推送消息(消息体相同时)
     */
    public void boardcastMessageSingle(String playerId, MessageLite messageLite, int functionId) {
        NoticeRPCUtil.sendSingMsg(getPlayType(), playerId, functionId, messageLite);
        RabbitMqSender.me.producer(functionId, messageLite.toString());
    }

    @Getter
    @Setter
    private long oldTimemillis = 0;

    private Map<String, Long> tmpTableUsers = new ConcurrentHashMap<>();

    public boolean isAllActionOver(PlayerStateEnum stateEnum) {
        if (inGamePlayersBySeatNum.size() < 1) {
            return false;
        }
        for (PlayerInfo playerInfo : inGamePlayersBySeatNum.values()) {
            if (!playerInfo.isOffLine() && !playerInfo.getState().equals(stateEnum)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否都操作过
     */
    public boolean isActionOver(PlayerStateEnum stateEnum) {
        if (inGamePlayers.values().size() < 1) {
            return false;
        }
        for (PlayerInfo playerInfo : inGamePlayers.values()) {
            if (!playerInfo.getState().equals(stateEnum)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 超时离桌
     */
    public void timeOutLeaveTable() {
        try {
            boolean success = StoredObjManager.hsetnx(
                    RedisConst.HASH_SET_NX.getProfix() + "TABLE",
                    RedisConst.HASH_SET_NX.getField() + playType + getRoomId() + getTableId(),
                    "" + System.currentTimeMillis());
            if (!success) {
                return;
            }
            TableUtil.checkTablePlayer(this);


            StringBuilder sb = new StringBuilder();
            long nowTime = System.currentTimeMillis();
            sb.append("timeOutLeaveTable()， nowTime: " + nowTime + ", gameId->" + playType + ", roomId:" + roomId + ". tableId:" + tableId + ", inGamePlayerCnt:" + getInGamePlayersBySeatNum().size());
            if (getInGamePlayersBySeatNum().size() == 1) {
                for (PlayerInfo player : getInGamePlayersBySeatNum().values()) {
                    if (player.getStandUpTime() == 0L) {
                        //sb.append(", playerId :" + player.getPlayerId());
                        log.debug("发现桌内只剩余一个玩家，给此玩家设置倒计时为当前时间。userId->{}, roomId-{}, tableId->{}, gameId->{}, nowTime->{}",
                                player.getPlayerId(), roomId, tableId, playType, nowTime);
                        player.setStandUpTime(System.currentTimeMillis());
                    }
                }
            } else if (getInGamePlayersBySeatNum().size() > 1) {
                //5分钟本桌没有操作则重新开始
                if (lastActionTime > 0 && nowTime - lastActionTime >= 5 * DateUtils.MILLIS_PER_MINUTE) {
                    log.error("此桌子长时间没有任何操作 tableInfo:{},players:{},inGameBySeatNum userInfo:{}", TableUtil.toStringNormal(this), TableUtil.toStringAllPlayers(this),
                            TableUtil.inGamePlayersBySeatNumToString(this));
                }
//                if (lastActionTime > 0 && nowTime - lastActionTime >= 2 * DateUtils.MILLIS_PER_MINUTE) {
//                    lastActionTime = System.currentTimeMillis();
//                    setTableStateEnum(TableStateEnum.IDEL);
//                    setTableStatus();
//                    TableService.getInstance().playGame(this);
//                    StoredObjManager.set(RedisConst.GAME_STATUS_ACTIVE.getProfix() + Config.BIND_IP, "The game pauses ，" +
//                            "playType:" + getPlayType() + ",roomId:" + roomId + ",tableId:" + tableId);
//                }
            }

            //循环检查全部玩家，如果玩家已经站起，并且旁观时间
            allPlayers.forEach((k, v) -> {
                if (v.getStandUpTime() != 0L && nowTime - v.getStandUpTime() >= DateUtils.MILLIS_PER_MINUTE) {
                    //玩家达到离桌标准，强制离桌（标准：桌子上无任何玩家，并且玩家旁观等待时间超过配置的临界值）
                    try {
                        LeaveTableLogic.getInstance().logic(v, this);
                        v.setStandUpTime(0L);
                        sb.append(",强制玩家离开桌子 ,PlayerId:" + v.getPlayerId() + ", stanupTime->" + v.getStandUpTime() + ";" + System.getProperty("line.separator"));
                    } catch (Exception ex) {
                        log.error("强制玩家离开桌子，异常。playerId->{}, gameId->{}, roomId->{}, tableId->{}, msg->{}",
                                v.getPlayerId(), playType, roomId, tableId, ex.getMessage(), ex);
                    }
                }

                //桌子上无任何玩家时，强制给每个旁观者赋值离开桌子倒计时
                if (getInGamePlayersBySeatNum().size() == 0) {
                    if (v.getStandUpTime() == 0L) {
                        v.setStandUpTime(System.currentTimeMillis());
                    }
                }
            });

            //加报警（玩家在桌上坐着确找不到相应数据）
            if (Config.GAME_ALARM) {
                TableUtil.tablePlayersMatchSeat(this, tmpTableUsers);
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            StoredObjManager.hdel(RedisConst.HASH_SET_NX.getProfix() + "TABLE",
                    RedisConst.HASH_SET_NX.getField() + playType + getRoomId() + getTableId());
        }
    }

    public void initTableStateAttribute() {
        //初始化牌桌配置
        try {
            this.inGamePlayers.clear(); //座位信息清除
        } catch (Exception e) {
        }
    }

    //设置玩家手牌
    public void setPlayerHandCards() {}

    //发牌并设置玩家牌数组
    public void pressCard(boolean isFirstRound,boolean allCards) {}

    //寻找第一个下注玩家
    public void lookForFirstBetPlayer() {}

    public void setTableStatus() {
        RoomTableRelationModel ret = StoredObjManager.getStoredObjInMap(RoomTableRelationModel.class,
                RedisConst.TABLE_INSTANCE.getProfix() + getPlayType() + getRoomId(),
                RedisConst.TABLE_INSTANCE.getField() + getTableId());
        log.debug("从redis查找桌子信息：--key: {}, --field: {}", RedisConst.TABLE_INSTANCE.getProfix() + getPlayType() + getRoomId(), RedisConst.TABLE_INSTANCE.getField() + tableId);
        if (ret == null) {
            log.error("setTableStatus ERROR, ret from redis is null. {}, {}, {}", getPlayType(), getRoomId(), getTableId());
            return;
        }
        ret.setTableStatus(getTableStateEnum().getValue());
        StoredObjManager.hset(RedisConst.TABLE_INSTANCE.getProfix() + getPlayType() + getRoomId(),
                RedisConst.TABLE_INSTANCE.getField() + getTableId(), ret);
    }

    public void clearAllSeats() {
        allPlayers.clear();
        inGamePlayers.clear();
        inGamePlayersBySeatNum.clear();
        inGamePlayersByPlayerId.clear();
        tmpTableUsers.clear();
    }

    public String getPlayTypeStr() {
        return String.valueOf(this.playType);
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
            //再次找到当前位置，说明已无合适下一个行动者，返回null
            if (nextBetSeatNum == preBetSeatNum) {
                log.error("再次找到当前座位，已无合适的下一个行动者 nextBetSeatNum == preBetSeatNum=={}", preBetSeatNum);
                return null;
            }
            if (this.getInGamePlayers().containsKey(nextBetSeatNum)) {
                nextBetPlayer = this.getInGamePlayers().get(nextBetSeatNum);
                if (nextBetPlayer.getState() != PlayerStateEnum.fold) {
                    //找到了合适的下一个行动者，结束循环
                    currActionSeatNum = nextBetSeatNum;//记录当前行动者座位号
                    nextBetPlayer.setState(PlayerStateEnum.beting);
                    nextBetPlayer.setIsCurrActive(1);
                    //记录玩家开始行动的时间
                    nextBetPlayer.setStartActionTime(System.currentTimeMillis());
                    break;
                }
            }
        }
        return nextBetPlayer;
    }

    @Override
    public String toString() {
        return "此方法应该被子类重写";
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableId, roomId, roomTableRelation);
    }
}
