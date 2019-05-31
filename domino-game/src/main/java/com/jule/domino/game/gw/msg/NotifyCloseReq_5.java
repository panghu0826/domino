package com.jule.domino.game.gw.msg;

import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.GameRoomTableSeatRelationModel;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.gate.service.GateChannelService;
import com.jule.domino.game.gw.netty.AbstractGwcHander;
import com.jule.domino.game.gw.netty.GwcMsg;
import com.jule.domino.game.gw.netty.GwcMsgID;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 通知服务器玩家断开
 */
@Slf4j
public class NotifyCloseReq_5 extends AbstractGwcHander {

    private static final ExecutorService fixpool = Executors.newFixedThreadPool(5);

    public NotifyCloseReq_5() {
        super(GwcMsgID.NotifyClose);
    }

    @Override
    public void process(ChannelHandlerContext ctx, GwcMsg msg) throws Exception {
//        Gwc.NotifyClose req = Gwc.NotifyClose.parseFrom(msg.getBody());
        log.debug("收到玩家断开通知}");
//
//        //平台ID
//        String openId = req.getUid();
//
//        fixpool.submit(() -> closeCtx(openId) );
    }

    private void closeCtx (String openId){
        try {
            User user = DBUtil.selectByOpenId(openId);
            if (user == null){
                log.error("游戏内没有该玩家 openid={}",openId);
                return;
            }

            //首先查看自己有没有在游戏内
            GameRoomTableSeatRelationModel inGame = StoredObjManager.getStoredObjInMap(
                    GameRoomTableSeatRelationModel.class,
                    RedisConst.USER_TABLE_SEAT.getProfix(),
                    RedisConst.USER_TABLE_SEAT.getField() + user.getId());
            if (inGame != null){
                log.error("玩家在游戏中,不断开 openId={},userId={} ",openId,user.getId());
                return;
            }

            //通知gate断开
//            GateChannelService.OBJ.handlerDestoryUserChannel(user.getId());
        }catch (Exception ex){
            log.error("玩家下线失败openid={}",openId);
        }
    }
}
