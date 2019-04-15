package com.jule.domino.robot;

import com.jule.core.service.CronTaskManager;
import com.jule.core.service.ThreadPoolManager;
import com.jule.core.utils.xml.LogConfigUtils;
import com.jule.db.dao.CommDao;
import com.jule.db.entities.User;
import com.jule.db.proxy.EntityProxy;
import com.jule.robot.Robot;
import com.jule.robot.config.Config;
import com.jule.robot.network.IOServer;
import com.jule.robot.service.LogService;
import com.jule.robot.service.PlayersService;
import com.jule.robot.service.RobotPoolService;
import com.jule.robot.service.holder.RobotUserHolder;
import com.jule.robot.service.holder.RoomConfigHolder;
import com.jule.robot.service.thread.*;
import com.jule.robot.valve.gate.RobotThreadPoolManager;
import lombok.extern.slf4j.Slf4j;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public class Main {

    public static void main(String[] args){
        try {
            CronTaskManager.getInstance();
            LogConfigUtils.initLogConfig();
            Config.load();

            log.info("GATE_SVR_URI -> " + Config.GATE_SVR_URI);

            CommDao.OBJ.initDbConfig();

            RoomConfigHolder.getInstance();

            RobotPoolService.OBJ.init().reset();

            if (Config.TEST_TYPE_IS_LOCAL == 0) {
                //退还异常机器人的货币
                RobotThreadPoolManager.getInstance().addTask(new ReturnRobotMoneyThread());
            }

            //初始化机器人列表
            RobotUserHolder.InitRobotUser();

            long t = new Date().getTime();

            //更新任务：机器人配置
            Runnable runnable = new RefreshRoboConfigThread();
            Consumer<Object> callRefreshConfig = obj -> {
                RobotThreadPoolManager.getInstance().addTask(runnable);
            };
            CronTaskManager.CountDownTask taskRefreshConfig = new CronTaskManager.CountDownTask(t, callRefreshConfig);
            CronTaskManager.getInstance().addCountDownJob(taskRefreshConfig.getName(), "refreshRoboConfigThread", taskRefreshConfig, "0 0/3 * * * ?");
            RobotThreadPoolManager.getInstance().addTask(runnable);

            //定时任务：检查机器人是否超时连接
            Runnable runnableTimeout = new CheckTimeoutRobotClient();
            Consumer<Object> callTimeout = obj -> {
                RobotThreadPoolManager.getInstance().addTask(runnableTimeout);
            };
            CronTaskManager.CountDownTask taskTimeout = new CronTaskManager.CountDownTask(t, callTimeout);
            CronTaskManager.getInstance().addCountDownJob(taskTimeout.getName(), "CheckTimeoutRobotClient", taskTimeout, "0 0/1 * * * ?");
            RobotThreadPoolManager.getInstance().addTask(runnableTimeout);

            //启动线程，监控桌子列表状态
            Consumer<Object> callCheck = obj -> {
                if (PlayersService.getInstance().getOnlinePlayersNum() < Config.ROBOT_TOTAL_ROBOT_NUM) {
                    RobotThreadPoolManager.getInstance().addTask(new CheckTableIsNeedRobot(71001001));
                }
            };
            RobotThreadPoolManager.getInstance().addTask(new CheckTableIsNeedRobot(71001001));
            CronTaskManager.CountDownTask taskCheck = new CronTaskManager.CountDownTask(t, callCheck);
            CronTaskManager.getInstance().addCountDownJob(taskCheck.getName(), "CheckTableIsNeedRobot", taskCheck, "0 0/5 * * * ?");


            //如果不是压测环境
            if (Config.TEST_TYPE_IS_STRESS == 0) {
                //启动线程，监控桌子列表状态
                //延迟50秒进行
                Consumer<Object> call = obj -> {
                    if (PlayersService.getInstance().getOnlinePlayersNum() < Config.ROBOT_TOTAL_ROBOT_NUM) {
                        RobotThreadPoolManager.getInstance().addTask(new CheckTableIsNeedRobot(71001001));
                    }
                };
                CronTaskManager.CountDownTask task = new CronTaskManager.CountDownTask(t, call);
                CronTaskManager.getInstance().addCountDownJob(task.getName(), "checkTableNeedRobot", task, "0/" + Config.CHECK_THREAD_INTERVAL_SEC + " * * * * ?");
            }

            //如果是压测环境
            if (Config.TEST_TYPE_IS_STRESS == 1) {
                Stress();
            }

            if(Config.TEST_TYPE_IS_LOCAL == 1){ //是本地环境
            }

            IOServer.connect();

            //日志服务注册
            LogService.OBJ.init(Config.LOG_URL);
        } catch (Exception e) {
            log.error("!!!!!!!!!!!!!!! 启动失败->{}", e.getMessage(), e);
        }

    }

    private static void Stress() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(()->{makeRobot();},0,1, TimeUnit.SECONDS);
    }

    private static void makeRobot(){
        try {
            int robotCnt = RobotUserHolder.getRobotMap().size();
            log.debug("当前备用机器人 robotCnt->{}", robotCnt);

            int curPlayers = PlayersService.getInstance().getOnlinePlayersNum();
            log.info("当前压测人数players={}", curPlayers);

            if (curPlayers >= Config.ROBOT_TOTAL_ROBOT_NUM){
                log.info("当前压测人数已达配置极限");
                return;
            }

            robotJoin();
        }catch (Exception ex){
            log.error(ex.getMessage(), ex);
        }
    }


    private static void robotJoin(){
        try {
            //取机器人
            String userId = RobotUserHolder.getUser("压力测试");
            User user = EntityProxy.OBJ.get(userId, User.class);

            //给钱去玩
            if (user != null && user.getMoney() < 50000d) {
                user.setMoney(50000d);
                EntityProxy.OBJ.update(user, User.class);
            }

            //开始加入游戏
            new Robot(71001001, userId, null, null);
        }catch (Exception ex){
            log.error(ex.getMessage(), ex);
        }

    }
}
