package com.jule.domino.game.api.impl;

import com.alibaba.fastjson.JSONObject;
import com.jule.core.common.log.LoggerUtils;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.game.api.BaseServlet;
import com.jule.domino.game.api.model.DetailedInformationModel;
import com.jule.domino.game.api.model.RealTimeQueryModel;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.RoomConfigModel;
import com.jule.domino.game.room.service.RoomOprService;
import com.jule.domino.game.service.LogService;
import com.jule.domino.game.service.ProductionService;
import com.jule.domino.game.service.TableService;
import com.jule.domino.game.service.holder.CommonConfigHolder;
import com.jule.domino.game.service.holder.RoomConfigHolder;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisChannel;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.RoomTableRelationModel;
import com.jule.domino.log.service.LogReasons;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

@Path(value = "/api/gm")
public class GmServlet extends BaseServlet {

    @GET
    @Path("/statue")
    @Produces({MediaType.APPLICATION_JSON})
    public boolean statue(){
        return true;
    }

    @POST
    @Path("/changeGold")
    @Produces({MediaType.APPLICATION_JSON})
    public int changeGold(@FormParam("uid") String uid, @FormParam("changeValue") String changeValue) {
        logger.info("uid ={},changeValue = {}", uid, changeValue);
        if (StringUtils.isEmpty(uid) || StringUtils.isEmpty(changeValue)) {
            return ERR;
        }
        User user = DBUtil.selectByPrimaryKey(uid);
        if (user == null) {
            logger.error("user == null");
            return ERR;
        }
        double org = user.getMoney();
        int change = Integer.valueOf(changeValue);

        //更新数据
        user.setMoney(org + change);
        DBUtil.updateByPrimaryKey(user);

        //刷新缓存
        logger.info("11save userInfo->" + user.toString());
        StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);

        //发送日志
        if (change >= 0) {
            LogService.OBJ.sendMoneyLog(user, org, user.getMoney(), change, LogReasons.CommonLogReason.GM);
        } else if (change < 0) {
            LogService.OBJ.sendMoneyLog(user, org, user.getMoney(), change, LogReasons.CommonLogReason.GM_MIUNES);
        }
        return SUC;
    }

    /**
     * 刷新游戏配置
     *
     * @param configtype 配置类型
     *                   1-礼物
     *                   2-房间通用配置
     *                   3-房间配置
     *                   4-签到奖励配置
     */
    @POST
    @Path("/refreshConfig")
    @Produces({MediaType.APPLICATION_JSON})
    public int refreshConfig(@FormParam("configtype") int configtype) {
        logger.info("开始重载内存 ，操作类型= " + configtype);
        boolean result = false;
        switch (configtype) {
            case 1:
                result = ProductionService.getInstance().discoverData();
                TableService.getInstance().tablePropertyChange();
                logger.info("重载礼物配置内存完成");
                break;
            case 2:
                result = CommonConfigHolder.getInstance().init();
                logger.info("julo-game 重载房间通用配置内存完成");

                logger.info("通知julo-room重载");
                TableService.getInstance().tablePropertyChange();
                RoomOprService.OBJ.refreshConfigHandler(configtype);
                break;
            case 3:
                result = RoomConfigHolder.getInstance().init();
                logger.info("julo-game 重载房间配置内存完成");

                logger.info("通知julo-room重载");
                TableService.getInstance().tablePropertyChange();
                RoomOprService.OBJ.refreshConfigHandler(configtype);
                break;
            case 4:
                // TODO: 2018/4/4 签到代码还未合并
                logger.info("重载房间配置内存完成");
                break;
            default:
                logger.error("未知类型-->" + configtype);
                break;
        }
        return result ? SUC : ERR;
    }

    @POST
    @Path("/addMail")
    @Produces({MediaType.APPLICATION_JSON})
    public int addNewMail(@FormParam("mailId") int mailConfigId, @FormParam("type") int type) {
        JSONObject js = new JSONObject();
        js.put("mailConfigId", mailConfigId);
        js.put("type", type);
        StoredObjManager.publish(js.toString(), RedisChannel.MAIL_NEW_CONFIG.getChannelName());
        LoggerUtils.mailLog.info("addNewMail mailId:{}，type:{}", mailConfigId, type);
        return 1;
    }

    @POST
    @Path("/onlineNumber")
    @Produces({MediaType.APPLICATION_JSON})
    public int onlineNumber(@FormParam("name") String name) {
        logger.info("name=" + name);
        int number = 0;
        List<String> list = null;
        if ("game".equals(name)) {
            list = StoredObjManager.hvals(RedisConst.GAME_SVR_TOTAL_USER.getProfix());
        } else {
            list = StoredObjManager.hvals(RedisConst.DISPACHER_SVR_TOTAL_USER.getProfix());
        }
        if (list == null) {
            return 0;
        }

        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String val = iterator.next();
            number += Integer.parseInt(val);
        }
//        number = 12;
        logger.debug(name + "===当前的人数：" + number);
        return number;
    }

    @POST
    @Path("/onlineDispacher")
    @Produces({MediaType.APPLICATION_JSON})
    public int onlineDispacher() {
        logger.info("onlineDispacher");
        int number = 0;
        List<String> list = StoredObjManager.hvals(RedisConst.DISPACHER_SVR_TOTAL_USER.getProfix());
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String val = iterator.next();
            number += Integer.parseInt(val);
        }
        return number;
    }

    @POST
    @Path("/adConfigRefresh")
    @Produces({MediaType.APPLICATION_JSON})
    public int adConfigRefresh() {
        logger.info("刷新广告配置");
        StoredObjManager.deleteExistsObj(GameConst.CACHE_AD_CONFIGS);
        return SUC;
    }

    @POST
    @Path("/realTimeQuery")
    @Produces({MediaType.APPLICATION_JSON})
    public List<RealTimeQueryModel> realTimeQuery(@FormParam("gameId") String gameId) {
        logger.info("实时查询房间使用情况"+gameId);
        List<RealTimeQueryModel> list = new ArrayList<>();//记录所有需要返回的信息,以逗号分隔
        List<RoomConfigModel> rcm = DBUtil.selectAllRoom();
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
                Set<String> users = StoredObjManager.hkeys(RedisConst.TABLE_USERS.getProfix() + r.getGameId() + r.getRoomId() + r.getTableId());
                if(users != null) {
                    int palyer = 0;
                    int robot = 0;
                    for(String u : users){
                        String userId = u.substring(RedisConst.TABLE_USERS.getField().length());
                        User user = DBUtil.selectByPrimaryKey(userId);
                        if(!"robot".equals(user.getChannel_id())){
                            palyer++;
                        }else{
                            robot++;
                        }
                    }
                    for(RoomConfigModel model : rcm){
                        if(model.getRoomId().equals(r.getRoomId())){
                            RealTimeQueryModel rtqm = new RealTimeQueryModel();
                            rtqm.setGameId(r.getGameId());
                            rtqm.setRoomId(r.getRoomId());
                            rtqm.setTableId(r.getTableId());
                            rtqm.setPlayers(palyer);
                            rtqm.setRobots(robot);
                            rtqm.setAnte(model.getAnte());
                            list.add(rtqm);
                        }
                    }
                }
            });
        });
        RealTimeQueryModel rtqm = new RealTimeQueryModel();
        rtqm.setRoomId(String.valueOf(10));
        rtqm.setTableId(String.valueOf(10001));
        rtqm.setGameId(String.valueOf(91001001));
        rtqm.setPlayers(2);
        rtqm.setRobots(3);
        rtqm.setAnte(10);
        list.add(rtqm);
        RealTimeQueryModel rtqm1 = new RealTimeQueryModel();
        rtqm1.setRoomId(String.valueOf(11));
        rtqm1.setTableId(String.valueOf(10002));
        rtqm1.setGameId(String.valueOf(91001002));
        rtqm1.setPlayers(2);
        rtqm1.setRobots(3);
        rtqm1.setAnte(20);
        list.add(rtqm1);
        RealTimeQueryModel rtqm2 = new RealTimeQueryModel();
        rtqm2.setRoomId(String.valueOf(12));
        rtqm2.setTableId(String.valueOf(10003));
        rtqm2.setGameId(String.valueOf(91001003));
        rtqm2.setPlayers(2);
        rtqm2.setRobots(3);
        rtqm2.setAnte(30);
        list.add(rtqm2);
        RealTimeQueryModel rtqm3 = new RealTimeQueryModel();
        rtqm3.setRoomId(String.valueOf(14));
        rtqm3.setTableId(String.valueOf(10004));
        rtqm3.setGameId(String.valueOf(91001004));
        rtqm3.setPlayers(2);
        rtqm3.setRobots(3);
        rtqm3.setAnte(40);
        list.add(rtqm3);
        RealTimeQueryModel rtqm4 = new RealTimeQueryModel();
        rtqm4.setGameId(String.valueOf(91001005));
        rtqm4.setRoomId(String.valueOf(50));
        rtqm4.setTableId(String.valueOf(10005));
        rtqm4.setPlayers(2);
        rtqm4.setRobots(3);
        rtqm4.setAnte(50);
        list.add(rtqm4);
        return list;
    }

    @POST
    @Path("/detailed")
    @Produces({MediaType.APPLICATION_JSON})
    public List<DetailedInformationModel> detailed(@FormParam("args") String args) {
        List<DetailedInformationModel> list = new ArrayList<>();
        Set<String> users = StoredObjManager.hkeys(RedisConst.TABLE_USERS.getProfix() + args);
        if(users != null) {
            for (String u : users) {
                String userId = u.substring(RedisConst.TABLE_USERS.getField().length());
                User user = DBUtil.selectByPrimaryKey(userId);
                DetailedInformationModel dim = new DetailedInformationModel();
                dim.setPlayerId(user.getId());
                dim.setNickName(user.getNick_name());
                dim.setRegistrationTime(user.getRegistration_time());
                dim.setMoney(user.getMoney());
                dim.setIsPlayer("robot".equals(user.getChannel_id()) ? 0 : 1);
                list.add(dim);
            }
        }
        return  list;
    }
}
