package com.jule.domino.room;

import com.jule.core.service.CronTaskManager;
import com.jule.core.service.ThreadPoolManager;
import com.jule.core.utils.xml.LogConfigUtils;
import com.jule.domino.room.config.Config;
import com.jule.domino.room.network.IOServer;
import com.jule.domino.room.service.TableService;
import com.jule.domino.room.service.TableSub;
import com.jule.domino.room.service.holder.CommonConfigHolder;
import com.jule.domino.room.service.holder.RoomConfigHolder;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class Main {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"all"};
        }
        try {
            LogConfigUtils.initLogConfig();
            Config.load(args[0]);
            RoomConfigHolder.getInstance();
            TableService.getInstance();
            //处理桌子订阅
            TableSub sub = new TableSub();
            sub.start();

            IOServer.connect();

            loadDBConfig();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 每5秒加载一次
     * 修正配置信息
     */
    private static void loadDBConfig() {
        Consumer<Object> loadDBConfigTask = obj -> {
            ThreadPoolManager.getInstance().addTask(new Runnable() {
                @Override
                public void run() {
                    //加载common配置文件
                    CommonConfigHolder.getInstance().init();
                    //加载room配置文件
                    RoomConfigHolder.getInstance().init();

                }
            });
        };
        CronTaskManager.CountDownTask task = new CronTaskManager.CountDownTask(0l, loadDBConfigTask);
        CronTaskManager.getInstance().addCountDownJob(task.getName(), "addTableRobot", task, "0 0 " + Config.loadDBConfigHour + " * * ?");

    }

}
