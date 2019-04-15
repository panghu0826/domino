package com.jule.domino.game.service;

import com.jule.domino.game.model.TimerKey;
import com.jule.domino.game.network.protocol.TableInnerReq;
import com.jule.domino.base.model.RoomTableRelationModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 房间倒计时调度服务
 */
@Slf4j
public class TimerService {
    private static final Map<TimerKey, TableInnerReq> EXPIRABLES = new ConcurrentHashMap<>();
    private static final ReentrantLock lock = new ReentrantLock();

    private static class SingletonHolder {
        protected static final TimerService instance = new TimerService();
    }

    public static final TimerService getInstance() {
        return SingletonHolder.instance;
    }

    public TimerService() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                long timeNow = System.currentTimeMillis();

                //TableInnerReq
                for (Map.Entry<TimerKey, TableInnerReq> entry : EXPIRABLES.entrySet()) {
                    if (entry == null || entry.getKey() == null) {
                        log.error("Found Timer is null, entryIsNull={}, entry.getKey()IsNull={}",
                                null == entry, null==entry?"NULL":entry.getKey()==null);
                        continue;
                    }
                    long endTime = entry.getKey().time;
                    TableInnerReq tableInnerReq = entry.getValue();
                    if (timeNow - endTime >= 0) {
                        EXPIRABLES.remove(entry.getKey());
                        tableInnerReq.sendToSelf();
                    }
                }
                //TableInfo
                TableService.getInstance().timeOutLeaveTable();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {

            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }


    /**
     * 增加一个倒计时的任务
     *
     * @param delaySec       延迟时间（单位：秒）
     * @param tableInnerReq
     */
    public void addTimerTask(long delaySec, RoomTableRelationModel relationModel, final TableInnerReq tableInnerReq) {
        long _endTime = System.currentTimeMillis() + delaySec * 1000;
        lock.lock();
        try {
//            delTimerTask(relationModel);
            EXPIRABLES.put(new TimerKey(_endTime, relationModel), tableInnerReq);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 删除一个倒计时的任务
     * <p>
     * //     * @param tableId 倒计时的时间
     */
    public void delTimerTask(RoomTableRelationModel relationModel) {
        //lock.lock(); modify lyb 2018-07-09 EXPIRABLES是线程安全的
        try {
            TimerKey timerKey = new TimerKey(relationModel);
            EXPIRABLES.remove(timerKey);
        } finally {
            //lock.unlock();
        }
    }

    public int getLeftCountDown(RoomTableRelationModel relationModel) {
        try {
        long timeNow = System.currentTimeMillis();
        for (Map.Entry<TimerKey, TableInnerReq> entry : EXPIRABLES.entrySet()) {
            if (entry == null || entry.getKey() == null) {
                log.error("getLeftCountDown(),Found Timer is null, entryIsNull={}, entry.getKey()IsNull={}",
                        null == entry, null==entry?"NULL":entry.getKey()==null);
                continue;
            }
            TimerKey timerKey = entry.getKey();
            if (timerKey.hashCode() == relationModel.hashCode()) {
                long endTime = entry.getKey().time;
                return (int) ((endTime - timeNow) / 1000);
            }
        }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return 0;
    }

}
