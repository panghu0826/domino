package com.jule.domino.game;


import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.configuration.GameConfig;
import com.jule.core.configuration.ItemConfig;
import com.jule.core.jedis.StoredObjManager;
import com.jule.core.service.CronTaskManager;
import com.jule.core.service.ThreadPoolManager;
import com.jule.core.utils.HttpsUtil;
import com.jule.core.utils.xml.LogConfigUtils;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.model.RoomTableRelationModel;
import com.jule.domino.game.api.RestfulServer;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.*;
import com.jule.domino.game.gate.GateServer;
import com.jule.domino.game.gw.GwcNettyServer;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.model.TexasPoker;
import com.jule.domino.game.notice.NoticeServer;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.room.RoomServer;
import com.jule.domino.game.vavle.notice.NoticeConnectPool;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.service.ItemServer;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.network.IOServer;
import com.jule.domino.game.service.*;
import com.jule.domino.game.service.holder.CommonConfigHolder;
import com.jule.domino.game.service.holder.RoomConfigHolder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.eclipse.persistence.jpa.jpql.parser.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class Main {
    private static Thread shutdownHook;
    private static String[] argsTmp;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"all"};
        }
        argsTmp = args;
        try {
            LogConfigUtils.initLogConfig();
            GameConfig.init();
            ItemConfig.init();
            Config.load(argsTmp[0]);
            //ItemServer.OBJ.init(Config.ITEM_SERVER_URL, Config.GAME_ID);

            /**加载礼物配置*/
            ProductionService.getInstance();
//            CardTypeMultipleService.getInstance();
            CommonConfigHolder.getInstance();
            RoomConfigHolder.getInstance();
            TimerService.getInstance();
            TableService.getInstance();
            NoticeConnectPool.getInstance();
            IOServer.connect();

            //处理桌子订阅
           /* TableSub sub = new TableSub();
            sub.start();*/

           //http服务
            RestfulServer.OBJ.start();
            //日志服务注册
//            LogService.OBJ.putServerId(Config.GAME_ID).init(Config.LOG_URL);

            //加载任务配置信息
            TaskService.getInstance().initConfig();
            /**热更新配置*/
//            loadDBConfig();


            String[] str =  Config.GAME_GWCURL.split(":");
            int port = Integer.valueOf(str[1]);
            String host = str[0];

            //GWC 服务启动
//            GwcNettyServer.OBJ.bind(host, port)
//                    .start();

            //Notice 服务启动
            NoticeServer.OBJ.start();
            //Gate 服务启动
            GateServer.OBJ.start();
            //Room 服务启动
            RoomServer.OBJ.start();


            shutdownHook = new Thread(() -> TerminateService.destroy());
            Runtime.getRuntime().addShutdownHook(shutdownHook);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {

        }
    }

//     public static void main(String[] args) throws Exception {
//        Config.load("all");
//        AbstractTable table = TableService.getInstance().createNewTable("1", "1", 5);
//        //修改相关map内容，增加新玩家进入房间状态
//         RoomTableRelationModel roomTable = new RoomTableRelationModel("1", "1", "222",2);
//        PlayerInfo playerA = new PlayerInfo(roomTable, "1001", "1", "1", null, null);
//        PlayerInfo playerB = new PlayerInfo(roomTable, "1002", "2", "2", null, null);
//        table.getInGamePlayers().put(1,playerA);
//        table.getInGamePlayers().put(2,playerB);
//        playerA.setHandCards(new int[]{1, 22, 8, 14,15});
//        playerB.setHandCards(new int[]{18, 4, 21, 25,11});
//        PlayerInfo winner = null; //获胜的用户
//        for (PlayerInfo player : table.getInGamePlayers().values()) {
//            if (winner == null) {
//                winner = player;
//            } else {//此处有多余操作new和set
//                TexasPoker texasA = new TexasPoker(winner.getHandCards());
//                TexasPoker texasB = new TexasPoker(player.getHandCards());
//                winner.setCardType(texasA.getTypeCompareValue());
//                if (texasA.compareTo(texasB) == -1) {
//                    winner = player;
//                }
//                player.setCardType(texasB.getTypeCompareValue());
//            }
//        }
//        winner.setWinner(true);
//        System.out.println("本局赢家");
//    }

    /**
     * 每天某个整点加载一次
     * 修正配置信息
     */
    private static void loadDBConfig() {
        Consumer<Object> loadDBConfigTask = obj -> {
            ThreadPoolManager.getInstance().addTask(() -> {
                //加载common配置文件
                CommonConfigHolder.getInstance().init();
                //加载room配置文件
                RoomConfigHolder.getInstance().init();
                //加载任务配置信息
                TaskService.getInstance().initConfig();
                //配置信息
                Config.load(argsTmp[0]);
            });
        };
        CronTaskManager.CountDownTask task = new CronTaskManager.CountDownTask(0l, loadDBConfigTask);
        CronTaskManager.getInstance().addCountDownJob(task.getName(), "addTableRobot", task, "0 0 " + Config.loadDBConfigHour + " * * ?");

    }

    /**
     * 每2分钟进行一次设置
     * 游戏服活跃状态0:活跃1:error
     */
    private static void countDownTask() {
        Consumer<Object> loadDBConfigTask = obj -> {
            ThreadPoolManager.getInstance().addTask(() -> {
                //定时任务
                if (StoredObjManager.setnx(RedisConst.GAME_STATUS_ACTIVE_SWITCH.getProfix() + Config.BIND_IP, "1") == 1) {
                    StoredObjManager.set(RedisConst.GAME_STATUS_ACTIVE.getProfix() + Config.BIND_IP, "1");
                }
            });
        };
        CronTaskManager.CountDownTask task = new CronTaskManager.CountDownTask(0, loadDBConfigTask);
        CronTaskManager.getInstance().addCountDownJob(task.getName(), "addCountDownTask", task, "* 0/2 * * * ?");

    }

}
