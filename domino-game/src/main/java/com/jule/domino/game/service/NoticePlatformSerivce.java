package com.jule.domino.game.service;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.enums.RoleType;
import com.jule.domino.base.platform.HallAPIService;
import com.jule.domino.base.platform.bean.*;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.gameUtil.GameOrderIdGenerator;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 平台通知服务
 * @since 2018/11/27 11:15
 */
@Slf4j
public class NoticePlatformSerivce {
    //单例
    public static final NoticePlatformSerivce OBJ = new NoticePlatformSerivce();
    //下注
    public static final String BET = "bet";
    //返奖
    public static final String PROFIT = "profit";
    //使用
    public static final String USE = "use";
    //结算
    public static final String SETTLEMENT = "settlement";
    //其他
    public static final String OTHER = "other";

    private static final SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //汇率换算
    public static final int EXCHANGE_MAX = 10000;
    public static final double EXCHANGE_MIN = 0.0001d;


    /**
     * 打赏、结算、换荷官
     * @param table
     * @param player
     * @param behavior
     * @param gold
     * @param isAdd
     * @return
     */
    public double updateMoney(AbstractTable table, PlayerInfo player, String behavior, double gold, boolean isAdd){
        return 0;
    }


    public void records(AbstractTable table,double servicefee , String winner, double winScore){
        settledAll(table,servicefee,winner,winScore);
    }

    private String makCardValue(AbstractTable table, String winner){
        StringBuffer buffer = new StringBuffer();
        try {
            int winnerseat = 1;
            for (int index = 1; index<=6; index ++){
                PlayerInfo player = table.getInGamePlayersBySeatNum().get(index);
                if (player == null){
                    //座位为空
                    buffer.append("0000");
                    continue;
                }

                //获胜玩家座位
                if (player.getPlayerId().equals(winner)){
                    winnerseat = player.getSeatNum();
                }

                buffer.append(makeCard(player.getHandCards()));
            }

            //加上赢家
            buffer.append(winnerseat);
        }catch (Exception ex){
            log.error("转换牌型失败ex={}",ex);
        }
        return buffer.toString();
    }

    private String makeCard (int[] handcard){
        StringBuffer buffer = new StringBuffer();
        if (handcard == null){
            buffer.append("0000");
            return buffer.toString();
        }

        for (int card: handcard){
            if (card < 10){
                buffer.append("0");
            }
            buffer.append(card);
        }
        return buffer.toString();
    }

    private Map<String, Double> getWinners(List<JoloGame.JoloGame_TablePlay_PlayerSettleInfo> winList){
        Map<String, Double> map = new HashMap<>();
        winList.forEach(e->{
            if (e.getWinLose() == 1){
                map.put(e.getUserId(),e.getWinLoseScore());
            }
        });
        return map;
    }

    /**
     * 根据给定规则，获取房间级别
     * @param roomId
     * @return
     */
    private String getRoomLevel(String roomId){
        if ("10".equals(roomId)){
            return "1";
        }else if ("20".equals(roomId)){
            return "2";
        }else if ("30".equals(roomId)){
            return "3";
        }else if ("40".equals(roomId)){
            return "4";
        }else {
            return "0";
        }
    }

    /**
     * 终局结算全部玩家
     *
     * @param table
     * @param servicefee
     * @param winner
     * @param winScore   净胜
     */
    public void settledAll(AbstractTable table, double servicefee, String winner, double winScore) {
        if (Config.GATESERVER_ISLOCAL) {
            return;
        }
        String count = String.valueOf(table.getInGamePlayersBySeatNum().size());
        String curTime = sdf.format(new Date());
        String cardvalue = makCardValue(table, winner);

        List<ModifyAndRecord> list = new ArrayList<>();
        ModifyAndRecord recordBean = new ModifyAndRecord();

        for (PlayerInfo player : table.getInGamePlayersBySeatNum().values()) {
            if (player.getRoleType() == RoleType.ROBOT) {
                continue;
            }

            String openId = player.getUser().getAndroid_id();
            String bet = NumUtils.double2String(player.getTotalAlreadyBetScore4Hand() * EXCHANGE_MAX);

            double extra = 0;
            //账号修改对象
            ModifyReqBean bean = new ModifyReqBean();
            bean.setUser_id(openId);
            bean.setBehavior(SETTLEMENT);
            if (player.getPlayerId().equals(winner)) {
                bean.setGold(NumUtils.double2String(winScore * EXCHANGE_MAX));
            } else {
                bean.setGold(NumUtils.double2String((extra - player.getTotalAlreadyBetScore4Hand()) * EXCHANGE_MAX));
            }
            bean.setGame_id(String.valueOf(Config.GAME_ID));
            bean.setRoom_id(getRoomLevel(table.getRoomId()) + "_" + Integer.toHexString(Integer.valueOf(table.getRoomId())));
            bean.setSeat_id(String.valueOf(player.getSeatNum()));
            bean.setRound_id(String.valueOf(table.getCurrGameOrderId()));
            //格式：0_yyyyMMddHHmmssSSS_{game_id}_{user_id}_内部编号，长度48位以内
            String orderId = "0_" + df.format(new Date()) + "_" + bean.getGame_id() + "_" + openId + "_" + GameOrderIdGenerator.generate();
            bean.setOrder_id(orderId);

            //游戏记录对象
            GameRecords records = new GameRecords();
            records.setUser_id(openId);
            records.setGame_id(String.valueOf(Config.GAME_ID));
            records.setRoom_id(getRoomLevel(table.getRoomId()) + "_" + Integer.toHexString(Integer.valueOf(table.getRoomId())));
            records.setRoom_type(getRoomLevel(table.getRoomId()));
            records.setTable_id(table.getRoomId());
            records.setSeat_id(String.valueOf(player.getSeatNum()));
            records.setUser_count(count);
            records.setRound_id(String.valueOf(table.getCurrGameOrderId()));
            records.setCard_value(cardvalue);
            records.setInit_balance(NumUtils.double2String(player.getTotalTakeInScore() * EXCHANGE_MAX));
            records.setBalance(NumUtils.double2String(player.getPlayScoreStore() * EXCHANGE_MAX));
            records.setAll_bet(bet);
            records.setAvail_bet(bet);

            if (player.getPlayerId().equals(winner)) {
                records.setProfit(NumUtils.double2String((player.getTotalAlreadyBetScore4Hand() + winScore + extra) * EXCHANGE_MAX));
                records.setRevenue(NumUtils.double2String(servicefee * EXCHANGE_MAX));
            } else {
                records.setProfit(NumUtils.double2String(extra * EXCHANGE_MAX));
                records.setRevenue("0");
            }

            records.setStart_time(curTime);
            records.setEnd_time(curTime);
            records.setChannel_id(player.getUser().getChannel_id());
            records.setSub_channel_id(StringUtils.isEmpty(player.getUser().getSub_channel_id()) ? "" : player.getUser().getSub_channel_id());

            recordBean.setAccount(bean);
            recordBean.setGame_record(records);
            list.add(recordBean);
        }

        if (list.size() <= 0) {
            return;
        }

        //提交到大厅
        Map<String, UserModifyBean> map = HallAPIService.OBJ.modifyUserAccountAndUpdateGameRecord(list);
        if (map == null || map.size() == 0) {
            log.error("同步到大厅失败,map = null");
            return;
        }

        for (PlayerInfo player : table.getInGamePlayersBySeatNum().values()) {
            if (player.getRoleType() == RoleType.ROBOT) {
                continue;
            }

            UserModifyBean userModifyBean = map.get(player.getUser().getAndroid_id());
            if (userModifyBean == null) {
                log.error("同步到大厅失败,userModifyBean = null");
                return;
            }
            if (userModifyBean.getCode() != 0) {
                log.error("同步到大厅失败,userModifyBean.getCode = {}", userModifyBean.getCode());
                return;
            }
            double goldTmp = userModifyBean.getBalance().getGoldDouble();
            double curMoney = NumUtils.double2Decimal(goldTmp * EXCHANGE_MIN);

            //覆盖当前玩家货币
            player.setPlayScoreStore(curMoney);
            User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + player.getPlayerId(), User.class);
            if (user != null) {
                user.setMoney(curMoney);
                StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + player.getPlayerId(), user);
            }
        }
    }

}
