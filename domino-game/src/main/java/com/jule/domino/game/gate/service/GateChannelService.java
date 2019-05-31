package com.jule.domino.game.gate.service;

import com.google.common.base.Strings;
import com.jule.domino.game.gate.pool.net.ChannelManageCenter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * channel处理
 *
 * @author
 * @since 2018/12/4 10:58
 */
@Slf4j
public class GateChannelService {

    public static final GateChannelService OBJ = new GateChannelService();

    /**
     * 重复登录
     * @param userId
     * @param gateSvr
     */
    public void handlerRepeatLoginChannel(String userId, String gateSvr){
        if (Strings.isNullOrEmpty(userId) || Strings.isNullOrEmpty(gateSvr)) {
            return;
        }
        if(gateSvr.equals(RegisteService.ADDRESS)){
            return;
        }
        ChannelManageCenter.getInstance().sub(userId);
    }

    /**
     * 大厅通知玩家下线
     * @param userId
     */
    public void handlerDestoryUserChannel(String userId){
        //连接
        ChannelHandlerContext ctx = UserService.getInstance().getCtx(userId);
        if (ctx == null){
            log.debug("玩家user = {}已经下线",userId);
            return;
        }

        //正常下线流程
//        UserService.getInstance().onUserBreak(ctx,false);
    }

}
