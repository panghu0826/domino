package com.jule.domino.gate.network.process;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.alibaba.fastjson.JSONObject;
import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;
import com.jule.core.network.ChannelHandler;
import com.jule.core.utils.RC4;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisChannel;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.gate.config.Config;
import com.jule.domino.gate.dao.DBUtil;
import com.jule.domino.gate.network.GateFunctionFactory;
import com.jule.domino.gate.network.protocol.Req;
import com.jule.domino.gate.service.JedisService;
import com.jule.domino.gate.service.RegisteService;
import com.jule.domino.gate.service.UserService;
import com.jule.domino.gate.vavle.net.ChannelManageCenter;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 可以理解为游戏内的登陆采用本地自制策略
 */
@Slf4j
public class JoloCommon_LoginReq_600001 extends Req {
    private byte[] blob;

    public JoloCommon_LoginReq_600001(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
    }

    @Override
    public void processImpl() throws Exception {
        JoloAuth.JoloCommon_LoginReq req = JoloAuth.JoloCommon_LoginReq.parseFrom(blob);
        try {
            log.debug(req.toString());

            String con = "";
            JSONObject js = new JSONObject();
            js.put("userId", req.getUserId());
            js.put("gateSvr", RegisteService.ADDRESS);
            con = js.toString();
            StoredObjManager.publish(con,
                    RedisChannel.REPEAT_LOGIN_CHANNEL.getChannelName());
            long sessionId = ChannelHandler.getSesseionId(this.ctx);
            StoredObjManager.hset(RedisConst.LINK_GATE_STAT.getProfix(),RedisConst.LINK_GATE_STAT.getField()+req.getUserId(),""+sessionId);
            ChannelManageCenter.getInstance().bind(sessionId,req.getUserId());
            if (!verifyToken(req)){
                //验证不通过
                GateFunctionFactory.getInstance().getResponse(functionId | 0x08000000, JoloAuth.JoloCommon_LoginAck.newBuilder()
                        .setResult(0)
                        .setResultMsg(ErrorCodeEnum.GATE_600001_1.getCode())
                        .setVerify(req.getVerify())
                        .build().toByteArray()).send(ctx, reqHeader, true);
                return;
            }

            User user = DBUtil.selectByPrimaryKey(req.getUserId());
            if (user == null) {
                GateFunctionFactory.getInstance().getResponse(functionId | 0x08000000, JoloAuth.JoloCommon_LoginAck.newBuilder()
                        .setResult(0)
                        .setVerify(req.getVerify())
                        .setResultMsg(ErrorCodeEnum.GATE_600001_2.getCode()).build().toByteArray()).send(ctx, reqHeader, true);
                return;
            }

            //游客存在
            UserService.getInstance().onUserLogin(req.getUserId(), ctx);

            String ip_port = Config.BIND_IP + ":" + Config.NOTICESERVER_BIND_PORT;
            JedisService.getInstance().server_information(""+reqHeader.gameId,req.getUserId(), ip_port);

            GateFunctionFactory.getInstance().getResponse(functionId | 0x08000000, JoloAuth.JoloCommon_LoginAck.newBuilder()
                    .setResult(1)
                    .setUserId(user.getId())
                    .setNickName(user.getNick_name())
                    .setIcoUrl(user.getIco_url())
                    .setMoney(user.getMoney())
                    .setVerify(req.getVerify())
                    .setChannelId(user.getChannel_id()).build().toByteArray()).send(ctx, reqHeader);

        } catch (Exception e) {
            GateFunctionFactory.getInstance().getResponse(functionId | 0x08000000, JoloAuth.JoloCommon_LoginAck.newBuilder().setResult(0).setVerify(req.getVerify()).setResultMsg(ErrorCodeEnum.GAME_50002_2.getCode()).build().toByteArray()).send(ctx, reqHeader, false);
        }
    }

    /**
     * 登录验证
     * @param req
     * @return
     */
    private boolean verifyToken(JoloAuth.JoloCommon_LoginReq req){
        String verify = req.getVerify();
        String uid = req.getUserId();
        log.info("verify params , verify = "+verify+" , uid = "+uid);

        if (StringUtils.isEmpty(verify)|| StringUtils.isEmpty(uid)){
            return false;
        }

        //验证缓存
        String cacheEncry = JedisPoolWrap.getInstance().get(GameConst.CACHE_USER_TOKEN+uid);
        if (StringUtils.isEmpty(cacheEncry) || !cacheEncry.equals(verify)){
            log.error("verify params not equal, verify = "+verify+" , cacheEncry = "+cacheEncry);
            return  false;
        }

        //解密验证串
        String decry = RC4.decry_RC4(verify,RC4.SECRET_KEY);
        if (!decry.contains("_")){
            log.error("verify decry not right, decry = "+decry);
            return false;
        }

        String strs[] = decry.split("_");
        if (strs == null || strs.length <= 0 ){
            log.error("verify decry not logic");
            return  false;
        }

        if (!uid.equals(strs[0])){
            log.error("verify decry not equal uid ,uid="+uid+",str[0]="+strs[0]);
            return  false;
        }
        return true ;
    }
}
