package com.jule.domino.game.service;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.core.jedis.StoredObjManager;
import com.jule.core.service.ThreadPoolManager;
import com.jule.core.utils.TimeUtil;
import com.jule.domino.base.dao.bean.*;
import com.jule.domino.game.dao.TaskDBUtil;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.model.eenum.TaskTypeEnum;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.play.TableUtil;
import com.jule.domino.game.service.holder.FunctionIdHolder;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.TaskAwardsModel;
import com.jule.domino.base.model.UserTaskListModel;
import com.jule.domino.base.model.UserTaskStatListModel;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class TaskService {


    //<userId,<id,TaskInfo>>
    private Map<String, Map<Long, TaskUserHistModel>> taskMap = new ConcurrentHashMap<>();
    private Map<String, Map<Long, TaskUserStatModel>> taskStatMap = new ConcurrentHashMap<>();

    public void initConfig() {
        if (StoredObjManager.setnx(RedisConst.GAME_TASK_LOADING.getProfix(),
                RedisConst.GAME_TASK_LOADING.getField()) == 0L) {//两个小时内不重复加载
            return;
        }
        StoredObjManager.setEx(RedisConst.GAME_TASK_LOADING.getProfix(),
                RedisConst.GAME_TASK_LOADING.getField(), 60 * 60 * 2);
        //加载任务线配置
        List<TaskLineConfigModel> taskLineList = TaskDBUtil.getTaskLineConfigFromDb();
        if (taskLineList != null) {
            taskLineList.forEach(line -> {
                StoredObjManager.hset(RedisConst.TASK_CONFIG_LINE.getProfix(), RedisConst.TASK_CONFIG_LINE.getField() + line.getLineId(), line);
            });
        }
        //加载任务关联配置
        List<TaskRelationConfigModel> taskRelationList = TaskDBUtil.getTaskRelationConfigFromDb();
        if (taskRelationList != null) {
            taskRelationList.forEach(relation -> {
                StoredObjManager.hset(RedisConst.TASK_CONFIG_RELATION.getProfix(),
                        RedisConst.TASK_CONFIG_RELATION.getField() + relation.getLineId() + relation.getTaskId(),
                        relation);
                StoredObjManager.hset(RedisConst.TASK_CONFIG_RELATION_LINE.getProfix(),
                        RedisConst.TASK_CONFIG_RELATION_LINE.getField() + relation.getLineId(), relation);
            });
        }
        //加载任务配置列表
        List<TaskConfigModel> taskList = TaskDBUtil.getTaskConfigFromDb();
        if (taskList != null) {
            taskList.forEach(task -> {
                StoredObjManager.hset(RedisConst.TASK_CONFIG_TASK.getProfix(),
                        RedisConst.TASK_CONFIG_TASK.getField() + task.getTaskId(),
                        task);

                //加载任务奖励列表
                List<TaskAwardConfigModel> awardList = TaskDBUtil.getTaskAwardConfigFromDb(task.getTaskId());
                if (awardList != null) {
                    TaskAwardsModel awardsModel = new TaskAwardsModel();
                    awardList.forEach(award -> {
                        awardsModel.getTaskAwards().put("" + award.getId(), award);
                    });
                    StoredObjManager.hset(RedisConst.TASK_CONFIG_AWARD.getProfix(),
                            RedisConst.TASK_CONFIG_AWARD.getField() + task.getTaskId(),
                            awardsModel);
                }
            });
        }
    }

    public void delUserTask(TaskUserHistModel userTask) {
        Map<Long, TaskUserHistModel> userTaskMap = taskMap.get(userTask.getUserId());
        userTaskMap.remove(userTask.getId());
        saveUserTask(userTask.getUserId());
    }

    public void winNum(AbstractTable table, List<String> winUserIds) {
        log.info("WinNum" + winUserIds.toString());
        String gameId = "" + table.getPlayType();
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < winUserIds.size(); i++) {
                    String userId = winUserIds.get(i);
                    TaskUserHistModel userTask = judgeTaskByType(userId, gameId, TaskTypeEnum.WIN_NUM);
                    if (userTask == null) {
                        continue;
                    }
                    log.info("winNum(),handler,userId:{}", userId);
                    handler(userId, gameId, table, userTask, TaskTypeEnum.WIN_NUM);
                }
            }
        });

    }

    public void winningStreakNum(AbstractTable table, List<String> winUserIds) {

        String gameId = "" + table.getPlayType();
        List<String> loseUserIds = new ArrayList<>();
        Iterator<String> iter = table.getLoseUserIds().iterator();
        while (iter.hasNext()) {
            String id = iter.next();
            if (winUserIds.indexOf(id) == -1) {
                loseUserIds.add(id);
            }
        }
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < loseUserIds.size(); i++) {
                    String userId = loseUserIds.get(i);
                    TaskUserHistModel userTask = judgeTaskByType(userId, gameId, TaskTypeEnum.WINNING_STREAK_NUM);
                    if (userTask == null) {
                        continue;
                    }
                    handler(userId, gameId, table, userTask, TaskTypeEnum.WINNING_STREAK_NUM, false);
                }

                for (int i = 0; i < winUserIds.size(); i++) {
                    String userId = winUserIds.get(i);
                    TaskUserHistModel userTask = judgeTaskByType(userId, gameId, TaskTypeEnum.WINNING_STREAK_NUM);
                    if (userTask == null) {
                        continue;
                    }
                    handler(userId, gameId, table, userTask, TaskTypeEnum.WINNING_STREAK_NUM, true);
                }
            }
        });

    }

    /**
     * 牌局
     *
     * @param table
     */
    public void cardNum(AbstractTable table) {
        String gameId = "" + table.getPlayType();
        Iterator<PlayerInfo> iter = table.getInGamePlayers().values().iterator();
        List<String> ids = new ArrayList<>();
        while (iter.hasNext()) {
            PlayerInfo p = iter.next();
            ids.add(p.getPlayerId());
        }
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < ids.size(); i++) {
                    String userId = ids.get(i);
                    TaskUserHistModel userTask = judgeTaskByType(userId, gameId, TaskTypeEnum.CARD_NUM);
                    if (userTask == null) {
                        continue;
                    }

                    handler(userId, gameId, table, userTask, TaskTypeEnum.CARD_NUM);

                }
            }
        });
    }

    private void handler(String userId, String gameId, AbstractTable table, TaskUserHistModel userTask, TaskTypeEnum taskTypeEnum) {
        handler(userId, gameId, table, userTask, taskTypeEnum, false);
    }

    private void handler(String userId, String gameId, AbstractTable table, TaskUserHistModel userTask, TaskTypeEnum taskTypeEnum, boolean plus) {
        log.info("handler()，userId:{},gameId:{},tableInfo:{},userTask:{},taskTypeEnum:{}", userId, gameId, TableUtil.toStringNormal(table), userTask.toString(), taskTypeEnum);
        Map<Long, TaskUserStatModel> taskStat = taskStatMap.get(userId);
        boolean hasItem = false;
        if (taskStat != null) {
            Iterator<TaskUserStatModel> taskStatIte = taskStat.values().iterator();
            while (taskStatIte.hasNext()) {
                TaskUserStatModel m = taskStatIte.next();
                if (m.getTaskGameId().equals(gameId) && m.getUserId().equals(userId)
                        && m.getTaskType() == taskTypeEnum.getIndex()) {
                    if (taskTypeEnum.equals(TaskTypeEnum.CARD_NUM) || taskTypeEnum.equals(TaskTypeEnum.WIN_NUM)) {
                        m.setValue(m.getValue() + 1);
                    }
                    if (TaskTypeEnum.WINNING_STREAK_NUM.equals(taskTypeEnum) && plus) {
                        m.setTmpValue(m.getTmpValue() + 1);
                        if (m.getTmpValue() > m.getValue()) {
                            m.setValue(m.getTmpValue());
                        }
                    }
                    if (TaskTypeEnum.WINNING_STREAK_NUM.equals(taskTypeEnum) && !plus) {//加一判断达标不能清空
                        TaskConfigModel task = StoredObjManager.hget(RedisConst.TASK_CONFIG_TASK.getProfix(),
                                RedisConst.TASK_CONFIG_TASK.getField() + userTask.getTaskId(),
                                TaskConfigModel.class);
                        if (m.getValue() < task.getTaskTargetValue()) {
                            m.setTmpValue(0);
                            m.setValue(0);
                        }
                    }
                    hasItem = true;
                    TaskUserStatModel tmp = TaskDBUtil.insertOrUpdateTaskUserStat(m);
                    m.setId(tmp.getId());
                }
            }
        }
        if (!hasItem) {
            taskStat = taskStatMap.get(userId);
            if (taskStat == null) {
                taskStat = new ConcurrentHashMap<>();
                taskStatMap.put(userId, taskStat);
            }
            TaskUserStatModel model = new TaskUserStatModel();
            if (taskTypeEnum.equals(TaskTypeEnum.CARD_NUM) || taskTypeEnum.equals(TaskTypeEnum.WIN_NUM)) {
                model.setValue(1);
            }
            if (TaskTypeEnum.WINNING_STREAK_NUM.equals(taskTypeEnum) && plus) {
                model.setValue(1);
            }
            if (TaskTypeEnum.WINNING_STREAK_NUM.equals(taskTypeEnum) && !plus) {
                model.setValue(0);
            }
            model.setTaskType((byte) taskTypeEnum.getIndex());
            model.setTaskGameId(gameId);
            model.setUserId(userId);

            TaskUserStatModel tmp = TaskDBUtil.insertOrUpdateTaskUserStat(model);
            model.setId(tmp.getId());
            taskStat.put(model.getId(), model);
        }
        //保存到缓存
        saveUserTaskStat(userId);
        //发消息
        TaskConfigModel task = StoredObjManager.hget(RedisConst.TASK_CONFIG_TASK.getProfix(),
                RedisConst.TASK_CONFIG_TASK.getField() + userTask.getTaskId(),
                TaskConfigModel.class);
        JoloGame.JoloGame_TaskInfo.Builder taskInfo = JoloGame.JoloGame_TaskInfo.newBuilder();
        taskInfo.setTaskId(userTask.getId());
        taskInfo.setTaskName(task.getTaskName());
        taskInfo.setTaskIcon(task.getTaskIcon());
        taskInfo.setTaskType(task.getTaskType());
        taskInfo.setTaskDesc(task.getTaskDesc());
        taskInfo.setGameId(task.getTaskGameId());
        taskInfo.addAllTaskAwards(getTaskAwardInfo(userTask.getTaskId()));
        taskInfo.setTargetValue(task.getTaskTargetValue());
        taskInfo.setValue(getValueBy(userId, gameId, task.getTaskType()));
        log.debug("send51061 handler userId:{},task id :{},taskType:{},value:{}",
                userId, userTask.getId(), taskTypeEnum.getIndex(), taskInfo.getValue());
        table.boardcastMessageSingle(userId, JoloGame.JoloGame_Notice2Client_TaskProgReq.newBuilder()
                        .setUserId(userId)
                        .setRoomId(table.getRoomId())
                        .setTableId(table.getTableId())
                        .setResult(1)
                        .setTaskInfo(taskInfo)
                        .build(),
                FunctionIdHolder.Game_Notice_TaskProg);
    }

    /**
     * 是否在时间段内
     *
     * @param userId
     * @param id
     * @return
     */

    public boolean checkTimePeriod(String userId, long id) {
        Map<Long, TaskUserHistModel> userTasks = taskMap.get(userId);
        if (userTasks != null) {
            Iterator<TaskUserHistModel> iter = userTasks.values().iterator();
            while (iter.hasNext()) {
                TaskUserHistModel task = iter.next();
                TaskLineConfigModel taskLineConfigModel = StoredObjManager.hget(RedisConst.TASK_CONFIG_LINE.getProfix(),
                        RedisConst.TASK_CONFIG_LINE.getField() + task.getLineId(), TaskLineConfigModel.class);
                long beginTime = TimeUtil.getDateToMillis(taskLineConfigModel.getBeginTime());
                long endTime = TimeUtil.getDateToMillis(taskLineConfigModel.getEndTime());
                long currentTime = System.currentTimeMillis();
                if (task.getId() == id && currentTime >= beginTime && currentTime < endTime) {
                    return true;
                }
            }
        }
        return false;
    }

    public TaskUserHistModel checkTaskAward(String userId, long id) {
        Map<Long, TaskUserHistModel> userTasks = taskMap.get(userId);
        if (userTasks != null) {
            Iterator<TaskUserHistModel> iter = userTasks.values().iterator();
            while (iter.hasNext()) {
                TaskUserHistModel task = iter.next();
                if (task.getId() == id && task.getTaskStatus() != 1) {
                    return task;
                }
            }
        }
        return null;
    }

    /**
     * @param userId 玩家Id
     * @param type   1：牌局，2：赢局，3:连胜
     * @return
     */
    public TaskUserHistModel judgeTaskByType(String userId, String gameId, TaskTypeEnum type) {
        StringBuilder sb = new StringBuilder();
        sb.append("judgeTaskBy()，userId:" + userId + ",gameId:" + gameId + ",type:" + type.getIndex() + ",found:");
        Map<Long, TaskUserHistModel> userTasks = taskMap.get(userId);
        TaskUserHistModel tmp = null;
        if (userTasks != null) {
            Iterator<TaskUserHistModel> iter = userTasks.values().iterator();
            while (iter.hasNext()) {
                TaskUserHistModel task = iter.next();
                if (task.getTaskType() == type.getIndex() && task.getTaskStatus() != 1
                        && task.getTaskGameId().equals(gameId)) {
                    sb.append("true");
                    tmp = task;
                    break;
                }
            }
        }
        if (tmp == null) {
            sb.append("false");
        }
        log.info(sb.toString());
        return tmp;
    }

    public List<JoloGame.JoloGame_TaskInfo> getAllTaskList(String userId, String gameId) {
        log.info("getAllTaskList(),userId:{},gameId:{}", userId, gameId);
        List<JoloGame.JoloGame_TaskInfo> list = new ArrayList<>();
        Map<Long, TaskUserHistModel> map = taskMap.get(userId);
        if (map == null) {
            return list;
        }

        Iterator<TaskUserHistModel> iterator = map.values().iterator();
        while (iterator.hasNext()) {
            TaskUserHistModel v = iterator.next();
            TaskConfigModel taskConfig = StoredObjManager.hget(RedisConst.TASK_CONFIG_TASK.getProfix(),
                    RedisConst.TASK_CONFIG_TASK.getField() + v.getTaskId(), TaskConfigModel.class);
            log.info("taskConfig.gameId:{},taskGameId==gameId?:{}", taskConfig.getTaskGameId(), taskConfig.getTaskGameId().equals(gameId));
            if (!taskConfig.getTaskGameId().equals(gameId)) {
                continue;
            }
            log.info("taskConfig.gameId:{},taskGameId==gameId?:{}", taskConfig.getTaskGameId(), taskConfig.getTaskGameId().equals(gameId));
            List<JoloGame.JoloGame_TaskAwardInfo> awardList = getTaskAwards(v.getTaskId());

            list.add(JoloGame.JoloGame_TaskInfo.newBuilder()
                    .setGameId(v.getTaskGameId())
                    .setTaskId(v.getId())
                    .setTaskName(taskConfig.getTaskName())
                    .setTaskIcon(taskConfig.getTaskIcon())
                    .setTaskType(taskConfig.getTaskType())
                    .setTaskDesc(taskConfig.getTaskDesc())
                    .setTargetValue(taskConfig.getTaskTargetValue())
                    .setValue(getValueBy(userId, taskConfig.getTaskGameId(), v.getTaskType()))
                    .addAllTaskAwards(awardList)
                    .build());
        }
        return list;
    }

    public int getValueBy(String userId, String gameId, int taskType) {
        int val = 0;
        Map<Long, TaskUserStatModel> taskStatM = taskStatMap.get(userId);
        if (taskStatM == null) {
            log.debug("userId:{},gameId:{},taskType:{}", userId, gameId, taskType);
            return val;
        }
        Iterator<TaskUserStatModel> itr = taskStatM.values().iterator();
        while (itr.hasNext()) {
            TaskUserStatModel model = itr.next();
            if (model.getTaskType() == taskType && model.getTaskGameId().equals(gameId)) {
                log.debug("getValueBy(),userId:{},gameId:{},taskType:{},value:{}", userId, gameId, taskType, model.getValue());
                return model.getValue();
            }
        }
        return val;
    }

    public List<JoloGame.JoloGame_TaskAwardInfo> getTaskAwardInfo(int taskId) {
        TaskAwardsModel awards = StoredObjManager.hget(RedisConst.TASK_CONFIG_AWARD.getProfix(),
                RedisConst.TASK_CONFIG_AWARD.getField() + taskId,
                TaskAwardsModel.class);
        List<JoloGame.JoloGame_TaskAwardInfo> taskAwards = new ArrayList<>();
        if (awards != null) {
            Iterator<TaskAwardConfigModel> iter = awards.getTaskAwards().values().iterator();
            while (iter.hasNext()) {
                TaskAwardConfigModel model = iter.next();
                JoloGame.JoloGame_TaskAwardInfo.Builder awardInfo = JoloGame.JoloGame_TaskAwardInfo.newBuilder();
                awardInfo.setItemId(model.getItemId());
                awardInfo.setType(model.getType());
                awardInfo.setUrl(model.getUrl() == null ? "" : model.getUrl());
                awardInfo.setName(model.getName() == null ? "" : model.getName());
                awardInfo.setNum(model.getNum());
                taskAwards.add(awardInfo.build());
            }
        }
        return taskAwards;
    }

    public TaskUserHistModel getNextTask(TaskUserHistModel userTask) {
        TaskRelationConfigModel relationConfig = StoredObjManager.hget(RedisConst.TASK_CONFIG_RELATION.getProfix(),
                RedisConst.TASK_CONFIG_RELATION.getField() + userTask.getLineId() + userTask.getTaskId(),
                TaskRelationConfigModel.class);
        int newTaskId = relationConfig.getChildTaskId();
        if (newTaskId == 0) {
            Map<String, TaskRelationConfigModel> taskRelationList = StoredObjManager.hgetAll(
                    RedisConst.TASK_CONFIG_RELATION_LINE.getProfix(), TaskRelationConfigModel.class);
            Iterator<TaskRelationConfigModel> iter = taskRelationList.values().iterator();
            while (iter.hasNext()) {
                TaskRelationConfigModel relaConfig = iter.next();
                if (relaConfig.getParentTaskId() == 0 && relaConfig.getLineId() == userTask.getLineId()) {
                    newTaskId = relaConfig.getTaskId();
                    break;
                }
            }

        }
        TaskConfigModel taskConfig = StoredObjManager.hget(RedisConst.TASK_CONFIG_TASK.getProfix(),
                RedisConst.TASK_CONFIG_TASK.getField() + newTaskId,
                TaskConfigModel.class);
        TaskUserHistModel task = new TaskUserHistModel();
        task.setLineId(relationConfig.getLineId());
        task.setTaskGameId(taskConfig.getTaskGameId());
        task.setTaskStatus((byte) 0);
        task.setTaskTargetValue(taskConfig.getTaskTargetValue());
        task.setTaskType(taskConfig.getTaskType());
        task.setUserId(userTask.getUserId());
        task.setTaskId(taskConfig.getTaskId());
        TaskUserHistModel result = TaskDBUtil.insert(task);
        if (result != null) {
            Map<Long, TaskUserHistModel> taskUserHist = taskMap.get(userTask.getUserId());
            if (taskUserHist == null) {
                taskUserHist = new ConcurrentHashMap<>();
                taskMap.put(userTask.getUserId(), taskUserHist);
            }
            taskUserHist.put(result.getId(), result);
            saveUserTask(userTask.getUserId());
        }
        return result;
    }

    /**
     * 判断是否有符合条件的任务
     *
     * @return
     */
    public boolean hasMatchCondition(String userId) {
        AtomicBoolean has = new AtomicBoolean(false);
        UserTaskListModel model = StoredObjManager.hget(RedisConst.USER_TASK_LIST.getProfix(),
                RedisConst.USER_TASK_LIST.getField() + userId, UserTaskListModel.class);
        if (model == null) {
            log.debug("userId:{},userTaskList is null.", userId);
            has.set(false);
            return has.get();
        }
        Map<Long, TaskUserHistModel> taskUserHist = taskMap.get(userId);
        //if(taskUserHist==null){
        taskUserHist = new ConcurrentHashMap<>();
        taskMap.put(userId, taskUserHist);
        // }

        Iterator<TaskUserHistModel> iter = model.getTaskMap().values().iterator();
        while (iter.hasNext()) {
            TaskUserHistModel v = iter.next();
            //判断时间段、目标达成否
            TaskLineConfigModel taskLineConfigModel = StoredObjManager.hget(RedisConst.TASK_CONFIG_LINE.getProfix(),
                    RedisConst.TASK_CONFIG_LINE.getField() + v.getLineId(), TaskLineConfigModel.class);
            long beginTime = TimeUtil.getDateToMillis(taskLineConfigModel.getBeginTime());
            long endTime = TimeUtil.getDateToMillis(taskLineConfigModel.getEndTime());
            long currentTime = System.currentTimeMillis();
            if (currentTime > beginTime && currentTime < endTime) {
                if (v.getTaskStatus() != GameConst.TaskFinished) {
                    taskUserHist.put(v.getId(), v);
                    has.set(true);
                }

            }
        }
        StringBuilder sb = new StringBuilder();
        Iterator<TaskUserHistModel> tasks = taskUserHist.values().iterator();
        while (tasks.hasNext()) {
            TaskUserHistModel task = tasks.next();
            sb.append(task.toString());
        }
        log.info("userId:{},tasks:{}", userId, sb.toString());
        //把任务状态数据加载进来
        if (taskStatMap.get(userId) == null) {
            UserTaskStatListModel statList = StoredObjManager.hget(RedisConst.USER_TASK_STAT.getProfix(),
                    RedisConst.USER_TASK_STAT.getField() + userId, UserTaskStatListModel.class);
            if (statList != null) {
                Map<Long, TaskUserStatModel> tmp = new ConcurrentHashMap<>();
                Iterator<TaskUserStatModel> statIter = statList.getUserTaskStatList().values().iterator();
                while (statIter.hasNext()) {
                    TaskUserStatModel statModel = statIter.next();
                    tmp.put(statModel.getId(), statModel);
                }
                taskStatMap.put(userId, tmp);

            }
        }
        return has.get();
    }

    public List<JoloGame.JoloGame_TaskAwardInfo> getTaskAwards(int taskConfigId) {
        List<JoloGame.JoloGame_TaskAwardInfo> list = new ArrayList<>();
        TaskAwardsModel taskAwardsModel = StoredObjManager.hget(RedisConst.TASK_CONFIG_AWARD.getProfix(),
                RedisConst.TASK_CONFIG_AWARD.getField() + taskConfigId, TaskAwardsModel.class);
        if (taskAwardsModel != null) {
            Iterator<TaskAwardConfigModel> iter = taskAwardsModel.getTaskAwards().values().iterator();
            while (iter.hasNext()) {
                TaskAwardConfigModel award = iter.next();
                list.add(JoloGame.JoloGame_TaskAwardInfo.newBuilder()
                        .setItemId(award.getItemId())
                        .setType(award.getType())
                        .setUrl(award.getUrl() == null ? "" : award.getUrl())
                        .setName(award.getName() == null ? "" : award.getName())
                        .setNum(award.getNum())
                        .build());
            }
        }
        return list;
    }

    private void saveUserTaskStat(String userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("saveUserTaskStat(),userId:" + userId);

        Map<Long, TaskUserStatModel> taskStat = taskStatMap.get(userId);
        UserTaskStatListModel userTaskStatListModel = new UserTaskStatListModel();
        if (taskStat != null) {
            Iterator<TaskUserStatModel> iter = taskStat.values().iterator();
            while (iter.hasNext()) {
                TaskUserStatModel stat = iter.next();
                userTaskStatListModel.getUserTaskStatList().put("" + stat.getId(), stat);
                sb.append(",taskStat:" + stat.toString());
            }
            StoredObjManager.hset(RedisConst.USER_TASK_STAT.getProfix(),
                    RedisConst.USER_TASK_STAT.getField() + userId, userTaskStatListModel);
            log.info(sb.toString());
        }

    }

    private void saveUserTask(String userId) {
        Map<Long, TaskUserHistModel> taskStat = taskMap.get(userId);
        UserTaskListModel taskList = new UserTaskListModel();
        Iterator<TaskUserHistModel> iterator = taskStat.values().iterator();
        while (iterator.hasNext()) {
            TaskUserHistModel task = iterator.next();
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

    public TaskUserStatModel clearUserTaskStat(String userId, String gameId, int taskType) {
        int val = 0;
        Map<Long, TaskUserStatModel> taskStatM = taskStatMap.get(userId);
        if (taskStatM == null) {
            return null;
        }
        Iterator<TaskUserStatModel> itr = taskStatM.values().iterator();
        while (itr.hasNext()) {
            TaskUserStatModel model = itr.next();
            if (model.getTaskType() == taskType && model.getTaskGameId().equals(gameId)) {
                model.setValue(0);
                model.setTmpValue(0);
                TaskDBUtil.insertOrUpdateTaskUserStat(model);
                saveUserTaskStat(userId);
                return model;
            }
        }
        return null;
    }

    private static class SingletonHolder {
        protected static final TaskService instance = new TaskService();
    }

    public static final TaskService getInstance() {
        return TaskService.SingletonHolder.instance;
    }
}
