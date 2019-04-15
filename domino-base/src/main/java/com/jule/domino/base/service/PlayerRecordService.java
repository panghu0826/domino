package com.jule.domino.base.service;

import JoloProtobuf.AuthSvr.JoloAuth;
import JoloProtobuf.GameSvr.JoloGame;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jule.core.jedis.StoredObjManager;
import com.jule.core.utils.TimeUtil;
import com.jule.domino.base.model.PlayerRecordsModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Slf4j
public class PlayerRecordService {

    private static final Gson gson = new GsonBuilder().serializeNulls().create();

    public static final PlayerRecordService OBJ = new PlayerRecordService();

    //最大记录数
    private static final Integer MAX_RECORDS = 10;

    //redis缓存key
    private static final String REDIS_KEY_PREFIX = "playerrecordserivce_cache_prefix_";
    private static final String REDIS_FILD_PREFIX = "playerrecordserivce_fild_prefix_";

    /**
     * 添加缓存
     * @param uid
     * @param gameOrderId
     * @param roomId
     * @param tableId
     * @param wins
     */
    public void addRecords(String uid, String gameOrderId, String roomId, String tableId, int playType, double wins,boolean iswin){
        try {
            LimitLinkedList<PlayerRecordsModel> queue = new LimitLinkedList<>(MAX_RECORDS);

            //查询缓存数据
            List<PlayerRecordsModel> cache = getModelsFromCache(uid, playType);
            if (cache != null){
                queue.addAll(cache);
            }

            //数据构造
            PlayerRecordsModel model = new PlayerRecordsModel();
            model.setGameOrderId(gameOrderId);
            model.setRoomId(roomId);
            model.setTableId(tableId);
            model.setWins(wins);
            model.setTime(TimeUtil.getDateFormat(new Date()));
            model.setUid(uid);
            model.setWin(iswin);

            //放入队列
            queue.offer(model);

            //缓存数据
            this.setModel2Cache(uid, playType, queue);

        }catch (Exception e){
            log.error("缓存数据失败,exception = {}", e.getMessage());
        }
    }

    /**
     * 查询缓存
     * @param uid
     * @return
     */
    public List<JoloGame.JoloGame_GameRecordsInfo> getPlayRecordsGame(String uid, int playType){
        try {
            LimitLinkedList<PlayerRecordsModel> queue = new LimitLinkedList<>(MAX_RECORDS);

            //查询缓存数据
            List<PlayerRecordsModel> cache = getModelsFromCache(uid, playType);
            if (cache != null){
                //反转列表
                queue.addAll(Lists.reverse(cache));
            }

            //数据构造
            return makeMsgGame(queue);
        }catch (Exception e){
            log.error("查询缓存数据失败,exception = {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * 查询缓存
     * @param uid
     * @return
     */
    public List<JoloAuth.JoloAuth_GameRecordsInfo> getPlayRecordsAuth(String uid, int playType){
        try {
            LimitLinkedList<PlayerRecordsModel> queue = new LimitLinkedList<>(MAX_RECORDS);

            //查询缓存数据
            List<PlayerRecordsModel> cache = getModelsFromCache(uid, playType);
            if (cache != null){
                //反转列表
                queue.addAll(Lists.reverse(cache));
            }

            //数据构造
            return makeMsgAuth(queue);
        }catch (Exception e){
            log.error("查询缓存数据失败,exception = {}", e.getMessage());
        }
        return new ArrayList<>();
    }


    /**
     * 查询玩家缓存记录
     * @return
     */
    private List<PlayerRecordsModel> getModelsFromCache(String uid, int playType){
        try {
            //查询缓存
            String recordsJson = StoredObjManager.hget(REDIS_KEY_PREFIX+uid+"_"+playType, REDIS_FILD_PREFIX);
            if (StringUtils.isEmpty(recordsJson)){
                return null;
            }

            //反序列化
            List<PlayerRecordsModel> list = gson.fromJson(recordsJson, new TypeToken<List<PlayerRecordsModel>>(){}.getType());
            return list;
        }catch (Exception e){
            log.error("查询缓存失败,exception = {}", e.getMessage());
        }
        return null;
    }

    /**
     * 设置缓存
     * @param records
     */
    private void setModel2Cache(String uid, int playType, LimitLinkedList<PlayerRecordsModel> records){
        //序列化
        String recordsJson = gson.toJson(records);
        //缓存
        StoredObjManager.hset(REDIS_KEY_PREFIX+uid+"_"+playType, REDIS_FILD_PREFIX, recordsJson);
    }

    /**
     * 数据构造
     * @param queue
     * @return
     */
    private List<JoloGame.JoloGame_GameRecordsInfo> makeMsgGame(LimitLinkedList<PlayerRecordsModel> queue){
        List<JoloGame.JoloGame_GameRecordsInfo> list = new ArrayList<>();
        if (queue == null){
            return list;
        }

        queue.forEach(e->{
            list.add(JoloGame.JoloGame_GameRecordsInfo.newBuilder()
                    .setGameOrderId(e.getGameOrderId())
                    .setRoomId(e.getRoomId())
                    .setTableId(e.getTableId())
                    .setWins(e.getWins())
                    .setTime(e.getTime())
                    .setIsWin(e.isWin()?0:1)
                    .build());
        });
        return list;
    }

    /**
     * 数据构造
     * @param queue
     * @return
     */
    private List<JoloAuth.JoloAuth_GameRecordsInfo> makeMsgAuth(LimitLinkedList<PlayerRecordsModel> queue){
        List<JoloAuth.JoloAuth_GameRecordsInfo> list = new ArrayList<>();
        if (queue == null){
            return list;
        }

        queue.forEach(e->{
            list.add(JoloAuth.JoloAuth_GameRecordsInfo.newBuilder()
                    .setGameOrderId(e.getGameOrderId())
                    .setRoomId(e.getRoomId())
                    .setTableId(e.getTableId())
                    .setWins(e.getWins())
                    .setTime(e.getTime())
                    .setIsWin(e.isWin()?0:1)
                    .build());
        });
        return list;
    }
}

/**
 * 自定义一个固定长度链表结构
 *
 * @param <E>
 */
class LimitLinkedList <E> implements Queue<E> {

    //队列长度
    private int limit;

    Queue<E> queue = new LinkedList<E>();

    public LimitLinkedList(int limit){
        this.limit = limit;
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return queue.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return queue.iterator();
    }

    @Override
    public Object[] toArray() {
        return queue.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return queue.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return queue.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return queue.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return queue.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return queue.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return queue.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return queue.retainAll(c);
    }

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public boolean offer(E e) {
        if (queue.size() >= limit){
            //如果超出长度,入队时,先出队
            queue.poll();
        }
        return queue.offer(e);
    }

    @Override
    public E remove() {
        return queue.remove();
    }

    @Override
    public E poll() {
        return queue.poll();
    }

    @Override
    public E element() {
        return queue.element();
    }

    @Override
    public E peek() {
        return queue.peek();
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Queue<E> getQueue() {
        return queue;
    }

    public void setQueue(Queue<E> queue) {
        this.queue = queue;
    }
}
