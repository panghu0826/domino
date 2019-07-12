package com.jule.domino.game.play;

import JoloProtobuf.GameSvr.JoloGame;
import com.google.common.primitives.Ints;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.dao.bean.RoomConfigModel;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.game.service.TimerService;
import com.jule.domino.base.enums.AlarmEnum;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.GameRoomTableSeatRelationModel;
import com.jule.domino.game.utils.CardComparator;
import com.jule.domino.game.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

import java.util.*;

@Slf4j
public class TableUtil {

    /**
     * 牌局配置信息（底注、最大盲注轮次、最小下注额、最大下注额、底池上限以及牌局内是否允许sideShow等其它配置信息）
     *
     * @return
     */
    public static String tableInfoToString(AbstractTable table) {
        RoomConfigModel roomConfig = table.getRoomConfig();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{ant:" + roomConfig.getAnte() + ",");
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public static String toStringNormal(AbstractTable table) {
        return "Table{" +
                "gameId=" + table.getPlayType() +
                ", roomId=" + table.getRoomId() +
                ", tableId='" + table.getTableId() + '\'' +
                ", tableStateEnum=" + table.getTableStateEnum() +
                ", allPlayersCnt=" + table.getAllPlayers().size() +
                ", inGamePlayersCnt=" + table.getInGamePlayersBySeatNum().size() +
                '}';
    }

    public static String toStringAllPlayers(AbstractTable table) {
        StringBuilder sb = new StringBuilder();

        for (PlayerInfo player : table.getAllPlayers().values()) {
            sb.append("userId=" + player.getPlayerId()
                    + ", seatNum=" + player.getSeatNum()
                    + ", state=" + player.getState()
                    + ", playScore=" + player.getPlayScoreStore()
                    + ", isOffline = " + player.isOffLine()
                    + System.getProperty("line.separator"));
        }

        return sb.toString();
    }

    public static String toStringInGamePlayers(AbstractTable table) {
        StringBuilder sb = new StringBuilder();

        for (PlayerInfo player : table.getInGamePlayersBySeatNum().values()) {
            sb.append("userId=" + player.getPlayerId()
                    + ", seatNum=" + player.getSeatNum()
                    + ", state=" + player.getState()
                    + ", playScore=" + player.getPlayScoreStore()
                    + ", isDealer=" + player.getIsDealer()
                    + ", isOffLine = " + player.isOffLine()
                    + System.getProperty("line.separator"));
        }
        return sb.toString();
    }

    public static String inGamePlayersBySeatNumToString(AbstractTable table) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        table.getInGamePlayersBySeatNum().forEach((k, v) -> {
            stringBuilder.append("{seat:" + k + ",");
            stringBuilder.append("userId:" + v.getPlayerId() + ",");
            stringBuilder.append("nickName:" + v.getNickName() + ",");
            stringBuilder.append("money:" + v.getPlayScoreStore());
            stringBuilder.append("isOffLine:" + v.isOffLine());
            stringBuilder.append("},");
        });
        if (stringBuilder.indexOf(",") > 0) {
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "");
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    /**
     * 获得上一个行动的玩家
     *
     * @param nextBetSeatNum
     * @return
     */
    public static PlayerInfo getPreBetPlayer(AbstractTable table, int nextBetSeatNum) {
        PlayerInfo preBetPlayer = null;

        //计算下一个下注玩家的座位号
        int preBetSeatNum = nextBetSeatNum;
        while (true) {
            preBetSeatNum -= 1;
            if (preBetSeatNum < 1) {
                preBetSeatNum = 5;
            }
            //再次找到当前位置，说明已无合适上一个行动者，返回null
            if (nextBetSeatNum == preBetSeatNum) {
                return null;
            }
            if (table.getInGamePlayers().containsKey(preBetSeatNum)) {
                preBetPlayer = table.getInGamePlayers().get(preBetSeatNum);
                break;
            }
        }
        return preBetPlayer;
    }

    public static void tablePlayersMatchSeat(AbstractTable table, Map<String, Long> tmpTableUsers) {
        Set<String> list = StoredObjManager.hkeys(RedisConst.TABLE_USERS.getProfix() + table.getPlayType() + table.getRoomId() + table.getTableId());
        if (list != null) {
            list.forEach(str -> {
                String userId = str.substring(RedisConst.TABLE_USERS.getField().length());
                GameRoomTableSeatRelationModel gameRoomTableSeatRelationModel = StoredObjManager.getStoredObjInMap(
                        GameRoomTableSeatRelationModel.class,
                        RedisConst.USER_TABLE_SEAT.getProfix(),
                        RedisConst.USER_TABLE_SEAT.getField() + userId
                );
                if (gameRoomTableSeatRelationModel == null) {
                    if (!tmpTableUsers.containsKey(userId)) {
                        tmpTableUsers.put(userId, System.currentTimeMillis());
                    }//超过1分钟还存在说明有问题
                    if (System.currentTimeMillis() - tmpTableUsers.get(userId) > DateUtils.MILLIS_PER_MINUTE) {
                        tmpTableUsers.remove(userId);
                        StoredObjManager.hsetnx(RedisConst.ALARM_CHECK.getProfix() + AlarmEnum.TABLE_PLAYERS_MATCH_SEAT.getType(),
                                RedisConst.ALARM_CHECK.getField() + AlarmEnum.TABLE_PLAYERS_MATCH_SEAT.getType(),
                                "IP:" + Config.BIND_IP + ",GameId:" + table.getPlayType() + ",roomId:" + table.getRoomId() + ",tableId:" + table.getTableId()
                                        + ",userId:" + userId);
                    }
                }
            });
        }
        if (tmpTableUsers.size() > 0) {
            log.debug("tablePlayersMatchSeat(),userIds:{}", tmpTableUsers.keySet());
            tmpTableUsers.forEach((k, v) -> {
                if (System.currentTimeMillis() - tmpTableUsers.get(k) > DateUtils.MILLIS_PER_MINUTE) {
                    tmpTableUsers.remove(k);
                }
            });
        }
    }

    public static void checkTablePlayer(AbstractTable table) {
        if (System.currentTimeMillis() - table.getOldTimemillis() > 30 * DateUtils.MILLIS_PER_SECOND) {
            table.getInGamePlayersBySeatNum().forEach((k, v) -> {
                if (!table.getAllPlayers().containsKey(v.getPlayerId())) {
                    StoredObjManager.hsetnx(RedisConst.ALARM_CHECK.getProfix() + AlarmEnum.TABLE_PLAYER_MATCH,
                            RedisConst.ALARM_CHECK.getField() + AlarmEnum.TABLE_PLAYER_MATCH,
                            "GameId:" + table.getPlayType() + ",RoomId:" + table.getRoomId() + ",TableId:" + table.getTableId() +
                                    ",UserId:" + v.getPlayerId());
                }
            });
            table.setOldTimemillis(System.currentTimeMillis());
        }
    }

    /**
     * 获取桌子上的所有玩家状态
     */
    public static List<JoloGame.JoloGame_TablePlay_PlayerInfo> getPlayers(AbstractTable table) {
        List<JoloGame.JoloGame_TablePlay_PlayerInfo> list = new ArrayList<>();
        for (PlayerInfo player : table.getInGamePlayersBySeatNum().values()) {
            if (player != null) {
                JoloGame.JoloGame_TablePlay_PlayerInfo.Builder playerInfo = JoloGame.JoloGame_TablePlay_PlayerInfo.newBuilder()
                        .setUserId(player.getPlayerId())
                        .setNickName(player.getNickName())
                        .setIcon(player.getIcon())
                        .setPlayScoreStore(player.getPlayScoreStore())
                        .setSeatNum(player.getSeatNum())
                        .setAlreadyBetScore(player.getBetMultiple())
                        .setIsDealer(player.getIsDealer())
                        .setState(player.getState().getValue())
                        .setIsBlind(player.getIsBlind())
                        .setNotInGame(player.getState().getValue() > 1 ? 0 : 1);
                if (player.getState() == PlayerStateEnum.beting) {
                    playerInfo.setIsCurrAction(1);
                    playerInfo.setCurrActionSurplusTime(TimerService.getInstance().getLeftCountDown(table.getRoomTableRelation()));
                } else {
                    playerInfo.setIsCurrAction(0);
                }
                int[] handCards = player.getHandCards();
                if (null != handCards && handCards.length > 0) {
                    for (int card : handCards) {
                        playerInfo.addHandCards(card);
                    }
                    playerInfo.setIsDealer(CardComparator.OBJ.isSpecialCard(handCards));
                }
                list.add(playerInfo.build());
            }
        }
        return list;
    }

    public static List<JoloGame.JoloGame_TablePlay_PlayerInfo> getPlayersInTable(AbstractTable table, String currPlayerId) {
        List<JoloGame.JoloGame_TablePlay_PlayerInfo> list = new ArrayList<>();
        for (PlayerInfo player : table.getInGamePlayers().values()) {
            if (player != null) {
                JoloGame.JoloGame_TablePlay_PlayerInfo.Builder tablePlay = JoloGame.JoloGame_TablePlay_PlayerInfo.newBuilder()
                        .setUserId(player.getPlayerId())
                        .setNickName(player.getNickName())
                        .setPlayScoreStore(player.getPlayScoreStore())
                        .setSeatNum(player.getSeatNum())
                        .setIsCurrAction(player.getWinLoseScore4Hand() == table.getBetMaxScore() ? 0 : player.getIsCurrActive())
                        .setCurrActionSurplusTime(table.getBetCd())
                        .setIsDealer(0)
                        .setState(player.getState().getValue())
                        .setIsBlind(0);
                if(table.getGameType() == 1){
                    tablePlay.addAllHandCards(Ints.asList(player.getHandCards()));
                    if (table.getSeeHandCardsPlayerId() == null || !currPlayerId.equals(table.getSeeHandCardsPlayerId())) {
                        //广播里自己的手牌齐全，其他人的前两张张为0
                        if (!player.getPlayerId().equals(currPlayerId)) {
                            tablePlay.setHandCards(0, 0);
                            tablePlay.setHandCards(1, 0);
                        }
                    }
                }else if(table.getGameType() == 2){
                    if (player.getPlayerId().equals(currPlayerId)) {
                        tablePlay.addAllHandCards(Ints.asList(player.getHandCards()));
                    }else {
                        tablePlay.addAllHandCards(Ints.asList(new int[player.getHandCards().length]));//用0占位
                    }
                }
                list.add(tablePlay.build());
            }
        }
        return list;
    }

    public static List<PlayerInfo> getSameRobDealerPlayers(AbstractTable table) {
        List<PlayerInfo> list = new ArrayList<>(table.getInGamePlayers().size());
        Iterator<PlayerInfo> iter = table.getInGamePlayers().values().iterator();
        while (iter.hasNext()) {
            PlayerInfo player = iter.next();
            if (list.size() < 1 || player.getMultiple() > list.get(0).getMultiple()) {
                list.clear();
                list.add(player);
            } else if (list.size() >= 1 && player.getMultiple() == list.get(0).getMultiple()) {
                list.add(player);
            }
        }
        return list;
    }

    /**
     * 计算下注倍数
     *
     * @param table
     */
    public static void calculateBetMultiple(AbstractTable table) {
        long time = System.currentTimeMillis();
        Iterator<PlayerInfo> iter = table.getInGamePlayers().values().iterator();

        while (iter.hasNext()) {
            PlayerInfo playerInfo = iter.next();
            playerInfo.setBetMultipleAry(NumUtils.ConvertInt2ByteArr(table.getRoomConfig().getDoubleList()));

        }
        long sec = System.currentTimeMillis() - time;
        log.error("calculateBetMultiple()cost time:" + sec);
    }

    /**
     * 抢庄倍数：X  闲家下注倍数：Y 庄家牌型倍数：M   闲家牌型倍数：N  底注：A  人数：F 最终货币量：Z
     * 1 抢庄显示倍数公式：X=Z/（F*A*N*Y）
     * X 现阶段只有1 2 3 个选项可选择，取值大于或是等于3 ，也只显示1 2 3
     * 取值方式 为4舍5入，最终得到的值为多少相应的倍数就可以选择，反之不可选择
     * 注： 下注最低倍数现在定义为3
     *
     * @param table
     */
    public static void calculateRobMultiple(AbstractTable table) {
        long time = System.currentTimeMillis();
        Iterator<PlayerInfo> iter = table.getInGamePlayers().values().iterator();
        int playerSize = table.getInGamePlayers().size();
        long ante = table.getRoomConfig().getAnte();
        int maxMultiple = 0;
        double d = 1.00;

        while (iter.hasNext()) {
            PlayerInfo playerInfo = iter.next();

            List<Byte> multipleAry = new ArrayList<>();

            double tmp = d * playerSize * ante * maxMultiple * 3;
            double tmp1 = playerInfo.getPlayScoreStore() / tmp;
            int tmpMultiple = (int) (Math.round(tmp1));

            multipleAry.add((byte) 1);
            if (tmpMultiple >= 2) {
                multipleAry.add((byte) 2);
            }
            if (tmpMultiple >= 3) {
                multipleAry.add((byte) 3);
            }

            playerInfo.setRobMultipleAry(multipleAry);

        }
        long sec = System.currentTimeMillis() - time;
        log.error("calculateBetMultiple()cost time:" + sec);
    }
}
