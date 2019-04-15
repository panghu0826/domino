//package com.boot.controller;
//
//import ch.qos.logback.core.db.dialect.DBUtil;
//import com.boot.cache.CommMem;
//import com.boot.config.AppConfig;
//import com.boot.entity.RealTimeQueryModel;
//import com.boot.entity.RoomConfigModel;
//import com.boot.entity.RoomTableRelationModel;
//import com.boot.entity.User;
//import com.boot.enums.RedisConst;
//import com.boot.service.RedisService;
//import com.boot.service.RoomConfigService;
//import com.boot.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Set;
//
///**
// * 测试接口
// * @author
// * @since 2018/7/18 18:26
// */
//
//@RestController
//@RequestMapping(value = "/api/gm")
//public class LoginApiController extends AbstractController{
//
//    @Autowired
//    private AppConfig appConfig;
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private RedisService redisService;
//
//    @Autowired
//    private RoomConfigService roomConfigService;
//
//    @GetMapping(value = "dotest/{id}")
//    public String dotest(@PathVariable("id") String id) {
//        test();
//        return appConfig.getAppid();
//    }
//
//    @RequestMapping(value = "login", method={RequestMethod.GET, RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public String Login(@RequestParam("id") int id) {
//        User user = userService.getUser(id);
//        if (user == null){
//            log.info("user info = {}",user);
//            return "error";
//        }
//        return user.getName();
//    }
//
//    private void test(){
//        try {
//            String key = "key";
//            redisService.set(key,"value");
//            String value = redisService.getValue(key);
//            log.info("redis test  --> {}<==>{}",key, value);
//        }catch (Exception e){
//        }
//
//    }
//
//    @RequestMapping(value = "realTimeQuery", method={RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public List<RealTimeQueryModel> realTimeQuery(@RequestParam("gameId") String gameId) {
//        log.info("实时查询房间使用情况"+gameId);
//        List<RealTimeQueryModel> list = new ArrayList<>();//记录所有需要返回的信息,以逗号分隔
//        List<RoomConfigModel> rcm = roomConfigService.selectAllRoom();
//        List<String> keys = new ArrayList<>();
//        if ("-1".equals(gameId)) {
//            List<String> playType = Arrays.asList("91001001", "91001002", "91001003", "91001004");
//            playType.forEach(p -> {
//                rcm.forEach(r -> {
//                    keys.add(p + r.getRoomId());
//                });
//            });
//        } else {
//            rcm.forEach(r -> {
//                keys.add(gameId + r.getRoomId());
//            });
//        }
//        keys.forEach( key -> {
//            CommMem.OBJ.getCache().getMemList(key , )
//            List<RoomTableRelationModel> rttm = StoredObjManager.getStoredObject(RoomTableRelationModel.class, RedisConst.TABLE_INSTANCE.getProfix() + key);
//            rttm.forEach(r ->{
//                Set<String> users = StoredObjManager.hkeys(RedisConst.TABLE_USERS.getProfix() + r.getGameId() + r.getRoomId() + r.getTableId());
//                if(users != null) {
//                    int palyer = 0;
//                    int robot = 0;
//                    for(String u : users){
//                        String userId = u.substring(RedisConst.TABLE_USERS.getField().length());
//                        User user = DBUtil.selectByPrimaryKey(userId);
//                        if(!"robot".equals(user.getChannel_id())){
//                            palyer++;
//                        }else{
//                            robot++;
//                        }
//                    }
//                    for(RoomConfigModel model : rcm){
//                        if(model.getRoomId().equals(r.getRoomId())){
//                            RealTimeQueryModel rtqm = new RealTimeQueryModel();
//                            rtqm.setGameId(r.getGameId());
//                            rtqm.setRoomId(r.getRoomId());
//                            rtqm.setTableId(r.getTableId());
//                            rtqm.setPlayers(palyer);
//                            rtqm.setRobots(robot);
//                            rtqm.setAnte(model.getAnte());
//                            list.add(rtqm);
//                        }
//                    }
//                }
//            });
//        });
//        RealTimeQueryModel rtqm = new RealTimeQueryModel();
//        rtqm.setRoomId(String.valueOf(10));
//        rtqm.setTableId(String.valueOf(10001));
//        rtqm.setGameId(String.valueOf(91001001));
//        rtqm.setPlayers(2);
//        rtqm.setRobots(3);
//        rtqm.setAnte(10);
//        list.add(rtqm);
//        RealTimeQueryModel rtqm1 = new RealTimeQueryModel();
//        rtqm1.setRoomId(String.valueOf(11));
//        rtqm1.setTableId(String.valueOf(10002));
//        rtqm1.setGameId(String.valueOf(91001002));
//        rtqm1.setPlayers(2);
//        rtqm1.setRobots(3);
//        rtqm1.setAnte(20);
//        list.add(rtqm1);
//        RealTimeQueryModel rtqm2 = new RealTimeQueryModel();
//        rtqm2.setRoomId(String.valueOf(12));
//        rtqm2.setTableId(String.valueOf(10003));
//        rtqm2.setGameId(String.valueOf(91001003));
//        rtqm2.setPlayers(2);
//        rtqm2.setRobots(3);
//        rtqm2.setAnte(30);
//        list.add(rtqm2);
//        RealTimeQueryModel rtqm3 = new RealTimeQueryModel();
//        rtqm3.setRoomId(String.valueOf(14));
//        rtqm3.setTableId(String.valueOf(10004));
//        rtqm3.setGameId(String.valueOf(91001004));
//        rtqm3.setPlayers(2);
//        rtqm3.setRobots(3);
//        rtqm3.setAnte(40);
//        list.add(rtqm3);
//        RealTimeQueryModel rtqm4 = new RealTimeQueryModel();
//        rtqm4.setGameId(String.valueOf(91001005));
//        rtqm4.setRoomId(String.valueOf(50));
//        rtqm4.setTableId(String.valueOf(10005));
//        rtqm4.setPlayers(2);
//        rtqm4.setRobots(3);
//        rtqm4.setAnte(50);
//        list.add(rtqm4);
//        return list;
//    }
//}
