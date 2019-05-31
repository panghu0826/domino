package com.jule.domino.game.gate.network.process.reqs;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.RoomCardRecordsModel;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.log.producer.RabbitMqSender;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 请求房卡记录
 */
public class JoloCommon_RoomCardRecordsReq_80005 extends Req {

    private final static Logger logger = LoggerFactory.getLogger(JoloCommon_RoomCardRecordsReq_80005.class);

    private JoloAuth.JoloCommon_RoomCardRecordsReq req;

    public JoloCommon_RoomCardRecordsReq_80005(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloCommon_RoomCardRecordsReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId, req.toString());
    }

    @Override
    public void processImpl() throws Exception {
        JoloAuth.JoloCommon_RoomCardRecordsAck.Builder ack = JoloAuth.JoloCommon_RoomCardRecordsAck.newBuilder();
        try {
            List<JoloAuth.JoloCommon_RoomCardRecords> array = new ArrayList<>();
            if (req.hasSign()) {
                RoomCardRecordsModel rcrm = DBUtil.selectRoomCardByMoneyToken(req.getSign());
                array.add(setMessageBody(rcrm));
            } else {
                List<RoomCardRecordsModel> list = DBUtil.selectRoomCardByUserId(req.getUserId());
                for (RoomCardRecordsModel rcrm : list) {
                    array.add(setMessageBody(rcrm));
                }
            }
            sendResponse(functionId | 0x08000000, ack.addAllRecords(array).build().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            logger.info("80005 房卡请求消息回复：{}", ack.toString());
        }
    }

    /**
     * 填充消息内容
     *
     * @param rcrm
     * @return
     */
    private JoloAuth.JoloCommon_RoomCardRecords setMessageBody(RoomCardRecordsModel rcrm) {
        return JoloAuth.JoloCommon_RoomCardRecords.newBuilder()
                .setCreateUserId(rcrm.getCreateUserId())
                .setCreateNickName(rcrm.getCreateNickName())
                .setCreateIcoUrl(rcrm.getCreateIcoUrl())
                .setCreateTime(rcrm.getCreateTime())
                .setMoneyToken(rcrm.getMoneyToken())
                .setMoney(rcrm.getMoney())
                .setState(rcrm.getState())
                .setReceiveUserId(rcrm.getReceiveUserId() != null ? rcrm.getReceiveUserId() : "")
                .setReceiveNickName(rcrm.getReceiveNickName() != null ? rcrm.getReceiveNickName() : "")
                .setReceiveIcoUrl(rcrm.getReceiveIcoUrl() != null ? rcrm.getReceiveIcoUrl() : "")
                .setReceiveTime(rcrm.getReceiveTime()).build();
    }
}
