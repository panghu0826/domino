package com.jule.domino.dispacher.service;

import com.jule.core.jedis.StoredObjManager;
import com.jule.core.utils.TimeUtil;
import com.jule.domino.base.dao.bean.*;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.UserTaskListModel;
import com.jule.domino.base.model.UserTaskStatListModel;
import com.jule.domino.dispacher.dao.TaskDBUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 登录成功
 * 缓存玩家对应任务和统计
 */
@Slf4j
public class TaskService {
    private static class SingletonHolder {
        protected static final TaskService instance = new TaskService();
    }

    public static final TaskService getInstance() {
        return TaskService.SingletonHolder.instance;
    }

    /**
     * 是否有符合条件的任务
     *
     * @return
     */
    public boolean hasMatchCondition(Map<Integer, TaskUserHistModel> taskMap) {
        AtomicBoolean has = new AtomicBoolean(false);
        taskMap.forEach((k, v) -> {
            //判断时间段、目标达成否
            TaskLineConfigModel taskLineConfigModel = StoredObjManager.hget(RedisConst.TASK_CONFIG_LINE.getProfix(),
                    RedisConst.TASK_CONFIG_LINE.getField() + v.getLineId(), TaskLineConfigModel.class);
            long beginTime = TimeUtil.getDateToMillis(taskLineConfigModel.getBeginTime());
            long endTime = TimeUtil.getDateToMillis(taskLineConfigModel.getEndTime());
            long currentTime = System.currentTimeMillis();
            if (currentTime > beginTime && currentTime < endTime) {

                if (v.getTaskStatus() != GameConst.TaskFinished) {
                    has.set(true);
                    return;
                }

            }
        });
        return has.get();
    }

    /**
     * 玩家登录时加载自身可重置任务
     * 及统计信息
     *
     * @param userId
     */
    public void initTaskUserConfig(String userId) {
        fillTaskUserHist(userId);
        fillTaskUserStat(userId);
    }

    public void fillTaskUserHist(String userId) {
        List<TaskUserHistModel> userTasks = TaskDBUtil.getTaskUserHistFromDb(userId);
        log.info("userId:{},DBTask size:{}", userId, userTasks.size());
        Map<String, TaskLineConfigModel> taskLineList = StoredObjManager.hgetAll(RedisConst.TASK_CONFIG_LINE.getProfix(),
                TaskLineConfigModel.class);
        if (taskLineList == null) {
            return;//throw new Error("任务的缓存数据没有加载完成");
        }
        Iterator<TaskLineConfigModel> lineIter = taskLineList.values().iterator();
        while (lineIter.hasNext()) {
            TaskLineConfigModel v = lineIter.next();
            long beginTime = TimeUtil.getDateToMillis(v.getBeginTime());
            long endTime = TimeUtil.getDateToMillis(v.getEndTime());
            long currentTime = System.currentTimeMillis();
            if (currentTime >= beginTime && currentTime < endTime) {//在时间段内
                Map<String, TaskRelationConfigModel> taskRelationList = StoredObjManager.hgetAll(
                        RedisConst.TASK_CONFIG_RELATION_LINE.getProfix(), TaskRelationConfigModel.class);
                Iterator<TaskRelationConfigModel> iter = taskRelationList.values().iterator();
                while (iter.hasNext()) {
                    TaskRelationConfigModel val = iter.next();
                    if (val.getParentTaskId() == 0 && val.getLineId() == v.getLineId()) {
                        TaskConfigModel taskConf = StoredObjManager.hget(
                                RedisConst.TASK_CONFIG_TASK.getProfix(),
                                RedisConst.TASK_CONFIG_TASK.getField() + val.getTaskId(), TaskConfigModel.class);
                        if (userTasks.size() > 0) {
                            boolean exist = false;
                            Iterator<TaskUserHistModel> iter1 = userTasks.iterator();
                            while (iter1.hasNext()) {
                                TaskUserHistModel value = iter1.next();
                                if (value.getTaskId() == taskConf.getTaskId() &&
                                        value.getTaskGameId().equals(taskConf.getTaskGameId()) &&
                                        value.getLineId() == v.getLineId()) {
                                    exist = true;
                                }
                            }
                            if (!exist) {
                                insert(v, taskConf, userId);
                            }
                        } else {
                            insert(v, taskConf, userId);
                        }
                    }
                }
            }
        }
        //获取出来
        List<TaskUserHistModel> taskUserList = TaskDBUtil.getTaskUserHistFromDb(userId);
        if (taskUserList == null) {
            return;
        }

        UserTaskListModel taskList = new UserTaskListModel();

        Iterator<TaskUserHistModel> iter = taskUserList.iterator();
        while (iter.hasNext()) {
            TaskUserHistModel task = iter.next();
            TaskLineConfigModel taskLineConfigModel = StoredObjManager.hget(RedisConst.TASK_CONFIG_LINE.getProfix(),
                    RedisConst.TASK_CONFIG_LINE.getField() + task.getLineId(), TaskLineConfigModel.class);
            long beginTime = TimeUtil.getDateToMillis(taskLineConfigModel.getBeginTime());
            long endTime = TimeUtil.getDateToMillis(taskLineConfigModel.getEndTime());
            long currentTime = System.currentTimeMillis();
            if (currentTime >= beginTime && currentTime < endTime && task.getTaskStatus() != 1) {//在时间段内
                taskList.getTaskMap().put(task.getTaskId(), task);
            }
        }
        StoredObjManager.hset(RedisConst.USER_TASK_LIST.getProfix(), RedisConst.USER_TASK_LIST.getField() + userId, taskList);
    }

    private void insert(TaskLineConfigModel v, TaskConfigModel taskConf, String userId) {
        log.info("insert task,userId:{},lineId,{},taskId:{}", userId, v.getLineId(), taskConf.getTaskId());
        TaskUserHistModel task = new TaskUserHistModel();
        task.setLineId(v.getLineId());
        task.setTaskGameId(taskConf.getTaskGameId());
        task.setTaskStatus((byte) 0);
        task.setTaskTargetValue(taskConf.getTaskTargetValue());
        task.setTaskType(taskConf.getTaskType());
        task.setUserId(userId);
        task.setTaskId(taskConf.getTaskId());
        TaskDBUtil.insert(task);
    }

    public void fillTaskUserStat(String userId) {
        List<TaskUserStatModel> taskUserStatList = TaskDBUtil.getTaskUserStatFromDb(userId);
        if (taskUserStatList != null) {
            UserTaskStatListModel statList = new UserTaskStatListModel();
            Iterator<TaskUserStatModel> iter = taskUserStatList.iterator();
            while (iter.hasNext()) {
                TaskUserStatModel stat = iter.next();
                statList.getUserTaskStatList().put("" + stat.getId(), stat);
            }
            StoredObjManager.hset(RedisConst.USER_TASK_STAT.getProfix(),
                    RedisConst.USER_TASK_STAT.getField() + userId, statList);
        }
    }
}
