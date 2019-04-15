package com.jule.domino.auth.network.actions;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.google.protobuf.MessageLite;
import com.jule.domino.auth.loginprocess.ILoginProcess;
import com.jule.domino.auth.loginprocess.LoginProcessManager;
import com.jule.domino.auth.model.BaseAction;
import com.jule.domino.auth.model.Response;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xujian 2018-01-09
 * 登陆请求
 */
public class LoginAction extends BaseAction {
    private static final Logger logger = LoggerFactory.getLogger(LoginAction.class);

    @Override
    public void handlePost(ChannelHandlerContext ctx, byte[] payload, boolean iskeepAlive) throws Exception {

        JoloAuth.JoloCommon_LoginReq req = JoloAuth.JoloCommon_LoginReq.parseFrom(payload);
        logger.info("received LoginAction request ->" + req.toString());
        ILoginProcess process = LoginProcessManager.getInstance().getLoginProcess(req.getChannelId());
        logger.debug("--------------------------:"+process);
        if (process == null) {
            ctx.close();
            return;
        }

        MessageLite ret = process.process(req);
        ctx.writeAndFlush(Response.build(ret.toByteArray(), iskeepAlive)).addListener(ChannelFutureListener.CLOSE);
    }

}
