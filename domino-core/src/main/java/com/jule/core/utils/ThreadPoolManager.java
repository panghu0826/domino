package com.jule.core.utils;

import com.jule.core.common.log.LoggerUtils;
import com.jule.core.configuration.ThreadConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author xujian
 */
@Slf4j
public final class ThreadPoolManager {


    private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE - System.nanoTime()) / 2;

    private final ScheduledThreadPoolExecutor scheduledPool;
    private final ThreadPoolExecutor instantPool;
    private final ThreadPoolExecutor longRunningPool;

    private ThreadPoolManager() {
        final int instantPoolMinSize = Runtime.getRuntime().availableProcessors() * 2;
        final int instantPoolMaxSize = Math.max(instantPoolMinSize, ThreadConfig.THREAD_POOL_MAX_SIZE);
        scheduledPool = new ScheduledThreadPoolExecutor(/*Math.max(1, ThreadConfig.EXTRA_THREAD_PER_CORE) * */Runtime.getRuntime().availableProcessors() * 2);
        scheduledPool.setRejectedExecutionHandler((r, executor) -> r.run());
        scheduledPool.prestartAllCoreThreads();

        instantPool = new ThreadPoolExecutor(instantPoolMaxSize, instantPoolMaxSize, 0, TimeUnit.SECONDS, new LinkedTransferQueue<>());
        instantPool.setRejectedExecutionHandler((r, executor) -> {

        });

        int instantPoolThreadNum = instantPool.prestartAllCoreThreads();
        LoggerUtils.performance.info("ThreadPoolManager,instantPoolMinSize:{},instantPoolMaxSize:{}, instantPoolThreadNum;{}",
                instantPoolMinSize,instantPoolMaxSize,instantPoolThreadNum);

        longRunningPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(ThreadConfig.DB_THREAD_POOL_MAX_SIZE);
        longRunningPool.setRejectedExecutionHandler((r, executor) -> r.run());
        longRunningPool.prestartAllCoreThreads();

        Thread maintainThread = new Thread(() -> purge(), "ThreadPool Purge Task");
        maintainThread.setDaemon(true);
        scheduleAtFixedRate(maintainThread, 150000, 150000);

        LoggerUtils.performance.info("ThreadPoolManager: Initialized with " + scheduledPool.getPoolSize() + " scheduler, " + instantPool.getPoolSize()
                + " instant, " + longRunningPool.getPoolSize() + " long running thread(s).");
    }

    private long validate(long delay) {
        return Math.max(0, Math.min(MAX_DELAY, delay));
    }

    private static final class ThreadPoolRunnableWrapper extends ExecuteWrapper {
        private ThreadPoolRunnableWrapper(Runnable runnable) {
            super(runnable, MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING);
        }
    }

    public final ScheduledFuture<?> schedule(Runnable r, long delay) {
        r = new ThreadPoolRunnableWrapper(r);
        delay = validate(delay);
        return scheduledPool.schedule(r, delay, TimeUnit.MILLISECONDS);
    }

    public final ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long delay, long period) {
        //r = new ThreadPoolRunnableWrapper(r);
        delay = validate(delay);
        period = validate(period);
        return scheduledPool.scheduleAtFixedRate(r, delay, period, TimeUnit.MILLISECONDS);
    }

    /**
     * @param r
     */
    public final void execute(Runnable r) {
        instantPool.execute(r);
        LoggerUtils.performance.info("execute() taskNum:{}，threadNum:{}",
                instantPool.getQueue().size(),instantPool.getActiveCount());
    }

    public final void executeDbTask(Runnable r) {
        //r = new ThreadPoolRunnableWrapper(r);
        longRunningPool.execute(r);
    }

    public final Future<?> submit(Runnable r) {
        r = new ThreadPoolRunnableWrapper(r);

        return instantPool.submit(r);
    }

    public final Future<?> submitLongRunning(Runnable r) {
        //r = new ThreadPoolRunnableWrapper(r);

        return longRunningPool.submit(r);
    }

    /**
     * Executes a loginServer packet task
     *
     * @param pkt runnable packet for Login Server
     */
    public void executeLsPacket(Runnable pkt) {
        execute(pkt);
    }

    public void purge() {
        scheduledPool.purge();
        instantPool.purge();
        longRunningPool.purge();
    }

    /**
     * Shutdown all thread pools.
     */
    public void shutdown() {
        final long begin = System.currentTimeMillis();

        log.info("ThreadPoolManager: Shutting down.");
        log.info("... executing " + getTaskCount(scheduledPool) + " scheduled tasks.");
        log.info("... executing " + getTaskCount(instantPool) + " instant tasks.");
        log.info("... executing " + getTaskCount(longRunningPool) + " long running tasks.");

        scheduledPool.shutdown();
        instantPool.shutdown();
        longRunningPool.shutdown();

        boolean success = false;
        try {
            success |= awaitTermination(5000);

            scheduledPool.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            scheduledPool.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

            success |= awaitTermination(10000);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        log.info("\t... success: " + success + " in " + (System.currentTimeMillis() - begin) + " msec.");
        log.info("\t... " + getTaskCount(scheduledPool) + " scheduled tasks left.");
        log.info("\t... " + getTaskCount(instantPool) + " instant tasks left.");
        log.info("\t... " + getTaskCount(longRunningPool) + " long running tasks left.");
    }

    private int getTaskCount(ThreadPoolExecutor tp) {
        return tp.getQueue().size() + tp.getActiveCount();
    }

    public List<String> getStats() {
        List<String> list = new ArrayList<String>();

        list.add("");
        list.add("Scheduled pool:");
        list.add("=================================================");
        list.add("\tgetActiveCount: ...... " + scheduledPool.getActiveCount());
        list.add("\tgetCorePoolSize: ..... " + scheduledPool.getCorePoolSize());
        list.add("\tgetPoolSize: ......... " + scheduledPool.getPoolSize());
        list.add("\tgetLargestPoolSize: .. " + scheduledPool.getLargestPoolSize());
        list.add("\tgetMaximumPoolSize: .. " + scheduledPool.getMaximumPoolSize());
        list.add("\tgetCompletedTaskCount: " + scheduledPool.getCompletedTaskCount());
        list.add("\tgetQueuedTaskCount: .. " + scheduledPool.getQueue().size());
        list.add("\tgetTaskCount: ........ " + scheduledPool.getTaskCount());
        list.add("");
        list.add("Instant pool:");
        list.add("=================================================");
        list.add("\tgetActiveCount: ...... " + instantPool.getActiveCount());
        list.add("\tgetCorePoolSize: ..... " + instantPool.getCorePoolSize());
        list.add("\tgetPoolSize: ......... " + instantPool.getPoolSize());
        list.add("\tgetLargestPoolSize: .. " + instantPool.getLargestPoolSize());
        list.add("\tgetMaximumPoolSize: .. " + instantPool.getMaximumPoolSize());
        list.add("\tgetCompletedTaskCount: " + instantPool.getCompletedTaskCount());
        list.add("\tgetQueuedTaskCount: .. " + instantPool.getQueue().size());
        list.add("\tgetTaskCount: ........ " + instantPool.getTaskCount());
        list.add("");
        list.add("Long running pool:");
        list.add("=================================================");
        list.add("\tgetActiveCount: ...... " + longRunningPool.getActiveCount());
        list.add("\tgetCorePoolSize: ..... " + longRunningPool.getCorePoolSize());
        list.add("\tgetPoolSize: ......... " + longRunningPool.getPoolSize());
        list.add("\tgetLargestPoolSize: .. " + longRunningPool.getLargestPoolSize());
        list.add("\tgetMaximumPoolSize: .. " + longRunningPool.getMaximumPoolSize());
        list.add("\tgetCompletedTaskCount: " + longRunningPool.getCompletedTaskCount());
        list.add("\tgetQueuedTaskCount: .. " + longRunningPool.getQueue().size());
        list.add("\tgetTaskCount: ........ " + longRunningPool.getTaskCount());

        return list;
    }

    private boolean awaitTermination(long timeoutInMillisec) throws InterruptedException {
        final long begin = System.currentTimeMillis();

        while (System.currentTimeMillis() - begin < timeoutInMillisec) {
            if (!scheduledPool.awaitTermination(10, TimeUnit.MILLISECONDS) && scheduledPool.getActiveCount() > 0)
                continue;

            if (!instantPool.awaitTermination(10, TimeUnit.MILLISECONDS) && instantPool.getActiveCount() > 0)
                continue;

            if (!longRunningPool.awaitTermination(10, TimeUnit.MILLISECONDS) && longRunningPool.getActiveCount() > 0)
                continue;

            return true;
        }

        return false;
    }

    private static final class SingletonHolder {
        private static final ThreadPoolManager INSTANCE = new ThreadPoolManager();
    }

    public static ThreadPoolManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static final long MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING = 5 * 1000l;
}
