package com.jule.robot.valve.gate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.*;

public class RobotThreadPoolManager {
    private final static Logger logger = LoggerFactory.getLogger(RobotThreadPoolManager.class);
    private static RobotThreadPoolManager ins;

    private ThreadPoolExecutor pool;

    public RobotThreadPoolManager() {
        //使用有大小限制的Queue，当同时并行线程数大于50，且排队数量task同样大于50时，再加入的新任务将接收到异常信息，task不能被允许执行。
        pool = new ThreadPoolExecutor(10, 50, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));
    }


    public static RobotThreadPoolManager getInstance(){
        if(ins == null)
            ins = new RobotThreadPoolManager();
        return ins;
    }

    public synchronized void addTask(Runnable runnable) {
        if(logger.isDebugEnabled()) {
            logger.debug("核心线程数->{}, 线程池数->{}, 队列任务数->{}, 活动线程数->{}, 任务数->{}, 已完成任务数->{},object->{}",
                    pool.getCorePoolSize(), pool.getPoolSize(), pool.getQueue().size(), pool.getActiveCount(), pool.getTaskCount(), pool.getCompletedTaskCount(), runnable.getClass());
        }
        pool.execute(runnable);
        if(pool.getTaskCount() % 100 == 0){
            logger.debug("执行 垃圾回收。");
            System.gc();
        }
    }

    public long getTaskSum() {
        return pool.getQueue().size();
    }

    public static void shutdown() {
        if(ins!=null)
            ins.shutdown();
    }

    public String getPoolInfo(){
        return String.format(">>>>>>>>>>>>>>>>>>>,basePool, 核心线程数->%d, 线程池数->%d, 队列任务数->%d, 活动线程数->%d, 任务数->%d, 已完成任务数->%d",
                pool.getCorePoolSize(), pool.getPoolSize(), pool.getQueue().size(), pool.getActiveCount(), pool.getTaskCount(), pool.getCompletedTaskCount());
    }
}
