package com.jule.domino.dispacher.network.process;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.utils.HttpsUtil;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.config.Config;
import com.jule.domino.dispacher.network.DispacherFunctionFactory;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoloCommon_OrderReq_600003 extends Req {
    private final static Logger logger = LoggerFactory.getLogger(JoloCommon_OrderReq_600003.class);

    private byte[] blob;

    public JoloCommon_OrderReq_600003(int functionId) {
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
            byte[] ret = HttpsUtil.doPostProtoc(Config.ORDER_URL, blob, Config.ENABLE_SSL);
            if (ret != null) {
                DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ret).send(ctx, reqHeader);
            } else {
                logger.warn("auth rpc fail! ->" + Config.ORDER_URL);
            }

        } catch (Exception e) {
            DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000,
                    JoloAuth.JoLoCmmon_OrderAck.newBuilder().setResult(0).setResultMsg(ErrorCodeEnum.GAME_50002_2.getCode()
            ).build().toByteArray()).send(ctx, reqHeader);
        }
    }
}
