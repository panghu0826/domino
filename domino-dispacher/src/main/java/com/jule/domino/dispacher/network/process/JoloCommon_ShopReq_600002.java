package com.jule.domino.dispacher.network.process;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.utils.HttpsUtil;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.config.Config;
import com.jule.domino.dispacher.network.DispacherFunctionFactory;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Slf4j
public class JoloCommon_ShopReq_600002 extends Req {

    private byte[] blob;

    public JoloCommon_ShopReq_600002(int functionId) {
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
            byte[] ret = HttpsUtil.doPostProtoc(Config.PRODUCT_URL, blob, Config.ENABLE_SSL);
            if (ret != null) {
                DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ret).send(ctx, reqHeader);
            } else {
                log.warn("auth rpc fail! ->" + Config.PRODUCT_URL);
            }

        } catch (Exception e) {
            DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000,
                    JoloAuth.JoLoCommon_ProductAck.newBuilder().setResult(0).setResultMsg(ErrorCodeEnum.GAME_50002_2.getCode()
            ).build().toByteArray()).send(ctx, reqHeader);
        }
    }
}
