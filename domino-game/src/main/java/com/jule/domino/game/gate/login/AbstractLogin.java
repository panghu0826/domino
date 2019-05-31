package com.jule.domino.game.gate.login;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.common.log.LoggerUtils;
import com.jule.core.jedis.StoredObjManager;
import com.jule.core.network.ChannelHandler;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.platform.bean.PlatUserBean;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.gate.pool.net.ChannelManageCenter;
import com.jule.domino.game.gate.service.GateChannelService;
import com.jule.domino.game.gate.service.JedisService;
import com.jule.domino.game.gate.service.RegisteService;
import com.jule.domino.game.gate.service.UserService;
import com.jule.domino.game.service.LogService;
import com.jule.domino.game.service.NoticePlatformSerivce;
import com.jule.domino.log.service.LogReasons;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 登录处理器
 * @author
 * @since 2018/11/26 19:35
 */
@Getter@Setter@Slf4j
public abstract class AbstractLogin {

    public String ChannelId;

    /**
     * 构造
     * @param channelId
     */
    public AbstractLogin(String channelId) {
        ChannelId = channelId;
        //注册
        LoginService.OBJ.reg(this);
    }

    /**
     * 处理登录
     * @param req
     * @param ctx
     * @param reqHeader
     */
    public abstract void process(JoloAuth.JoloCommon_LoginReq req, ChannelHandlerContext ctx, Req.ReqHeader reqHeader) throws Exception;


    protected void bindUser(ChannelHandlerContext ctx, Req.ReqHeader reqHeader, String userId, String token){
        GateChannelService.OBJ.handlerRepeatLoginChannel(userId, RegisteService.ADDRESS);

        //绑定netty连接
        long sessionId = ChannelHandler.getSesseionId(ctx);
        StoredObjManager.hset(RedisConst.LINK_GATE_STAT.getProfix(), RedisConst.LINK_GATE_STAT.getField()+userId, String.valueOf(sessionId));
        ChannelManageCenter.getInstance().bind(sessionId,userId);

        //绑定netty连接
        UserService.getInstance().onUserLogin(userId, ctx);

        //关联玩家游戏
        String ip_port = Config.BIND_IP + ":" + Config.NOTICESERVER_BIND_PORT;
        JedisService.getInstance().server_information(""+reqHeader.gameId, userId, ip_port);

        //关联token
        StoredObjManager.set(RedisConst.USER_PLATFORM_TOKEN+userId, token);
    }

    /**
     * 来一个名字
     *
     * @param userId
     * @return
     */
    protected String getNickName(String userId) {
        try {
            String prefix = "G-";
            if (StringUtils.isEmpty(userId)) {
                return "";
            }

            int length = userId.length();
            if (userId.length() <= 6) {
                return prefix + userId;
            }

            String name = userId.substring(length - 6, length);
            return prefix + name;
        } catch (Exception e) {
            log.error("create name exception , " + e.getMessage());
            return ("G-" + userId).length() > 8 ? ("G-" + userId).substring(0, 8) : ("G-" + userId);
        }
    }

    //默认头像
    List<String> DEFUALT_ICONS = Arrays.asList(
            "txn1_png","txn2_png","txn3_png","txn4_png",
            "txv1_png","txv2_png","txv3_png","txv4_png"
    );
    /**
     * 随机头像方法
     * @return
     */
    private String randomIcon(){
        String ico = "";
        if (DEFUALT_ICONS == null
                || DEFUALT_ICONS.isEmpty()){
            return ico;
        }
        try {
            Random random = new Random();
            int idx = random.nextInt(DEFUALT_ICONS.size());
            ico = DEFUALT_ICONS.get(idx);
        }catch (Exception e){
            LoggerUtils.error.error("随机头像异常,exception={}",e.getMessage());
        }
        return ico;
    }
}
