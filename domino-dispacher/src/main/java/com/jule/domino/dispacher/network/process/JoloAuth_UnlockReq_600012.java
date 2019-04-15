package com.jule.domino.dispacher.network.process;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.domino.base.bean.ItemBean;
import com.jule.domino.base.bean.UnitVO;
import com.jule.domino.base.service.ItemServer;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.config.Config;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class JoloAuth_UnlockReq_600012 extends Req {
    private JoloAuth.JoloAuth_UnlockReq req;

    public JoloAuth_UnlockReq_600012(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloAuth_UnlockReq.parseFrom(blob);
    }

    @Override
    public void processImpl() throws Exception {
        log.debug("收到消息-> " + functionId + " reqNum-> " + reqHeader.reqNum + " " + req.toString());
        if (!Config.CAN_UNLOCK_ITEM) {
            log.error("error: Have no right to unlock");
            return;
        }
        try {
            int itemId = Integer.parseInt(req.getItemId());
            String userId = req.getUserId();

            ItemServer.OBJ.addUnit(Config.GAME_ID, userId, itemId, 1, "TestUnlock");

            UnitVO unitVO = ItemServer.OBJ.getUnit(Config.GAME_ID, userId, itemId);
            if (unitVO.getResult() == 0) {
                List<ItemBean> itemBeans = unitVO.getItems();
                if (itemBeans.size() > 0) {
                    if (itemBeans.get(0) != null) {
                        JoloAuth.JoloAuth_ItemStatusInfo.Builder itemStatusInfo = JoloAuth.JoloAuth_ItemStatusInfo.newBuilder();
                        itemStatusInfo.setNum(itemBeans.get(0).getItemNum()).setItemId(req.getItemId()).setCountDown(itemBeans.get(0).getTimeOut());

                        JoloAuth.JoloAuth_UnlockAck.Builder ack = JoloAuth.JoloAuth_UnlockAck.newBuilder();
                        ack.setUserId(req.getUserId())
                                .setItemStatusInfoList(itemStatusInfo).setResult(1);
                        sendAcqMsg(ack);
                    }
                }
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
