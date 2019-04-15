package com.jule.core.service;

import com.jule.core.utils.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Slf4j
public class CronTaskManager {

    /**
     * 倒计时任务（该类计时是服务器停服后消失）
     */
    public static class CountDownTask {

        static final String TASK_KEY_NAME = "COUNTDOWNTASK";

        /**
         * 任务id 从10000开始计数
         **/
        private static AtomicLong IDSource = new AtomicLong(10000);

        /**
         * 任务id
         **/
        private long id;
        /**
         * 开始时间
         **/
        private long startTime;

        private long endTime;
        /**
         * 任务延迟
         **/
        private long delay;

        private JobDetail jobDetail;

        private Trigger trigger;

        private CronTaskManager mgr;

        private Object _package;

        private Consumer<Object> _remoteCallBack;
        public CountDownTask(long delay, Object event, Consumer<Object> callBack) {

            id = IDSource.getAndIncrement();

            this.startTime = TimeUtil.getSysCurTimeMillis();

            this.endTime = startTime + delay;

            this._package = event;

            this._remoteCallBack = callBack;
        }

        public CountDownTask(long endTime, Consumer<Object> callBack) {

            id = IDSource.getAndIncrement();

            this.startTime = TimeUtil.getSysCurTimeMillis();

            this.endTime = endTime;

            this._package = null;

            this._remoteCallBack = callBack;
        }

        public void end() {
            if (_remoteCallBack != null)
                _remoteCallBack.accept(_package);
        }

        public CountDownTask reset() {

            this.startTime = System.currentTimeMillis();

            this.endTime = startTime + delay;

            return this;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public long getLastTime() {
            return endTime - System.currentTimeMillis();
        }


        public long getDelay() {
            return delay;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return id + "";
        }

        public void extendTime(long time) {
            this.endTime += time;
        }

        public JobDetail getJobDetail() {
            return jobDetail;
        }

        public Trigger getTrigger() {
            return trigger;
        }

        public void cancel() {
            mgr.removeJob(this);
        }

        public void setDetail(CronTaskManager mgr, JobDetail jobDetail, Trigger trigger) {
            this.jobDetail = jobDetail;
            this.trigger = trigger;
            this.mgr = mgr;
        }

        @Override
        public String toString() {
            return "CountDownTask [id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", delay=" + delay + ", jobDetail=" + jobDetail + ", trigger=" + trigger + ", mgr=" + mgr
                    + ", _package=" + _package + ", _remoteCallBack=" + _remoteCallBack + "]";
        }

    }

    public static class CountDownJob implements Job {

        public CountDownJob() {

        }

        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                CountDownTask task = (CountDownTask) context.getJobDetail().getJobDataMap().get(CountDownTask.TASK_KEY_NAME);
                task.end();
            } catch (Exception e) {
                log.error(e.getMessage(),e);
            }

        }
    }

    private static final String TriggerKeyProfix = "T_";

    private static final String DEFAULT_GROUP_NAME = "default_group";

    private static SchedulerFactory gSchedulerFactory;

    private static CronTaskManager cronTaskManager;

    private ConcurrentHashMap<Long, CountDownTask> taskPool;

    public CronTaskManager() {
        init();
        startJobs();
    }

    public static CronTaskManager getInstance() {
        if (cronTaskManager == null)
            cronTaskManager = new CronTaskManager();
        return cronTaskManager;
    }

    public void init() {
        try {
            gSchedulerFactory = new StdSchedulerFactory("./config/quartz.properties");

            taskPool = new ConcurrentHashMap<>();

        } catch (SchedulerException e) {
        }
    }

    /**
     * 启动所有定时任务
     */
    public void startJobs() {
        if (gSchedulerFactory != null) {
            try {
                Scheduler sched = gSchedulerFactory.getScheduler();
                sched.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 关闭所有定时任务
     */
    public void shutdownJobs() {
        if (gSchedulerFactory != null) {
            try {
                Scheduler sched = gSchedulerFactory.getScheduler();
                if (!sched.isShutdown()) {
                    sched.shutdown();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /***
     * 添加一个周期任务
     *
     * 格式: [秒] [分] [小时] [日] [月] [周] [年]
     *
     * 序号 说明 是否必填 允许填写的值 允许的通配符 1 秒 是 0-59 , - * / 2 分 是 0-59 , - * / 3 小时 是
     * 0-23 ,* * - * / 4 日 是 1-31 , - * ? / L W 5 月 是 1-12 , - * / 6 周 是 1-7 , -
     * * ? / L # 7 年 否 empty 或 1970-2099 , - * /
     *
     * 常用示例:
     *
     * 0 0 12 * * ? 每天12点触发 0 15 10 ? * * 每天10点15分触发 0 15 10 * * ? 每天10点15分触发 0
     * 15 * 10 * * ? * 每天10点15分触发 0 15 10 * * ? 2005 2005年每天10点15分触发 0 * 14 * *
     * ? 每天下午的 * 2点到2点59分每分触发 0 0/5 14 * * ? 每天下午的 2点到2点59分(整点开始，每隔5分触发) 0 0/5
     * 14,18 * * ?每天下午的 18点到18点59分(整点开始，每隔5分触发) 0 0-5 14 * * ? * 每天下午的
     * 2点到2点05分每分触发 0 10,44 14 ? 3 WED 3月分每周三下午的 2点10分和2点44分触发 0 15 10 ? * *
     * MON-FRI 从周一到周五每天上午的10点15分触发 0 15 10 15 * ? 每月15号上午10点15分触发 0 15 10 L * ?
     * * 每月最后一天的10点15分触发 0 15 10 ? * 6L 每月最后一周的星期五的10点15分触发 0 15 10 ? * 6L
     * 2002-2005 * 从2002年到2005年每月最后一周的星期五的10点15分触发 0 15 10 ? * 6#3
     * 每月的第三周的星期五开始触发 0 0 12 1/5 * ? * 每月的第一个中午开始每隔5天触发一次 0 11 11 11 11 ?
     * 每年的11月11号 11点11分触发(光棍节)
     *
     *
     *
     * @param jobName
     *            任务名
     */
    public boolean addCountDownJob(String jobName, String groupName, CountDownTask task, String reg) {
        try {
            Scheduler scheduler0 = gSchedulerFactory.getScheduler();
            JobDetail jd = scheduler0.getJobDetail(new JobKey(jobName, groupName));

            if (jd != null) {
                /** 停止触发器 */
                scheduler0.pauseTrigger(new TriggerKey(TriggerKeyProfix + jobName, TriggerKeyProfix + groupName));
                /** 移除触发器 **/
                scheduler0.unscheduleJob(new TriggerKey(jd.getKey().getName(), jd.getKey().getGroup()));
                /** 删除任务 **/
                scheduler0.deleteJob(new JobKey(jd.getKey().getName(), jd.getKey().getGroup()));
            }
        } catch (SchedulerException e1) {
            log.error(e1.getMessage(),e1);
            return false;
        }

        try {
            Scheduler scheduler = gSchedulerFactory.getScheduler();

            JobDetail jobDetail = JobBuilder.newJob(CountDownJob.class)
                    .withIdentity(jobName, groupName).build();

            jobDetail.getJobDataMap().put(CountDownTask.TASK_KEY_NAME, task);

            CronTrigger trigger = TriggerBuilder.newTrigger().
                    withIdentity(TriggerKeyProfix + jobName, TriggerKeyProfix + groupName)
                    .withSchedule(CronScheduleBuilder.cronSchedule(reg))
                    .startAt(new Date(System.currentTimeMillis()))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);

            task.setDetail(this, jobDetail, trigger);

        } catch (SchedulerException e) {
            log.error(e.getMessage(),e);
            return false;
        }

        taskPool.put(task.id, task);

        return true;
    }

    public int getPoolTotal() {
        return taskPool.size();
    }

    /***
     * 添加一个定时任务
     *
     * @param jobName
     *            任务名
     */
    public boolean addCountDownJob(String jobName, String groupName, CountDownTask task) {
        String _gName = groupName;
        if (groupName == null) {
            _gName = DEFAULT_GROUP_NAME;
        }

        try {

            Scheduler scheduler0 = gSchedulerFactory.getScheduler();
            JobDetail jd = scheduler0.getJobDetail(new JobKey(jobName, _gName));
            //如果已经添加过这样的定时任务，那么将该任务取消，重新添加一次
            if (jd != null) {
                TriggerKey tk = new TriggerKey(TriggerKeyProfix + jobName, TriggerKeyProfix + _gName);
                /** 停止触发器 */
                scheduler0.pauseTrigger(tk);
                /** 移除触发器 **/
                scheduler0.unscheduleJob(new TriggerKey(jd.getKey().getName(), jd.getKey().getGroup()));
                /** 删除任务 **/
                scheduler0.deleteJob(new JobKey(jd.getKey().getName(), jd.getKey().getGroup()));
            }
        } catch (SchedulerException e1) {
            log.error(e1.getMessage(),e1);
            return false;
        }

        try {
            Scheduler scheduler = gSchedulerFactory.getScheduler();

            JobDetail jobDetail = JobBuilder.newJob(CountDownJob.class).withIdentity(jobName, _gName).build();

            jobDetail.getJobDataMap().put(CountDownTask.TASK_KEY_NAME, task);

            SimpleTrigger trigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                    .withIdentity(TriggerKeyProfix + jobName, TriggerKeyProfix + _gName)
                    .startAt(new Date(task.getEndTime()))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);

            task.setDetail(this, jobDetail, trigger);

        } catch (SchedulerException e) {
            log.error(e.getMessage(),e);
            return false;
        }

        taskPool.put(task.id, task);

        return true;
    }

    /***
     * 移除一个任务(使用默认的任务组名，触发器名，触发器组名)
     *
     */
    void removeJob(CountDownTask task) {
        try {
            Scheduler sched = gSchedulerFactory.getScheduler();
            /** 停止触发器 */
            sched.pauseTrigger(new TriggerKey(task.getTrigger().getKey().getName(), task.getTrigger().getKey().getGroup()));
            /** 移除触发器 **/
            sched.unscheduleJob(new TriggerKey(task.getTrigger().getKey().getName(), task.getTrigger().getKey().getGroup()));
            /** 删除任务 **/
            sched.deleteJob(new JobKey(task.getJobDetail().getKey().getName(), task.getJobDetail().getKey().getGroup()));

        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

    public CountDownTask removeJob(long taskId) {
        CountDownTask task = taskPool.remove(taskId);
        if (task != null) {
            task.cancel();
            return task;
        } else {
        }
        return null;
    }
}
