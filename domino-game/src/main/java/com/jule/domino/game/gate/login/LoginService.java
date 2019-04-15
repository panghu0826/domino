package com.jule.domino.game.gate.login;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.domino.game.gate.network.protocol.Req;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录处理服务
 * @author
 * @since 2018/11/26 19:52
 */
@Slf4j
public class LoginService {

    public static final LoginService OBJ = new LoginService();

    private Map<String, AbstractLogin> _handler = new ConcurrentHashMap<>();

    public LoginService(){
    }

    public void init(){
        //注册登录处理类
        new Login_Guest();
        new Login_Robot();
        new Login_Test();
    }

    /**
     * 注册处理类
     * @param handler
     */
    public void reg(AbstractLogin handler){
        log.info("注册登录处理类 {}", handler.getClass().getSimpleName());
        _handler.put(handler.getChannelId(), handler);
    }

    /**
     * 获得处理类
     * @param channel
     * @return
     */
    public AbstractLogin getHandler(String channel){
        if (_handler.containsKey(channel)){
            return _handler.get(channel);
        }

        return null;
    }

    /**
     * 登录处理、按渠道分发
     * @param req
     * @param ctx
     * @param reqHeader
     */
    public void processLogin(JoloAuth.JoloCommon_LoginReq req, ChannelHandlerContext ctx, Req.ReqHeader reqHeader) throws Exception{
        log.debug("登录处理器");
        //渠道
        String channel = req.getChannelId();
        //登录处理器
        AbstractLogin handler = getHandler(channel);
        if (handler == null){
            log.error("渠道channel = {}, 没有注册对应的处理类",channel);
            return;
        }

        log.info("处理登录");
        //处理登录请求
        handler.process(req, ctx, reqHeader);
    }
}
