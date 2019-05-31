package com.jule.domino.game.gate.network.process.reqs;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.platform.HallAPIService;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.service.LogService;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 请求更换玩家头像
 *
 */
public class JoloAuth_ChangeIcoReq_600005 extends Req {

    private final static Logger logger = LoggerFactory.getLogger(JoloAuth_ChangeIcoReq_600005.class);

    private JoloAuth.JoloAuth_ChangeIcoReq req;

    public JoloAuth_ChangeIcoReq_600005(int functionId ) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl( ByteBuf buf ) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloAuth_ChangeIcoReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId,req.toString());
    }

    @Override
    public void processImpl() throws Exception {
        JoloAuth.JoloAuth_ChangeIcoAck.Builder ack = JoloAuth.JoloAuth_ChangeIcoAck.newBuilder();
        String userId = req.getUserId();
        String ico = req.getIcoUrl();

        //参数是否正常
        if (StringUtils.isEmpty(userId)
                || StringUtils.isEmpty(ico)){
            logger.error("params is empty ,uid = "+userId+",ico ="+ico);
            ack.setResult(-1).setResultMsg("参数不正常");
            sendResponse(functionId | 0x08000000, ack.build().toByteArray());
            return;
        }

        //查询玩家
        User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId, User.class);
        if (user == null){
            user = DBUtil.selectByPrimaryKey(userId);
        }
        if (user == null){
            logger.error("user not exists ,uid = "+userId);
            ack.setResult(-2).setResultMsg("找不到该玩家");
            sendResponse(functionId | 0x08000000, ack.build().toByteArray());
            return;
        }
        //发送头像更改日志
        LogService.OBJ.sendIcoChangeLog(user,user.getUser_defined_head(),ico);

        //保存数据
        user.setUser_defined_head(ico);
        user.setIco_url(ico);

        if (DBUtil.updateByPrimaryKey(user) != 1){
            logger.error("Saving db error");
            ack.setResult(-3).setResultMsg("保存数据失败");
            sendResponse(functionId | 0x08000000, ack.build().toByteArray());
            return;
        }

        HallAPIService.OBJ.updateIcon(user.getAndroid_id(),ico);

        logger.info("9save userInfo->" + user.toString());
        StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
        sendResponse(functionId | 0x08000000, ack.setResult(1).build().toByteArray());
    }
}
