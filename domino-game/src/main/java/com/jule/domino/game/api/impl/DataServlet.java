package com.jule.domino.game.api.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jule.core.utils.GsonUtil;
import com.jule.domino.game.api.BaseServlet;
import com.jule.domino.game.api.entity.CommonConfigEntity;
import com.jule.domino.game.api.entity.RoomConfigEntity;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.CommonConfigModel;
import com.jule.domino.game.dao.bean.RoomConfigModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path(value = "/api/data")
public class DataServlet extends BaseServlet {

    @GET
    @Path("/selectAllPaiGowRoom")
    @Produces({MediaType.APPLICATION_JSON})
    public String selectAllRoomConfig() {
        CodeObject codeObject = new CodeObject();
        try {
            List<RoomConfigModel> list = DBUtil.selectAllRoom();
            List<RoomConfigEntity> array = new ArrayList<>();
            list.forEach(e ->
                    array.add(new RoomConfigEntity(e))
            );
            codeObject.setCode(0);
            codeObject.setMsg("");
            codeObject.setResult(JSONArray.toJSON(array).toString());
            Object obj = JSONObject.toJSON(codeObject);
            return obj.toString();
        } catch (Exception e) {
            codeObject.setCode(5000);
            codeObject.setMsg("服务器内部错误");
            codeObject.setResult("");
            Object obj = JSONObject.toJSON(codeObject);
            logger.error("查询房间配置出错：{}", e.getMessage());
            return obj.toString();
        }
    }

    @GET
    @Path("/updatePaiGowRoom")
    @Produces({MediaType.APPLICATION_JSON})
    public String updateRoomConfig(@QueryParam("id") int id,
                                   @QueryParam("roomId") String roomId,
                                   @QueryParam("minJoinTable") Long minJoinTable,
                                   @QueryParam("ante") Long ante,
                                   @QueryParam("serviceCharge") double serviceCharge,
                                   @QueryParam("doubleRoles") String doubleRoles,
                                   @QueryParam("onOff") int onOff) {
        CodeObject codeObject = new CodeObject();
        try {
            if (minJoinTable <= 0 || ante <= 0 || serviceCharge < 0 || doubleRoles == null || onOff < 0) {
                codeObject.setCode(5001);
                codeObject.setMsg("非法参数错误");
                codeObject.setResult("");
                Object obj = JSONObject.toJSON(codeObject);
                return obj.toString();
            }
            RoomConfigModel roomConfigModel = new RoomConfigModel();
            roomConfigModel.setId(id);
            //roomConfigModel.setRoomId(roomId);
            roomConfigModel.setMinScore4JoinTable(minJoinTable / 100);
            roomConfigModel.setAnte(ante / 100);
            roomConfigModel.setServiceChargeRate(serviceCharge / 10000);
            roomConfigModel.setDoubleRoles("[" + doubleRoles + "]");
            roomConfigModel.setOnOff(onOff);
            int in = DBUtil.updateRoom(roomConfigModel);
            if (in == 1) {
                RoomConfigEntity roomConfigEntity = new RoomConfigEntity(DBUtil.selectRoom(id));
                codeObject.setCode(0);
                codeObject.setMsg("");
                codeObject.setResult(JSONObject.toJSON(roomConfigEntity).toString());
            } else {
                codeObject.setCode(5001);
                codeObject.setMsg("参数错误");
                codeObject.setResult("");
            }
            Object obj = JSONObject.toJSON(codeObject);
            return obj.toString();
        } catch (Exception e) {
            codeObject.setCode(5000);
            codeObject.setMsg("服务器内部错误");
            codeObject.setResult("");
            Object obj = JSONObject.toJSON(codeObject);
            logger.error("修改房间配置出错：{}", e.getMessage());
            return obj.toString();
        }
    }

    @GET
    @Path("/selectAllPaiGowCommon")
    @Produces({MediaType.APPLICATION_JSON})
    public String selectAllCommonConfig() {
        CodeObject codeObject = new CodeObject();
        try {
            List<CommonConfigModel> list = DBUtil.selectAllCommon();
            List<CommonConfigEntity> array = new ArrayList<>();
            list.forEach(e ->
                    array.add(new CommonConfigEntity(e))
            );
            codeObject.setCode(0);
            codeObject.setMsg("");
            codeObject.setResult(JSONArray.toJSON(array).toString());
            Object obj = JSONObject.toJSON(codeObject);
            return obj.toString();
        } catch (Exception e) {
            codeObject.setCode(5000);
            codeObject.setMsg("服务器内部错误");
            codeObject.setResult("");
            Object obj = JSONObject.toJSON(codeObject);
            logger.error("查询通用配置出错：{}", e.getMessage());
            return obj.toString();
        }
    }

    @GET
    @Path("/updatePaiGowCommon")
    @Produces({MediaType.APPLICATION_JSON})
    public String updateCommonConfig(@QueryParam("id") int id,
                                     @QueryParam("gameStartCD") int gameStartCD,
                                     @QueryParam("betCD") int betCD,
                                     @QueryParam("openCardsCD") int openCardsCD,
                                     @QueryParam("settleCD") int settleCD) {
        CodeObject codeObject = new CodeObject();
        try {
            if (gameStartCD <= 0 || betCD <= 0 || openCardsCD <= 0 || settleCD <= 0) {
                codeObject.setCode(5001);
                codeObject.setMsg("非法参数错误");
                codeObject.setResult("");
                Object obj = JSONObject.toJSON(codeObject);
                return obj.toString();
            }
            CommonConfigModel configModel = DBUtil.selectCommon(id);
            configModel.setGameStartCountDownSec(gameStartCD);
            configModel.setBetCountDownSec(betCD);
            configModel.setOpenCardsCD(openCardsCD);
            configModel.setSettleCD(settleCD);
            int in = DBUtil.updateCommon(configModel);
            if (in == 1) {
                CommonConfigEntity common = new CommonConfigEntity(DBUtil.selectCommon(id));
                codeObject.setCode(0);
                codeObject.setMsg("");
                codeObject.setResult(JSONObject.toJSON(common).toString());
            } else {
                codeObject.setCode(5001);
                codeObject.setMsg("参数错误");
                codeObject.setResult("");
            }
            Object obj = JSONObject.toJSON(codeObject);
            return obj.toString();
        } catch (Exception e) {
            codeObject.setCode(5000);
            codeObject.setMsg("服务器内部错误");
            codeObject.setResult("");
            Object obj = JSONObject.toJSON(codeObject);
            logger.error("修改通用配置出错：{}", e.getMessage());
            return obj.toString();
        }
    }

    @GET
    @Path("/updateOnliner")
    @Produces({MediaType.APPLICATION_JSON})
    public String updateOnliner(@QueryParam("x") String x,
                                @QueryParam("y") String y,
                                @QueryParam("a") String a,
                                @QueryParam("b") String b,
                                @QueryParam("c") String c) {
        CodeObject codeObject = new CodeObject();
        try {
            if (StringUtils.isEmpty(x) || StringUtils.isEmpty(y) || StringUtils.isEmpty(a) || StringUtils.isEmpty(b) ||StringUtils.isEmpty(c)){
                codeObject.setCode(5001);
                codeObject.setMsg("非法参数错误");
                codeObject.setResult("");
                Object obj = JSONObject.toJSON(codeObject);
                return obj.toString();
            }

            //构建参数
            List<String> list = Arrays.asList(x,y,a,b,c);
            String roles = GsonUtil.getGson().toJson(list);

            //更新数据库
            int count = DBUtil.updateOnlineRoles(roles);
            if (count >= 1) {
                codeObject.setCode(0);
                codeObject.setMsg("success");
                codeObject.setResult("");
            } else {
                codeObject.setCode(5001);
                codeObject.setMsg("参数错误");
                codeObject.setResult("");
            }
            Object obj = JSONObject.toJSON(codeObject);
            return obj.toString();
        } catch (Exception e) {
            codeObject.setCode(5000);
            codeObject.setMsg("服务器内部错误");
            codeObject.setResult("");
            Object obj = JSONObject.toJSON(codeObject);
            logger.error("修改通用配置出错：{}", e.getMessage());
            return obj.toString();
        }
    }
}


@Setter
@Getter
@ToString
class CodeObject {
    private int code;
    private String msg;
    private String result;
}
