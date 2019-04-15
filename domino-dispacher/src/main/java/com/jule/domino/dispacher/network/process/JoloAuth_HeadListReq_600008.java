package com.jule.domino.dispacher.network.process;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.service.ProductionService;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JoloAuth_HeadListReq_600008 extends Req {
    private JoloAuth.JoloAuth_HeadListReq req;

    public JoloAuth_HeadListReq_600008(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloAuth_HeadListReq.parseFrom(blob);
    }

    @Override
    public void processImpl() throws Exception {
        log.debug("收到消息-> " + functionId + " reqNum-> " + reqHeader.reqNum + " " + req.toString());
        try {
            JoloAuth.JoloAuth_HeadListAck.Builder ack = JoloAuth.JoloAuth_HeadListAck.newBuilder();
            ack.setUserId(req.getUserId())
                    .addAllItemInfoList(ProductionService.getInstance().getAllGiftConfig())
                    .addAllLimitItemList(ProductionService.getInstance().getLimitGiftConfig()).setResult(1);
            sendAcqMsg(ack);
            if(ProductionService.getInstance().getLimitGiftConfig().size()<2) {
                log.info("giftListReq,allHeadConfig size:{},limitHeadConfig size:{}",
                        ProductionService.getInstance().getLimitGiftConfig().size(),
                        ProductionService.getInstance().getLimitGiftConfig().size()
                );
            }
        }catch(Exception ex){
            log.error(ex.getMessage(),ex);
        }
    }
}
