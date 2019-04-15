package com.jule.domino.game.network.protocol;

import com.jule.domino.game.gate.pool.net.ChannelManageCenter;
import com.jule.domino.game.play.AbstractTable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xujian on 2017/12/07.
 */
public abstract class ClientReq implements Cloneable, Runnable {
    private final static Logger logger = LoggerFactory.getLogger(ClientReq.class);

    protected ChannelHandlerContext ctx;
    protected int functionId;
    protected ClientHeader header;
    private AbstractTable table;
    protected String userId;

    public ClientReq(int functionId) {
        this.functionId = functionId;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public boolean readPayLoad(ByteBuf byteBuf) {
//        logger.debug("解析包");
        try {
            int gameId = byteBuf.readInt();
            int gameSvrId = byteBuf.readInt();
            boolean isAsync = byteBuf.readInt() == 1;
            int reqNum = byteBuf.readInt();
            long channelId = byteBuf.readLong();
            setUserId(channelId);
            this.header = new ClientHeader(functionId, gameId, gameSvrId, isAsync, reqNum, channelId);
            readPayLoadImpl(byteBuf);
        } catch (Exception e) {
            logger.error("解包错误", e);
            return false;
        }
        return true;
    }

    public AbstractTable getTable() {
        return table;
    }

    public void setTable(AbstractTable table) {
        this.table = table;
    }

    public abstract  void readPayLoadImpl(ByteBuf byteBuf) throws Exception;

    @Override
    public void run() {
        try {//获取玩家入座缓存
            processImpl();
        } catch (Exception e) {
            logger.error("业务处理异常", e);
        }
    }

    public abstract void processImpl() throws Exception;

    public ClientReq clone() {
        try {
            return (ClientReq) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public int getFunctionId() {
        return functionId;
    }

    private void setUserId(long channelId){
        try {
           String uid = ChannelManageCenter.getInstance().getSessionUID(channelId);
           if (!StringUtils.isEmpty(uid)){
               this.userId = uid;
               return;
           }
        }catch (Exception ex){
            logger.error("链接绑定ID",ex);
        }
    }

}
