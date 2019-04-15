package com.jule.domino.game.room.service;

import com.google.common.base.Strings;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.holder.CommonConfigHolder;
import com.jule.domino.game.service.holder.RoomConfigHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * room操作类
 * @author
 * @since 2018/12/4 14:54
 */
@Slf4j
public class RoomOprService {

    public static final RoomOprService OBJ = new RoomOprService();

    //站起
    public void standupHandler(String gameId, String roomId, String tableId){
        if (Strings.isNullOrEmpty(gameId) || Strings.isNullOrEmpty(roomId) || Strings.isNullOrEmpty(tableId)) {
            return;
        }
        //TableService
        TableService.getInstance().addRoomCanJoinTable(gameId, roomId, tableId);
    }

    //GM刷新
    public void refreshConfigHandler(int channel){
        switch(channel){
            case 2:
                CommonConfigHolder.getInstance().init();
                break;
            case 3:
                RoomConfigHolder.getInstance().init();
                TableService.getInstance().init();
                break;
            default:
                break;
        }
    }

    //离桌
    public void leaveTableHandler(String gameId, String roomId, String tableId, String userId){
        if (Strings.isNullOrEmpty(gameId) || Strings.isNullOrEmpty(roomId) ||
                Strings.isNullOrEmpty(tableId) || Strings.isNullOrEmpty(userId)) {
            return;
        }
        AbstractTable tableInfo = TableService.getInstance().getTable(gameId, roomId, tableId);
        if (tableInfo != null) {
            tableInfo.returnLobby(userId);
        }
        TableService.getInstance().destroyTable(gameId, roomId, tableId);
    }

    //新桌子
    public void createTableHandler(String gameId, String roomId, String tableId){
        try {
            if (Strings.isNullOrEmpty(gameId) || Strings.isNullOrEmpty(roomId) ||
                    Strings.isNullOrEmpty(tableId)) {
                return;
            }
            TableService.getInstance().addExitTable(gameId, roomId, tableId);
        }catch (Exception ex){
            log.error("创建新桌失败 exception=",ex);
        }

    }

    //销毁桌子
    public void destoryTableHandler(String gameId, String roomId, String tableId){
        if (Strings.isNullOrEmpty(gameId) || Strings.isNullOrEmpty(roomId) ||
                Strings.isNullOrEmpty(tableId)) {
            return;
        }
        TableService.getInstance().directDestroyTable(gameId, roomId, tableId);
    }

    //离桌换桌成功
    public void changeTableHandler(String gameId, String roomId, String tableId, String userId, String hashcode){
        if (Strings.isNullOrEmpty(gameId) || Strings.isNullOrEmpty(roomId) ||
                Strings.isNullOrEmpty(tableId) || Strings.isNullOrEmpty(userId)
                || Strings.isNullOrEmpty(hashcode)) {
            return;
        }
        ChangeTableService.getInstance().ReqHandler(hashcode);
    }







}
