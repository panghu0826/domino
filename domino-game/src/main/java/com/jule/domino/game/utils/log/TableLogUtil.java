package com.jule.domino.game.utils.log;

import com.jule.core.common.log.LoggerUtils;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.service.RegisterService;

public class TableLogUtil {

    /**
     * @param code       协议号
     * @param name       协议名
     * @param userId     玩家ID
     * @param nickName   昵称
     * @param channelId  渠道
     * @param gameId     游戏ID
     * @param roomId     房间ID
     * @param tableId    桌子ID
     * @param seatNum    座位号
     * @param sitDownSuc 坐下是否成功
     * @param money      带入钱
     * @param playersNum 在桌上玩的人数
     */
    public static void sitdown(int code, String name, String userId, String nickName, String channelId, String gameId, String roomId, String tableId, int seatNum, boolean sitDownSuc, double money, int playersNum) {
        String str = "server_ip:{},code:{}," +
                "name:{},user_id:{}," +
                "nick_name:{},channel_id:{}," +
                "game_id:{},room_id:{}," +
                "table_id:{},seat_num:{},sit_down_suc:{},money:{}," +
                "players_num:{}";
        LoggerUtils.tableLog.info(str, "[" + Config.BIND_IP + "," + RegisterService.GAME_SERVER_ID + "]", code,
                name, userId,
                nickName, channelId,
                gameId, roomId,
                tableId, seatNum, sitDownSuc, money,
                playersNum);
    }

    /**
     * @param code
     * @param name
     * @param game_id
     * @param room_id
     * @param table_id
     * @param game_order_id
     * @param players
     * @param dealer_seat_num
     */
    public static void gameStart(int code, String name, String game_id,
                                 String room_id, String table_id, String game_order_id, String players, int dealer_seat_num,String tableInfo) {
        String str = "server_ip:{},code:{},name:{},game_id:{},room_id:{},table_id:{},game_order_id:{},players:{},dealer_seat_num:{},table_info:{}";

        LoggerUtils.tableLog.info(str, "[" + Config.BIND_IP + "," + RegisterService.GAME_SERVER_ID + "]",
                code,
                name,
                game_id,
                room_id,
                table_id,
                game_order_id,
                players,
                dealer_seat_num,tableInfo
        );
    }

    public static void gameAnte(int code, String name, String user_id, String nick_name,
                                String game_id, String room_id, String table_id, String game_order_id, long ant) {
        String str = "server_ip:{}," +
                "code:{}," +
                "name:{}," +
                "user_id:{}," +
                "nick_name:{}," +
                "game_id:{}," +
                "room_id:{}," +
                "table_id:{}," +
                "game_order_id:{}," +
                "ant:{}";
        LoggerUtils.tableLog.info(str, "[" + Config.BIND_IP + "," + RegisterService.GAME_SERVER_ID + "]",
                code,
                name,
                user_id,
                nick_name,
                game_id,
                room_id,
                table_id,
                game_order_id,
                ant
        );
    }

    /**
     * @param code
     * @param name
     * @param user_id
     * @param game_id
     * @param room_id
     * @param table_id
     * @param money
     * @param players
     */
    public static void standUp(int code, String name, String user_id, String game_id, String room_id,
                               String table_id, double money, String players) {
        String str = "server_ip;{},code:{},name:{},user_id:{},game_id:{},room_id:{},table_id:{},money:{},players:{}";
        LoggerUtils.tableLog.info(str, "[" + Config.BIND_IP + "," + RegisterService.GAME_SERVER_ID + "]",
                code,
                name,
                user_id,
                game_id,
                room_id,
                table_id,
                money,
                players
        );
    }

    /**
     * @param code
     * @param name
     * @param user_id
     * @param game_id
     * @param room_id
     * @param table_id
     * @param game_order_id
     * @param card
     */
    public static void seenCard(int code, String name, String user_id, String game_id, String room_id,
                                String table_id, String game_order_id, String card) {
        String str = "server_ip:{}," +
                "code:{}," +
                "name:{}," +
                "user_id:{}," +
                "game_id:{}," +
                "room_id:{}," +
                "table_id:{}," +
                "game_order_id:{}," +
                "card:{}";
        LoggerUtils.tableLog.info(str, "[" + Config.BIND_IP + "," + RegisterService.GAME_SERVER_ID + "]",
                code,
                name,
                user_id,
                game_id,
                room_id,
                table_id,
                game_order_id,
                card
        );
    }

    public static void bet(int code, String name, String user_id, String nick_name, String game_id,
                           String room_id, String table_id, String game_order_id, int bet_round, int curr_bet_round, long bet_num, double money) {
        String str = "server_ip:{}," +
                "code:{}," +
                "name:{}," +
                "user_id:{}," +
                "nick_name:{}," +
                "game_id:{}," +
                "room_id:{}," +
                "table_id:{}," +
                "game_order_id:{}," +
                "bet_round:{}," +
                "curr_bet_round:{}," +
                "bet_num:{}," +
                "money:{}";
        LoggerUtils.tableLog.info(str, "[" + Config.BIND_IP + "," + RegisterService.GAME_SERVER_ID + "]",
                code,
                name,
                user_id,
                nick_name,
                game_id, room_id,
                table_id,
                game_order_id, bet_round, curr_bet_round,
                bet_num,
                money
        );
    }

    public static void fold(int code, String name, String user_id, String nick_name, String game_id,
                            String room_id, String table_id, String game_order_id, int bet_round, int curr_bet_round) {
        String str = "server_ip:{}," +
                "code:{}," +
                "name:{}," +
                "user_id:{}," +
                "nick_name:{}," +
                "game_id:{}," +
                "room_id:{}," +
                "table_id:{}," +
                "game_order_id:{}," +
                "bet_round:{}," +
                "curr_bet_round:{}";
        LoggerUtils.tableLog.info(str, "[" + Config.BIND_IP + "," + RegisterService.GAME_SERVER_ID + "]", code,
                name,
                user_id,
                nick_name,
                game_id,
                room_id,
                table_id,
                game_order_id,
                bet_round,
                curr_bet_round
        );
    }


    public static void show(int code, String name, String user_id, String nick_name, String game_id,
                            String room_id, String table_id, String game_order_id,long amountBet ,String players) {
        String str = "server_ip:{}," +
                "code:{}," +
                "name:{}," +
                "user_id:{}," +
                "nick_name:{}," +
                "game_id:{}," +
                "room_id:{}," +
                "table_id:{}," +
                "game_order_id:{},amountBet:{},players:{}";
        LoggerUtils.tableLog.info(str, "[" + Config.BIND_IP + "," + RegisterService.GAME_SERVER_ID + "]",
                code,
                name,
                user_id,
                nick_name,
                game_id,
                room_id,
                table_id,
                game_order_id,amountBet,players
        );
    }

    public static void sideShow(int code, String name, String user_id, String nick_name, String game_id,
                         String room_id, String table_id, String game_order_id,long amountBet){
        String str = "server_ip:{}," +
                "code:{}," +
                "name:{}," +
                "user_id:{}," +
                "nick_name:{}," +
                "game_id:{}," +
                "room_id:{}," +
                "table_id:{}," +
                "game_order_id:{},amountBet:{}";
        LoggerUtils.tableLog.info(str, "[" + Config.BIND_IP + "," + RegisterService.GAME_SERVER_ID + "]",
                code,
                name,
                user_id,
                nick_name,
                game_id,
                room_id,
                table_id,
                game_order_id,amountBet
        );
    }

    /***
     *
     * @param code
     * @param name
     * @param user_id
     * @param nick_name
     * @param game_id
     * @param room_id
     * @param table_id
     * @param game_order_id
     * @param target_user_id
     */
    public static void chooseUser(int code, String name, String user_id, String nick_name, String game_id,
                                  String room_id, String table_id, String game_order_id, String target_user_id) {
        String str = "server_ip:{}," +
                "code:{}," +
                "name:{}," +
                "user_id:{}," +
                "nick_name:{}," +
                "game_id:{}," +
                "room_id:{}," +
                "table_id:{}," +
                "game_order_id:{}," +
                "target_user_id:{}";
        LoggerUtils.tableLog.info(str, "[" + Config.BIND_IP + "," + RegisterService.GAME_SERVER_ID + "]",
                code,
                name,
                user_id,
                nick_name,
                game_id,
                room_id,
                table_id,
                game_order_id,
                target_user_id
        );
    }

    /***
     *
     * @param code
     * @param name
     * @param user_id
     * @param nick_name
     * @param game_id
     * @param room_id
     * @param table_id
     * @param game_order_id
     * @param target_user_id
     */
    public static void allowSideShowResult(int code, String name, String user_id, String nick_name, String game_id,
                                  String room_id, String table_id, String game_order_id, String target_user_id,String target_nick_name) {
        String str = "server_ip:{}," +
                "code:{}," +
                "name:{}," +
                "user_id:{}," +
                "nick_name:{}," +
                "game_id:{}," +
                "room_id:{}," +
                "table_id:{}," +
                "game_order_id:{}," +
                "target_user_id:{},target_nick_name:{}";
        LoggerUtils.tableLog.info(str, "[" + Config.BIND_IP + "," + RegisterService.GAME_SERVER_ID + "]",
                code,
                name,
                user_id,
                nick_name,
                game_id,
                room_id,
                table_id,
                game_order_id,
                target_user_id,target_nick_name
        );
    }

    public static void sideShowResult(int code, String name, String user_id, String nick_name, String game_id,
                                      String room_id, String table_id, String game_order_id, String target_user_id,String target_nick_name,String cards){
        String str = "server_ip:{}," +
                "code:{}," +
                "name:{}," +
                "user_id:{}," +
                "nick_name:{}," +
                "game_id:{}," +
                "room_id:{}," +
                "table_id:{}," +
                "game_order_id:{}," +
                "target_user_id:{},target_nick_name:{},cards:{}";
        LoggerUtils.tableLog.info(str, "[" + Config.BIND_IP + "," + RegisterService.GAME_SERVER_ID + "]",
                code,
                name,
                user_id,
                nick_name,
                game_id,
                room_id,
                table_id,
                game_order_id,
                target_user_id,target_nick_name,cards
        );
    }

    /**
     * @param code
     * @param name
     * @param game_id
     * @param room_id
     * @param table_id
     * @param players
     */
    public static void settle(int code, String name, String game_id, String room_id, String table_id, String players) {
        String str = "server_ip:{}," +
                "code:{}," +
                "name:{}," +
                "game_id:{}," +
                "room_id:{}," +
                "table_id:{}," +
                "players:{}";
        LoggerUtils.tableLog.info(str, "[" + Config.BIND_IP + "," + RegisterService.GAME_SERVER_ID + "]",
                code,
                name,
                game_id,
                room_id,
                table_id,
                players
        );
    }

    /**
     * @param code
     * @param name
     * @param user_id
     * @param nick_name
     * @param game_id
     * @param room_id
     * @param table_id
     * @param tip
     */
    public static void tip(int code, String name, String user_id, String nick_name, String game_id,
                           String room_id, String table_id, long tip) {
        String str = "server_ip:{}," +
                "code:{}," +
                "name:{}," +
                "user_id:{}," +
                "nick_name:{}," +
                "game_id:{}," +
                "room_id:{}," +
                "table_id:{}," +
                "tip:{}";
        LoggerUtils.tableLog.info(str, "[" + Config.BIND_IP + "," + RegisterService.GAME_SERVER_ID + "]",
                code,
                name,
                user_id,
                nick_name,
                game_id,
                room_id,
                table_id,
                tip
        );
    }

    /**
     * @param code
     * @param name
     * @param user_id
     * @param nick_name
     * @param game_id
     * @param room_id
     * @param table_id
     * @param cost
     */
    public static void chnageCroupier(int code, String name, String user_id, String nick_name, String game_id,
                                      String room_id, String table_id, long cost) {
        String str = "server_ip:{}," +
                "code:{}," +
                "name:{}," +
                "user_id:{}," +
                "nick_name:{}," +
                "game_id:{}," +
                "room_id:{}," +
                "table_id:{}," +
                "cost:{}";
        LoggerUtils.tableLog.info(str, "[" + Config.BIND_IP + "," + RegisterService.GAME_SERVER_ID + "]",
                code,
                name,
                user_id,
                nick_name,
                game_id,
                room_id,
                table_id,
                cost
        );
    }

    /**
     * @param code
     * @param name
     * @param user_id
     * @param nick_name
     * @param game_id
     * @param room_id
     * @param table_id
     * @param money
     * @param item_id
     * @param item_price
     * @param ant
     * @param extra
     * @param players
     */
    public static void gift(int code, String name, String user_id, String nick_name, String game_id,
                            String room_id, String table_id, double money, String item_id, int item_price, long ant, int extra, String players) {
        String str = "server_ip:{}," +
                "code:{}," +
                "name:{}," +
                "user_id:{}," +
                "nick_name:{}," +
                "game_id:{}," +
                "room_id:{}," +
                "table_id:{}," +
                "money:{}," +
                "item_id:{}," +
                "item_price:{}," +
                "ant:{}," +
                "extra:{}," +
                "players:{}";
        LoggerUtils.tableLog.info(str, "[" + Config.BIND_IP + "," + RegisterService.GAME_SERVER_ID + "]",
                code,
                name,
                user_id,
                nick_name,
                game_id,
                room_id,
                table_id,
                money,
                item_id,
                item_price, ant, extra,
                players
        );
    }
    public static void chat(int code, String name, String user_id, String nick_name, String game_id,
                            String room_id, String table_id,String chat){
        String str = "server_ip:{}," +
                "code:{}," +
                "name:{}," +
                "user_id:{}," +
                "nick_name:{}," +
                "game_id:{}," +
                "room_id:{}," +
                "table_id:{},chat:{}";
        LoggerUtils.tableLog.info(str, "[" + Config.BIND_IP + "," + RegisterService.GAME_SERVER_ID + "]",
                code,
                name,
                user_id,
                nick_name,
                game_id,
                room_id,
                table_id,chat);
    }
}
