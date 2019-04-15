package com.jule.domino.game.room;

import com.jule.domino.game.room.network.IOServer;
import com.jule.domino.game.room.service.RoomConfigSerivce;
import com.jule.domino.game.room.service.TableService;
import lombok.extern.slf4j.Slf4j;

/**
 *  room服務
 * @author
 * @since 2018/12/4 11:47
 */
@Slf4j
public class RoomServer {

    public static final RoomServer OBJ = new RoomServer();

    public void start(){
        try {
            log.info("room 服务启动");

            TableService.getInstance().init();

            RoomConfigSerivce.OBJ.init();

            IOServer.connect();

        }catch (Exception ex){
            throw new Error("room 服务启动异常",ex);
        }
    }

}
