package com.jule.robot.service;

import com.jule.core.jedis.StoredObjManager;
import com.jule.core.service.CronTaskManager;
import com.jule.core.service.ThreadPoolManager;
import com.jule.db.entities.RobotCapitalPoolConfigModel;
import com.jule.domino.base.enums.RedisConst;
import com.jule.robot.dao.DBUtil;
import com.jule.robot.service.holder.RobotMoneyPoolHolder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 机器人杀量池和资金池操作类
 *
 * @author
 * @since 2018/11/7 11:07
 */
@Slf4j
public class RobotPoolService {

    //单例
    public static final RobotPoolService OBJ = new RobotPoolService();

    private static final String GameId = "71001001";

    public RobotPoolService init(){
        loadDBConfig();
        //10分钟定时加载
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(()->loadRobotPool(),0,10, TimeUnit.MINUTES);
        return this;
    }

    public RobotPoolService reset(){
        resetData();
        return this;
    }

    /**
     * 每天零点重置一次<br/>
     * 加载数据库配置信息
     */
    private void loadDBConfig(){
        Consumer<Object> loadDBConfigTask = obj -> ThreadPoolManager.getInstance().addTask(()->loadConfig());
        CronTaskManager.CountDownTask task = new CronTaskManager.CountDownTask(0l, loadDBConfigTask);
        CronTaskManager.getInstance().addCountDownJob(task.getName(), "addTableRobot", task, "0 0 0 * * ?");
    }

    /**
     * 每天零点重置一次<br/>
     * 修正redis信息
     */
    private void resetData() {
        Consumer<Object> loadDBConfigTask = obj -> ThreadPoolManager.getInstance().addTask(()->resetPool());
        CronTaskManager.CountDownTask task = new CronTaskManager.CountDownTask(0l, loadDBConfigTask);
        CronTaskManager.getInstance().addCountDownJob(task.getName(), "addTableRobot", task, "0 0 0 * * ?");
    }


    private void loadConfig(){
        //每日机器人资金池金额重置
        RobotMoneyPoolHolder.resetRobotMoneyPool(GameId);
    }

    private void resetPool(){
        //重置总杀量池
        StoredObjManager.set(RedisConst.GAME_KILL_AMOUNT_POOL.getProfix(),String.valueOf(0));

        //重置机器人池X
        StoredObjManager.set(RedisConst.ROBOT_POOL_CURRENT_MONEY.getProfix(),String.valueOf(0));
    }

    private void loadRobotPool(){
        log.debug("定时加载机器人池初始值");
        //启动时加载机器人资金池
        RobotCapitalPoolConfigModel model = DBUtil.selectByCapitalPool(GameId);
        if (model == null){
            return;
        }
        StoredObjManager.set(RedisConst.ROBOT_INIT_KILL_POOL.getProfix(),String.valueOf(model.getInitKillPool()));
        StoredObjManager.set(RedisConst.ROBOT_INIT_ROBOT_POOL.getProfix(),String.valueOf(model.getInitRobotPool()));
    }

}
