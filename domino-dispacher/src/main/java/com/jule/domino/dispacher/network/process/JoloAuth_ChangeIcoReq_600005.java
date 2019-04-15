package com.jule.domino.dispacher.network.process;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.service.LogService;
import com.jule.domino.dispacher.dao.DBUtil;
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
            sendAcqMsg(ack.setResult(-1).setResultMsg(ErrorCodeEnum.DISPACHER_600005_1.getCode()));
            return;
        }

        //查询玩家
        User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId, User.class);
        if (user == null){
            user = DBUtil.selectByPrimaryKey(userId);
        }
        if (user == null){
            logger.error("user not exists ,uid = "+userId);
            sendAcqMsg(ack.setResult(-2).setResultMsg(ErrorCodeEnum.DISPACHER_600005_2.getCode()));
            return;
        }
        //发送头像更改日志
        LogService.OBJ.sendIcoChangeLog(user,user.getUser_defined_head(),ico);
        //保存数据
        user.setUser_defined_head(ico);
        if (DBUtil.updateByPrimaryKey(user) != 1){
            logger.error("Saving db error");
            sendAcqMsg(ack.setResult(-3).setResultMsg(ErrorCodeEnum.DISPACHER_600005_3.getCode()));
            return;
        }
        logger.info("9save userInfo->" + user.toString());
        StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);
        sendAcqMsg(ack.setResult(1).setResultMsg(ErrorCodeEnum.DISPACHER_600005_4.getCode()));
    }
}
