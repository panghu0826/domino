package com.jule.domino.game.service.holder;

import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.RoomConfigModel;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

@Slf4j
public class RoomConfigHolder {
    private static Map<String, RoomConfigModel> ROOM_CONFIG = new HashMap<>(); //所有房间配置

    private static List<RoomConfigModel> ROOM_JOIN_SCORE = new ArrayList<>(); //用此索引判断玩家携带积分量，该进入哪个房间游戏

    private long minJoinTableScore = Long.MAX_VALUE;

    private static class SingletonHolder {
        protected static final RoomConfigHolder instance = new RoomConfigHolder();
    }

    public static final RoomConfigHolder getInstance() {
        return RoomConfigHolder.SingletonHolder.instance;
    }

    private RoomConfigHolder() {
        init();
    }

    /**
     * @param roomId
     * @return
     */
    //获取桌子的所有信息
    public RoomConfigModel getRoomConfig(String roomId) {
        return ROOM_CONFIG.get(roomId);
    }

    /**
     * 初始化内存
     */
    public boolean init(){
        List<RoomConfigModel> configList = DBUtil.getRoomConfigFromDb();
        if(configList == null){
            log.error(" room's config is null ");
            throw new Error("无房间配置信息");
        }
        for (int i = 0; i < configList.size(); i++) {
            RoomConfigModel model = configList.get(i);
            if (minJoinTableScore > model.getMinScore4JoinTable()) {
                minJoinTableScore = model.getMinScore4JoinTable();
            }
            ROOM_CONFIG.put(model.getRoomId(), model);
            log.debug("加载桌子：roomId->" + model.getRoomId());
            //将配置信息放入list
            ROOM_JOIN_SCORE.add(model);
        }
        log.info("加载 " + ROOM_CONFIG.size() + " 桌子配置");
        log.info("minJoinTableScore = " + minJoinTableScore);
        return true;
    }

    public long getMinJoinTableScore() {
        List<RoomConfigModel> configList = DBUtil.getRoomConfigFromDb();
        return configList.get(0).getMinScore4JoinTable();
    }

    /**
     * 获取所有配置
     *
     * @return
     */
    public Map<String, RoomConfigModel> getAllConfig() {
        return ROOM_CONFIG;
    }

    /**
     * 是否能找到相应的房间
     *
     * @param score
     * @return
     */
    public boolean canFindSuuitableRoom(double score) {
        if (score >= minJoinTableScore) {
            return true;
        }
        return false;
    }
    public RoomConfigModel getRoomConfigByScore(double score) {
        Collections.sort(ROOM_JOIN_SCORE);
        for (int i = ROOM_JOIN_SCORE.size() - 1; i >= 0; i--) {
            log.debug(score + "::" + ROOM_JOIN_SCORE.get(i).getMinScore4JoinTable());
            if (score >= ROOM_JOIN_SCORE.get(i).getMinScore4JoinTable()) {
                return ROOM_JOIN_SCORE.get(i);
            }
        }
        return null;
    }

}
