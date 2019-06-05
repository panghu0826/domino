package com.jule.domino.game.gate.network.process.reqs;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.ItemRecordsModel;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.log.producer.RabbitMqSender;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 请求道具记录
 */
public class JoloCommon_ItemRecordsReq_80011 extends Req {

    private final static Logger logger = LoggerFactory.getLogger(JoloCommon_ItemRecordsReq_80011.class);

    private JoloAuth.JoloCommon_ItemRecordsReq req;

    public JoloCommon_ItemRecordsReq_80011(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloCommon_ItemRecordsReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId, req.toString());
    }

    @Override
    public void processImpl() throws Exception {
        JoloAuth.JoloCommon_ItemRecordsAck.Builder ack = JoloAuth.JoloCommon_ItemRecordsAck.newBuilder();
        try {
            List<JoloAuth.JoloCommon_ItemRecords> array = new ArrayList<>();
            if (req.hasSign()) {
                ItemRecordsModel irm = DBUtil.selectItemByItemToken(req.getSign());
                array.add(setMessageBody(irm));
            } else {
                List<ItemRecordsModel> list = DBUtil.selectItemByUserId(req.getUserId());
                list.forEach(e -> array.add(setMessageBody(e)));
            }
            sendResponse(functionId | 0x08000000, ack.addAllRecords(array).build().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            logger.info("80011 ack 道具请求消息回复：{}", ack.toString());
        }
    }

    /**
     * 填充消息内容
     */
    private JoloAuth.JoloCommon_ItemRecords setMessageBody(ItemRecordsModel irm) {
        return JoloAuth.JoloCommon_ItemRecords.newBuilder()
                .setCreateUserId(irm.getCreateUserId())
                .setCreateNickName(irm.getCreateNickName())
                .setCreateIcoUrl(irm.getCreateIcoUrl())
                .setCreateTime(irm.getCreateTime())
                .setItemToken(irm.getItemToken())
                .setItemId(irm.getItemId())
                .setItemTime(irm.getItemTime())
                .setState(irm.getState())
                .setReceiveUserId(irm.getReceiveUserId() != null ? irm.getReceiveUserId() : "")
                .setReceiveNickName(irm.getReceiveNickName() != null ? irm.getReceiveNickName() : "")
                .setReceiveIcoUrl(irm.getReceiveIcoUrl() != null ? irm.getReceiveIcoUrl() : "")
                .setReceiveTime(irm.getReceiveTime())
                .setDueTime(irm.getDueTime()).build();
    }
}
