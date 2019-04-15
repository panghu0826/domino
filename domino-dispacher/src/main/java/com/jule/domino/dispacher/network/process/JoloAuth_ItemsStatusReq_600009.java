package com.jule.domino.dispacher.network.process;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.domino.base.bean.ItemBean;
import com.jule.domino.base.bean.UnitVO;
import com.jule.domino.base.service.ItemServer;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.config.Config;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class JoloAuth_ItemsStatusReq_600009 extends Req {
    private JoloAuth.JoloAuth_ItemsStatusListReq req;
    public JoloAuth_ItemsStatusReq_600009(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloAuth_ItemsStatusListReq.parseFrom(blob);
    }

    @Override
    public void processImpl() throws Exception {
        log.debug("收到消息-> " + functionId + ", reqNum-> " + reqHeader.reqNum + ", req->" + req.toString());
        String userId = req.getUserId();
        int itemType = req.getItemType();

        JoloAuth.JoloAuth_ItemsStatusListAck.Builder ack = JoloAuth.JoloAuth_ItemsStatusListAck.newBuilder();
        ack.setResult(1).setUserId(userId);
        try {
            UnitVO unitVO = ItemServer.OBJ.getUnitByType(Config.GAME_ID, userId, itemType);
            if (unitVO.getResult() == 0) {
                List<ItemBean> list = unitVO.getItems();
                Map<Integer, JoloAuth.JoloAuth_ItemStatusInfo> items = new HashMap<>();
                list.forEach(itemBean -> {
                    JoloAuth.JoloAuth_ItemStatusInfo.Builder itemStatusInfo = JoloAuth.JoloAuth_ItemStatusInfo.newBuilder();
                    itemStatusInfo.setCountDown(itemBean.getTimeOut())
                            .setItemId("" + itemBean.getItemID())
                            .setNum(itemBean.getItemNum());
                    items.put(itemBean.getItemID(),itemStatusInfo.build());
                });
                ack.addAllItemStatusInfoList(items.values());
            }
            sendAcqMsg(ack);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
