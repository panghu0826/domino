package com.boot.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.boot.cache.StoredObjManager;
import com.boot.config.AppConfig;
import com.boot.entity.*;
import com.boot.enums.RedisConst;
import com.boot.service.RoomConfigService;
import com.boot.service.UserService;
import com.jule.domino.base.service.ItemServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping(value = "/api/gm")
public class GmController extends AbstractController{
    @Autowired
    private RoomConfigService roomConfigService;
    @Autowired
    private UserService userService;
    @Autowired
    private AppConfig appConfig;

    @RequestMapping(value = "realTimeQuery", method={RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<RealTimeQueryModel> realTimeQuery(@RequestParam("gameId") String gameId,@RequestParam("id") String id) {
        log.info("实时查询房间使用情况"+gameId);
        List<RealTimeQueryModel> list = new ArrayList<>();//记录所有需要返回的信息,以逗号分隔
        List<RoomConfigModel> rcm = roomConfigService.selectAllRoom();
        if(id != null && id != ""){
            String user = StoredObjManager.hget(RedisConst.USER_TABLE_SEAT.getProfix(), RedisConst.USER_TABLE_SEAT.getField() + id);
            JSONObject json = JSON.parseObject(user);
            String playTypeId = json.getString("gameId");
            String roomId = json.getString("roomId");
            String tableId = json.getString("tableId");
            addListObject(list,playTypeId,roomId,tableId);
            return list;
        }
        List<String> keys = new ArrayList<>();
        if ("-1".equals(gameId)) {
            List<String> playType = Arrays.asList("91001001", "91001002", "91001003", "91001004");
            playType.forEach(p -> {
                rcm.forEach(r -> {
                    keys.add(p + r.getRoomId());
                });
            });
        } else {
            rcm.forEach(r -> {
                keys.add(gameId + r.getRoomId());
            });
        }
        keys.forEach( key -> {
            List<RoomTableRelationModel> rttm = StoredObjManager.getStoredObject(RoomTableRelationModel.class,RedisConst.TABLE_INSTANCE.getProfix() + key);
            rttm.forEach(r ->{
                addListObject(list,r.getGameId(),r.getRoomId(),r.getTableId());
            });
        });
        return list;
    }

    private void addListObject(List<RealTimeQueryModel> list,String gameId,String roomId,String tableId){
        List<RoomConfigModel> rcm = roomConfigService.selectAllRoom();
        String tables = RedisConst.TABLE_USERS.getProfix() + gameId + roomId + tableId;
        Set<String> users = StoredObjManager.hkeys(tables);
        if(users != null) {
            int totalPalyers = 0;
            int totalRobots = 0;
            int inSeatPlayers = 0;
            int inSeatRobors = 0;
            for(String u : users){
                String userId = u.substring(RedisConst.TABLE_USERS.getField().length());
                User user = userService.getUser(userId);
                String in = StoredObjManager.hget(tables,u);
                if(!"robot".equals(user.getChannel_id())){
                    if("1".equals(in)){
                        inSeatPlayers++;
                    }
                    totalPalyers++;
                }else{
                    if("1".equals(in)){
                        inSeatRobors++;
                    }
                    totalRobots++;
                }
            }
            for(RoomConfigModel model : rcm){
                if((inSeatPlayers > 0 || inSeatRobors > 0) && model.getRoomId().equals(roomId)){
                    RealTimeQueryModel rtqm = new RealTimeQueryModel();
                    rtqm.setGameId(gameId);
                    rtqm.setRoomId(roomId);
                    rtqm.setTableId(tableId);
                    rtqm.setTotalPlayers(totalPalyers);
                    rtqm.setTotalRobots(totalRobots);
                    rtqm.setInSeatPlayers(inSeatPlayers);
                    rtqm.setInSeatRobots(inSeatRobors);
                    rtqm.setAnte(model.getAnte());
                    list.add(rtqm);
                }
            }
        }
    }

    @RequestMapping(value = "detailed", method={RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<DetailedInformationModel> detailed(@RequestParam("args") String args) {
        log.debug("走到这里了！！！");
        List<DetailedInformationModel> list = new ArrayList<>();
        String tables = RedisConst.TABLE_USERS.getProfix() + args;
        Set<String> users = StoredObjManager.hkeys(tables);
        if(users != null) {
            for (String u : users) {
                String userId = u.substring(RedisConst.TABLE_USERS.getField().length());
                User user = userService.getUser(userId);
                String in = StoredObjManager.hget(tables,u);
                DetailedInformationModel dim = new DetailedInformationModel();
                dim.setPlayerId(user.getId());
                dim.setNickName(user.getNick_name());
                dim.setRegistrationTime(user.getRegistration_time());
                dim.setMoney(user.getMoney());
                dim.setIsPlayer("robot".equals(user.getChannel_id()) ? "否" : "是");
                dim.setIsSeat("1".equals(in) ? "是" : "否");
                list.add(dim);
            }
        }
        return  list;
    }

    @RequestMapping(value = "getMap", method={RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<Integer,String> getMap(){
        return ItemServer.OBJ.getIdNameMap(appConfig.getGameId());
    }

}
