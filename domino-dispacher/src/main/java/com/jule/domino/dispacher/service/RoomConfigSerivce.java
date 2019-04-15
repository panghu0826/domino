package com.jule.domino.dispacher.service;

import JoloProtobuf.RoomSvr.JoloRoom;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jule.domino.dispacher.dao.DBUtil;
import com.jule.domino.dispacher.dao.bean.RoomConfigModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 房间配置
 *
 * @author
 * @since 2018/9/11 16:27
 */
@Slf4j
public class RoomConfigSerivce {

    //单例
    public static final RoomConfigSerivce OBJ = new RoomConfigSerivce();

    public static Map<String, JoloRoom.JoloRoom_RoomInfo> _rooms = new HashMap<>();

    private static final Gson gson = new GsonBuilder().serializeNulls().create();

    public RoomConfigSerivce() {
        //启动定时加载
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> loadData(),
                1,
                1,
                TimeUnit.MINUTES);
    }

    /**
     * 初始化
     */
    public void init() {
        loadData();
    }

    /**
     * 加载数据
     */
    public void loadData() {
        log.info("载入房间配置到内存");
        List<RoomConfigModel> configs = DBUtil.getRoomConfigFromDb();
        if (configs == null) {
            log.error("无房间配置");
            return;
        }

        configs.forEach(e -> {
            _rooms.put(e.getRoomId(), JoloRoom.JoloRoom_RoomInfo.newBuilder()
                    .setRoomId(e.getRoomId())
                    .setRoomName("").setRoomDesc("")
                    .setMinJoin(e.getMinScore4JoinTable())
                    .setAnte(e.getAnte())
                    .addAllDoubleRoles(makeArray(e.getDoubleRoles()))
                    .build());
        });
    }

    public Collection<JoloRoom.JoloRoom_RoomInfo> getRoomConfigs() {
        if (_rooms.size() == 0) {
            log.error("无房间配置");
        }
        return _rooms.values();
    }

    private List<Integer> makeArray(String str) {
        List<Integer> list = new ArrayList<>();
        try {
            if (StringUtils.isEmpty(str)) {
                return list;
            }
            list = gson.fromJson(str, new TypeToken<List<Integer>>() {
            }.getType());
            return list;
        } catch (Exception e) {
            log.error("转换规则字符串失败 str = {}, exception = {}", str, e.getMessage());
            return list;
        }
    }

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(4);
        String str = gson.toJson(list);
        System.out.println(str);
    }
}
