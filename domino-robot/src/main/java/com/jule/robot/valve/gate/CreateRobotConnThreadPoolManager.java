package com.jule.robot.valve.gate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 创建机器人WebSocket连接的线程池
 * 由于创建连接过程中，可能由于服务器端阻塞而导致程序执行较慢，因此独立线程池来处理逻辑执行，避免造成其它任务也被阻塞而不能执行
 */
public class CreateRobotConnThreadPoolManager {
    private final static Logger logger = LoggerFactory.getLogger(CreateRobotConnThreadPoolManager.class);
    private static CreateRobotConnThreadPoolManager ins;

    private ThreadPoolExecutor pool;

    public CreateRobotConnThreadPoolManager() {
        //使用大小无限制的Queue，促使全部task都能排队执行，而不会因为无线程可用，而出现抛出异常的情况
        pool = new ThreadPoolExecutor(10, 50, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    public static CreateRobotConnThreadPoolManager getInstance(){
        if(ins == null)
            ins = new CreateRobotConnThreadPoolManager();
        return ins;
    }

    public synchronized void addTask(Runnable runnable) {
        pool.execute(runnable);
    }

    public long getTaskSum() {
        return pool.getQueue().size();
    }

    public String getPoolInfo(){
        return String.format("???????????????????????,CreateRobot, 核心线程数->%d, 线程池数->%d, 队列任务数->%d, 活动线程数->%d, 任务数->%d, 已完成任务数->%d",
                pool.getCorePoolSize(), pool.getPoolSize(), pool.getQueue().size(), pool.getActiveCount(), pool.getTaskCount(), pool.getCompletedTaskCount());
    }

    public static void shutdown() {
        if(ins!=null)
            ins.shutdown();
    }
}
