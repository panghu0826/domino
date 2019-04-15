package com.jule.domino.game.api.impl;

import com.jule.core.jedis.StoredObjManager;
import com.jule.core.model.ServerState;
import com.jule.domino.game.api.BaseServlet;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.game.service.GameMaintentService;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


/**
 * 服务器启停
 */
@Path(value="/api/server")
public class ServerServlet extends BaseServlet {

    /**
     * 停服通知
     * @param start
     *              开始时间
     * @param end
     *              截止时间
     * @return
     */
    @POST
    @Path("/stopServer")
    @Produces({MediaType.APPLICATION_JSON})
    public int stopServer( @FormParam("start") String start, @FormParam("end") String end, @FormParam("details") String details){
        logger.info("stop server params , start = "+start+",end ="+end);
        if (StringUtils.isEmpty(start) || StringUtils.isEmpty(end)){
            return ERR;
        }

        //缓存一下时间
        ServerState server = new ServerState(Long.parseLong(start),Long.parseLong(end),details);
        boolean flag = StoredObjManager.set(GameConst.CACHE_SERVER_STATE, server);
        if (!flag){
            return ERR;
        }

        //发送停服通知
        //todo : 添加通知协议
        return SUC;
    }

    /**
     * 请求开服
     * @return
     */
    @POST
    @Path("/openServer")
    @Produces({MediaType.APPLICATION_JSON})
    public int openServer(){
        logger.info(" apply open server ");

        //是否存在这个键值对
        ServerState server = StoredObjManager.get(ServerState.class,GameConst.CACHE_SERVER_STATE);
        if (server == null){
            return SUC;
        }

        //移除维护标识
        boolean flag = StoredObjManager.deleteExistsObj(GameConst.CACHE_SERVER_STATE);
        return flag ? SUC : ERR;
    }


    @GET
    @Path("/gameStart")
    @Produces({MediaType.APPLICATION_JSON})
    public int gameStart(){
        logger.info("关闭维护墙");
        GameMaintentService.OBJ.turnOff();
        return SUC ;
    }

    @GET
    @Path("/gameStop")
    @Produces({MediaType.APPLICATION_JSON})
    public int gameStop(){
        logger.info("打开维护墙");
        GameMaintentService.OBJ.turnOn();
        return SUC ;
    }

}
