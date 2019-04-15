package com.jule.domino.game.gate.login;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.google.common.base.Strings;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.gate.network.GateFunctionFactory;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.service.LogService;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;

/**
 * 机器人登录处理器
 * @author
 * @since 2018/11/26 19:42
 */
@Slf4j
public class Login_Robot extends AbstractLogin{

    private static final String _script = "robot";

    /**
     * 构造
     */
    public Login_Robot() {
        super(_script);
    }

    @Override
    public void process(JoloAuth.JoloCommon_LoginReq req, ChannelHandlerContext ctx, Req.ReqHeader reqHeader) throws Exception {
        JoloAuth.JoloCommon_LoginAck.Builder builder = JoloAuth.JoloCommon_LoginAck.newBuilder();
        try {
            String userId = req.getUserId();
            log.debug("机器人登录：robotID=" + userId);
            User user = null;
            if (!Strings.isNullOrEmpty(userId)) {
                user = DBUtil.selectByPrimaryKey(userId);
            }


            if (user == null || StringUtils.isEmpty(userId)) {
                log.error("机器人ID={}不存在、登录失败", userId);

                builder.setUserId("")
                        .setMoney(0)
                        .setIcoUrl("")
                        .setNickName("")
                        .setDefaultIco("")
                        .setVerify("")
                        .setChannelId("")
                        .addAllServerinfo(new ArrayList<>())
                        .setResult(0).build();
                GateFunctionFactory.getInstance().getResponse(reqHeader.functionId  | 0x08000000, builder.build().toByteArray()).send(ctx, reqHeader);

                return;
            }

            user.setLast_login(new Date());
            DBUtil.updateByPrimaryKey(user);

            /**保存玩家信息到缓存*/
            log.info("5save userInfo->" + user.toString());
            StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);

            //发送登录日志
            LogService.OBJ.sendUserLoginLog(user);
            builder.setUserId(user.getId())
                    .setMoney(user.getMoney())
                    .setIcoUrl(user.getIco_url())
                    .setDefaultIco(user.getUser_defined_head() == null ? "" : user.getUser_defined_head())
                    .setNickName(user.getNick_name())
                    .setVerify("")
                    .setChannelId(user.getChannel_id())
                    .addAllServerinfo(new ArrayList<>())
                    .setResult(1).build();
            GateFunctionFactory.getInstance().getResponse(reqHeader.functionId  | 0x08000000, builder.build().toByteArray()).send(ctx, reqHeader);
            //玩家线程绑定
            bindUser(ctx, reqHeader, user.getId(), req.getToken());
        } catch (Exception ex) {
            log.error(ex.getMessage(),ex);
            builder.setUserId("")
                    .setMoney(0)
                    .setIcoUrl("")
                    .setNickName("")
                    .setDefaultIco("")
                    .setVerify("")
                    .setChannelId("")
                    .addAllServerinfo(new ArrayList<>())
                    .setResult(-1).build();
            GateFunctionFactory.getInstance().getResponse(reqHeader.functionId  | 0x08000000, builder.build().toByteArray()).send(ctx, reqHeader);
        }
    }
}
