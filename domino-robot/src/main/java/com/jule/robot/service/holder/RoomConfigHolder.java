package com.jule.robot.service.holder;

import com.jule.db.entities.RoomConfigModel;
import com.jule.db.proxy.EntityProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RoomConfigHolder {
    private final static Logger logger = LoggerFactory.getLogger(RoomConfigHolder.class);

    private static List<RoomConfigModel> configList = new ArrayList<>();
    private static Map<String, RoomConfigModel> ROOM_CONFIG = new LinkedHashMap(); //所有房间配置
    private static Map<Integer, List<RoomConfigModel>> INDEX_JOIN_SCORE = new HashMap<>(); //用此索引判断玩家携带积分量，该进入哪个房间游戏

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
     * 初始化入桌积分的索引值
     * @param model
     */
    public void initJoinScoreIndex(RoomConfigModel model){
        Long minScore4JoinTable = new Long(model.getMinScore4JoinTable());
        Integer index = minScore4JoinTable.toString().length();
        List<RoomConfigModel> list = new ArrayList();
        if(INDEX_JOIN_SCORE.containsKey(index)){
            list = INDEX_JOIN_SCORE.get(index);
        }

        //将配置信息放入list
        list.add(model);

        INDEX_JOIN_SCORE.put(index, list);
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
     * 获取所有配置
     *
     * @return
     */
    public Map<String, RoomConfigModel> getAllConfig() {
        return ROOM_CONFIG;
    }

    /**
     * 根据积分数量，获取最适合此积分数进入的房间
     * @param score
     * @return
     */
    public RoomConfigModel getRoomConfigByScore(long score){
        Integer index = new Long(score).toString().length();
        List<RoomConfigModel> indexList = INDEX_JOIN_SCORE.get(index);
        if(null != indexList && indexList.size() > 0){
            RoomConfigModel[] arrModel = indexList.toArray(new RoomConfigModel[indexList.size()]);
            Arrays.sort(arrModel, Collections.reverseOrder()); //排序房间列表
            //从最大的入场限制开始循环
            for(RoomConfigModel model : arrModel){
                if(score >= model.getMinScore4JoinTable()){
                    return model;
                }
            }
        }else{
            //如果索引中找不到指定的值，那么循环全部房间信息，找到最合适的房间提供给用户
            Object[] arrModel = (Object[])configList.toArray();
            Arrays.sort(arrModel, Collections.reverseOrder()); //排序房间列表
            for(Object object : arrModel){
                RoomConfigModel model = (RoomConfigModel)object;
                if(score >= model.getMinScore4JoinTable()){
                    return model;
                }
            }
        }
        return null;
    }

    /**
     * 初始化内存
     */
    public void init(){
        configList = EntityProxy.OBJ.getResultList("1=1",null,RoomConfigModel.class);
        for(int i=0; i < configList.size(); i++){
            RoomConfigModel model = configList.get(i);
            ROOM_CONFIG.put(model.getRoomId(), model);
            logger.debug("加载房间："+model.toString());
            initJoinScoreIndex(model);
        }
        logger.info("加载 " + ROOM_CONFIG.size() + " 个房间配置");
    }

    public List<RoomConfigModel> getAllRoomConfig(){
        return configList;
    }
}
