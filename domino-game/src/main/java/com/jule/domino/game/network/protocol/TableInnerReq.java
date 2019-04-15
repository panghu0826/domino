package com.jule.domino.game.network.protocol;

import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.TableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 匿名桌子内自发送消息
 * Created by xujian on 2017/12/07.
 */
public abstract class TableInnerReq implements Cloneable, Runnable {
    private final static Logger logger = LoggerFactory.getLogger(TableInnerReq.class);
    private final String gameId;
    protected final String roomId;
    protected final String tableId;

    public TableInnerReq(String gameId,String roomId, String tableId) {
        this.gameId = gameId;
        this.roomId = roomId;
        this.tableId = tableId;
    }

    public boolean sendToSelf() {
        AbstractTable table = TableService.getInstance().getTable(gameId,roomId, tableId);
        if (table != null) {
            table.getFifoRunnableQueue().execute(this);
        }
        return true;
    }

    public void deleteToSelf(){
        AbstractTable table = TableService.getInstance().getTable(gameId,roomId, tableId);
        if (table != null) {
            table.getFifoRunnableQueue().remove(this);
        }
    }
    @Override
    public void run() {
        try {
            processImpl();
        } catch (Exception e) {
            logger.error("桌子->" + tableId + "倒计时业务处理异常", e);
        }
    }

    public abstract void processImpl() throws Exception;

    public String getRoomId() {
        return roomId;
    }

    public String getTableId() {
        return tableId;
    }
}
