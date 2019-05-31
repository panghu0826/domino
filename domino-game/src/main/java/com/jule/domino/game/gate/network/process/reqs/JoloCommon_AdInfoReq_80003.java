package com.jule.domino.game.gate.network.process.reqs;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.jedis.StoredObjManager;
import com.jule.core.utils.MD5Security;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.RoomCardRecordsModel;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.log.producer.RabbitMqSender;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * 请求牌局记录
 */
public class JoloCommon_AdInfoReq_80003 extends Req {

    private final static Logger logger = LoggerFactory.getLogger(JoloCommon_AdInfoReq_80003.class);

    private JoloAuth.JoloCommon_AdInfoReq req;

    public JoloCommon_AdInfoReq_80003(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloCommon_AdInfoReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId, req.toString());
    }

    @Override
    public void processImpl() throws Exception {
        JoloAuth.JoloCommon_AdInfoAck.Builder ack = JoloAuth.JoloCommon_AdInfoAck.newBuilder();
        String userId = req.getUserId();
        ack.setUserId(userId);
        try {
            //用户信息：从缓存获取
            User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId, User.class);
            ack.setMoney((int) user.getMoney());
            if (!req.hasSign()) {//发出房卡
                int money = req.getMoney();
                String token = MD5Security.EncodeMD5Hex(String.valueOf(System.currentTimeMillis()));
                user.setMoney(user.getMoney() - money);
                DBUtil.updateByPrimaryKey(user);
                RoomCardRecordsModel rcrm = new RoomCardRecordsModel();
                rcrm.setCreateUserId(userId);
                rcrm.setCreateNickName(user.getNick_name());
                rcrm.setCreateIcoUrl(user.getIco_url());
                rcrm.setCreateTime(new Date());
                rcrm.setMoney(money);
                rcrm.setState(0);
                rcrm.setMoneyToken(token);
                rcrm.setReceiveTime(new Date());
                DBUtil.insertRoomCardRecords(rcrm);
                ack.setSign(token);
            } else {//领取房卡
                String token = req.getSign();
                RoomCardRecordsModel rcrm = DBUtil.selectRoomCardByMoneyToken(token);
                int money = rcrm.getMoney();
                user.setMoney(user.getMoney() + money);
                DBUtil.updateByPrimaryKey(user);
                rcrm.setReceiveUserId(userId);
                rcrm.setReceiveNickName(user.getNick_name());
                rcrm.setCreateIcoUrl(user.getIco_url());
                rcrm.setReceiveTime(new Date());
                rcrm.setState(1);
                DBUtil.updateRoomCardRecords(rcrm);
            }
            sendResponse(functionId | 0x08000000, ack.setMoney((int) user.getMoney()).setResult(1).build().toByteArray());
        } catch (Exception e) {
            sendResponse(functionId | 0x08000000, ack.setResult(-1).setResultMsg("该链接已失效。").build().toByteArray());
            e.printStackTrace();
        }finally {
            logger.info("80003 ack 房卡发送领取记录：{}", ack.toString());
        }
    }

    /**
     * 生成唯一字符串
     *
     * @return
     */
    public static String makeToken() {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte md5[] = md.digest(String.valueOf(System.currentTimeMillis()).getBytes());
            BASE64Encoder encoder = new BASE64Encoder();
            return encoder.encode(md5);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
