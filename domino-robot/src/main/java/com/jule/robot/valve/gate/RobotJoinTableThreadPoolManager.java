package com.jule.robot.valve.gate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RobotJoinTableThreadPoolManager {
    private final static Logger logger = LoggerFactory.getLogger(RobotJoinTableThreadPoolManager.class);
    private static RobotJoinTableThreadPoolManager ins;

    private ThreadPoolExecutor pool;

    public RobotJoinTableThreadPoolManager() {
        //使用大小无限制的Queue，促使全部task都能排队执行，而不会因为无线程可用，而出现抛出异常的情况
        pool = new ThreadPoolExecutor(10, 50, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    public static RobotJoinTableThreadPoolManager getInstance(){
        if(ins == null)
            ins = new RobotJoinTableThreadPoolManager();
        return ins;
    }

    public synchronized void addTask(Runnable runnable) {
        pool.execute(runnable);
    }

    public long getTaskSum() {
        return pool.getQueue().size();
    }

    public String getPoolInfo(){
        return String.format("++++++++++++++++++++++++,JoinTablePool, 核心线程数->%d, 线程池数->%d, 队列任务数->%d, 活动线程数->%d, 任务数->%d, 已完成任务数->%d",
                pool.getCorePoolSize(), pool.getPoolSize(), pool.getQueue().size(), pool.getActiveCount(), pool.getTaskCount(), pool.getCompletedTaskCount());
    }

    public static void shutdown() {
        if(ins!=null)
            ins.shutdown();
    }
}
