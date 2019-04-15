package com.jule.domino.game.room.service;

import JoloProtobuf.RoomSvr.JoloRoom;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.RoomConfigModel;
import com.jule.domino.game.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 房间配置
 *
 * @author
 *
 * @since 2018/9/11 16:27
 *
 */
@Slf4j
public class RoomConfigSerivce {

    //单例
    public static final RoomConfigSerivce OBJ = new RoomConfigSerivce();

    public static List<RoomConfigModel> _configs = new ArrayList<>();

    private static final Gson gson = new GsonBuilder().serializeNulls().create();

    public RoomConfigSerivce() {
        //启动定时加载
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                ()->loadData(),
                1,
                5,
                TimeUnit.MINUTES);
    }

    /**
     * 初始化
     */
    public void init(){
        loadData();
    }

    /**
     * 加载数据
     */
    public void loadData(){
        log.info("载入房间配置到内存");
        List<RoomConfigModel> configs = DBUtil.getRoomConfigFromDb();
        if (configs == null){
            log.error("无房间配置");
            return;
        }
        _configs = configs;
    }

    public List<JoloRoom.JoloRoom_RoomInfo> getRoomConfigs() {
        List<JoloRoom.JoloRoom_RoomInfo> _rooms = new ArrayList<>();
        _configs.forEach(e->{
            _rooms.add(JoloRoom.JoloRoom_RoomInfo.newBuilder()
                    .setRoomId(e.getRoomId())
                    .setRoomName("").setRoomDesc("")
                    .setMinJoin(e.getMinScore4JoinTable())
                    .setAnte(e.getAnte())
                    .addAllDoubleRoles(makeIntegerArray(e.getDoubleRoles()))
                    .setOnLiners(getOnliners(e.getId()))
                    .build());
        });
        return _rooms;
    }

    private long getOnliners(int id){
        long realPlayer = TableService.getInstance().countPlayers(id);
        log.debug("在线人数真实值={}",realPlayer);
        try {
            Optional<RoomConfigModel> config = _configs.stream().filter(e -> e.getId() == id).findFirst();
            if (config == null) return realPlayer;

            //计算规则
            String onlineRole = config.get().getOnlinerRoles();

            List<String> roles = makeArray(onlineRole);
            //规则参数异常
            if (roles == null || roles.size() != 5){
                return realPlayer;
            }

            log.debug("在线人数规则onlineRole={}, roles={}", onlineRole, roles.toString());
            int x = Integer.valueOf(roles.get(0));
            float y = Float.valueOf(roles.get(1));
            int A = Integer.valueOf(roles.get(2));
            int B = Integer.valueOf(roles.get(3));
            int C = Integer.valueOf(roles.get(4));

            //房间占比
            double roompre = 0;
            double A1 = getRandomDouble(A * 0.8, A * 1.2);
            double B1 = getRandomDouble(B * 0.8, B * 1.2);
            double C1 = getRandomDouble(C * 0.8, C * 1.2);
            if (id == 5){
                roompre = A1;
            }else if (id == 6){
                roompre = B1;
            }else if (id == 7){
                roompre = C1;
            }else {
                roompre = (1 - A1 - B1 - C1) >=0 ? (1 - A1 - B1 - C1) : 0;
            }

            double playersTmp = x * roompre + realPlayer * y;

            long players = Long.parseLong(NumUtils.double2String(Math.ceil(playersTmp)));

            return players;
        }catch (Exception e){
            log.error("转换异常",e);
        }

        return realPlayer;
    }

    private double getRandomDouble(double minNum, double maxNum){
        double boundedDouble = minNum + new Random(System.currentTimeMillis()+System.nanoTime()).nextDouble() * (maxNum - minNum);
        return boundedDouble;
    }

    private <T> List<T> makeArray(String str){
        List<T> list = new ArrayList<>();
        try {
            if (StringUtils.isEmpty(str)){
                return list;
            }
            list = gson.fromJson(str , new TypeToken<List<T>>(){}.getType());
            return list;
        }catch (Exception e){
            log.error("转换规则字符串失败 str = {}, exception = {}", str, e.getMessage());
            return list;
        }
    }

    private List<Integer> makeIntegerArray(String str){
        List<Integer> list = new ArrayList<>();
        try {
            if (StringUtils.isEmpty(str)){
                return list;
            }
            list = gson.fromJson(str , new TypeToken<List<Integer>>(){}.getType());
            return list;
        }catch (Exception e){
            log.error("转换规则字符串失败 str = {}, exception = {}", str, e.getMessage());
            return list;
        }
    }

}

