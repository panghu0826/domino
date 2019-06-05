package com.jule.domino.game.gate.network.process.reqs;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.configuration.ItemConfig;
import com.jule.core.jedis.StoredObjManager;
import com.jule.core.utils.MD5Security;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.ItemRecordsModel;
import com.jule.domino.game.dao.bean.RoomCardRecordsModel;
import com.jule.domino.game.dao.bean.UserItemModel;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.log.producer.RabbitMqSender;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 请求牌局记录
 */
public class JoloCommon_SendAndReceiveItemReq_80010 extends Req {

    private final static Logger logger = LoggerFactory.getLogger(JoloCommon_SendAndReceiveItemReq_80010.class);

    private JoloAuth.JoloCommon_SendAndReceiveItemReq req;

    public JoloCommon_SendAndReceiveItemReq_80010(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloCommon_SendAndReceiveItemReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId, req.toString());
    }

    @Override
    public void processImpl() throws Exception {
        JoloAuth.JoloCommon_SendAndReceiveItemAck.Builder ack = JoloAuth.JoloCommon_SendAndReceiveItemAck.newBuilder();
        String userId = req.getUserId();
        ack.setUserId(userId);
        try {
            //用户信息：从缓存获取
            User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId, User.class);
            ack.setMoney((int) user.getMoney());
            if (!req.hasSign()) {  //发出或购买道具
                int itemId = req.getItemId();
                int itemArgs = req.getItemTime();
                int itemPrice = ItemConfig.getItemParameter(itemId, itemArgs);
                if (req.getIsMe() == 1) { //购买道具或领取道具
                    if (user.getMoney() < itemPrice) {
                        sendResponse(functionId | 0x08000000, ack.setMoney((int) user.getMoney()).setResult(-2).setResultMsg("房卡不足").build().toByteArray());
                        return;
                    }
                    user.setMoney(user.getMoney() - itemPrice);
                    DBUtil.updateByPrimaryKey(user);
                    insertItemInfo(itemId, itemArgs);//插入道具信息
                } else {//赠送道具
                    String token = MD5Security.EncodeMD5Hex(String.valueOf(System.currentTimeMillis()));
                    user.setMoney(user.getMoney() - itemPrice);
                    DBUtil.updateByPrimaryKey(user);
                    ItemRecordsModel uim = new ItemRecordsModel();
                    uim.setCreateUserId(userId);
                    uim.setCreateNickName(user.getNick_name());
                    uim.setCreateIcoUrl(user.getIco_url());
                    uim.setCreateTime(new Date());
                    uim.setItemId(itemId);
                    uim.setItemTime(itemArgs);
                    uim.setState(0);
                    uim.setItemToken(token);
                    DBUtil.insertItemRecords(uim);
                    ack.setSign(token);
                }
            } else { //领取道具
                String token = req.getSign();
                ItemRecordsModel uim = DBUtil.selectItemByItemToken(token);
                Date date = insertItemInfo(uim.getItemId(), uim.getItemTime());//插入领取的道具信息
                uim.setReceiveUserId(userId);
                uim.setReceiveNickName(user.getNick_name());
                uim.setCreateIcoUrl(user.getIco_url());
                uim.setReceiveTime(new Date());
                uim.setState(1);
                uim.setDueTime(date);
                DBUtil.updateItemRecords(uim);
            }
            sendResponse(functionId | 0x08000000, ack.setMoney((int) user.getMoney()).setResult(1).build().toByteArray());
        } catch (Exception e) {
            sendResponse(functionId | 0x08000000, ack.setResult(-1).setResultMsg("该链接已失效。").build().toByteArray());
            e.printStackTrace();
        } finally {
            logger.info("80010 ack 道具发送领取记录：{}", ack.toString());
        }
    }

    private Date insertItemInfo(int itemId, int itemArgs) {
        UserItemModel uim = new UserItemModel();
        try {
            uim.setUserId(userId);
            uim.setItemId(String.valueOf(itemId));
            if (itemArgs == 2) {//一个月
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, 1);
                uim.setDueTime(calendar.getTime());
            } else {//七天
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, 7);
                uim.setDueTime(calendar.getTime());
            }
            DBUtil.insertItem(uim);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uim.getDueTime();
    }
}
