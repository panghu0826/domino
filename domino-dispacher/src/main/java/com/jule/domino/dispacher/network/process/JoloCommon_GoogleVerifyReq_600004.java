package com.jule.domino.dispacher.network.process;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.utils.HttpsUtil;
import com.jule.domino.base.bean.ItemBean;
import com.jule.domino.base.bean.ItemConfigBean;
import com.jule.domino.base.bean.UnitVO;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.service.ItemServer;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.config.Config;
import com.jule.domino.dispacher.network.DispacherFunctionFactory;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JoloCommon_GoogleVerifyReq_600004 extends Req {

    private byte[] blob;

    public JoloCommon_GoogleVerifyReq_600004(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
    }

    @Override
    public void processImpl() throws Exception {
        try {
            byte[] ret = HttpsUtil.doPostProtoc(Config.GOOGLEPLAY_VERIFY_URL, blob, Config.ENABLE_SSL);
            if (ret != null) {
                JoloAuth.JoloCommon_GoogleVerifyAck ack = JoloAuth.JoloCommon_GoogleVerifyAck.parseFrom(ret);
                if (ack.getType() == 2) {//购买物品
                    UnitVO unitVO = ItemServer.OBJ.getUnit(Config.GAME_ID, ack.getUserId(), ack.getItemTmpId());
                    if (unitVO.getResult() == 0) {
                        ItemBean itemBean = unitVO.getItems().get(0);
                        if (itemBean != null) {
                            ItemConfigBean itemConfigBean = ItemServer.OBJ.getTemplateByItemId(Config.GAME_ID,ack.getItemTmpId());
                            DispacherFunctionFactory.getInstance().getResponse(
                                    DispacherFunctionFactory.__function__id_600007 | 0x08000000,
                                    JoloAuth.JoloAuth_ItemsStatusAck.newBuilder().setUserId(ack.getUserId())
                                            .setCountDown(itemBean.getTimeOut())
                                            .setItemId(ack.getItemTmpId() + "")
                                            .setBigType(itemConfigBean.getItemType())
                                    .setNum(1).build().toByteArray()
                            ).send(ctx, reqHeader);
                        } else {
                            log.error("itemServer itemBean is null GameId:{},userId:{},itemTmpId:{}", Config.GAME_ID, ack.getUserId(), ack.getItemTmpId());
                        }

                    } else {
                        log.error("itemServer.getUnit is error GameId:{},userId:{},itemTmpId:{}", Config.GAME_ID, ack.getUserId(), ack.getItemTmpId());
                    }
                }
                DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ret).send(ctx, reqHeader);
            } else {
                log.warn("JoloCommon_GoogleVerifyReq_600004 rpc fail! ->" + Config.GOOGLEPLAY_VERIFY_URL);
            }

        } catch (Exception e) {
            DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000,
                    JoloAuth.JoloCommon_GoogleVerifyAck.newBuilder().setResult(0).
                            setResultMsg(ErrorCodeEnum.DISPACHER_600004_1.getCode()).build().toByteArray()).send(ctx, reqHeader);
        }
    }
}
