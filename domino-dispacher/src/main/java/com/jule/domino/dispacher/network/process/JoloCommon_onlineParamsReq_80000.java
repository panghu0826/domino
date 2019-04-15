package com.jule.domino.dispacher.network.process;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.dao.DBUtil;
import com.jule.domino.dispacher.dao.bean.OnlineConfigModel;
import com.jule.domino.dispacher.network.DispacherFunctionFactory;
import com.jule.domino.dispacher.dao.bean.AdvertisingConfigModel;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class JoloCommon_onlineParamsReq_80000 extends Req {
    private final static Logger logger = LoggerFactory.getLogger(JoloCommon_onlineParamsReq_80000.class);

    private byte[] blob;

    public JoloCommon_onlineParamsReq_80000(int functionId) {
        super(functionId);
    }

    private JoloAuth.JoloCommon_onlineParamsReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloCommon_onlineParamsReq.parseFrom(blob);
    }

    @Override
    public void processImpl() throws Exception {
        JoloAuth.JoloCommon_onlineParamsAck.Builder ack = JoloAuth.JoloCommon_onlineParamsAck.newBuilder();
        String channel = req.getChannel();
        String version = req.getVersion();
        String packName = req.getPackName();
        OnlineConfigModel ocm = DBUtil.selectOnlineConfigModel(Arrays.asList(channel, version,packName));//channel,version,packName
        if(ocm == null) {
            ack.setShowActivity(0);
            ack.setShowMail(0);
            ack.setPlayTypes("");
            ack.setShowAdvertising(0);
            DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ack.build().toByteArray()).send(ctx, reqHeader);
            return;
        }
        ack.setShowActivity(ocm.getActivitySwitch());
        ack.setShowMail(ocm.getMailSwitch());
        ack.setPlayTypes(ocm.getGameOrder());
        ack.setPlaynowTurn(ocm.getPlaynowTurn());
        ack.setShowAdvertising(ocm.getAdvertisingSwitch());
        ack.setExitAdvertising(ocm.getExitAdvertising());
        List<AdvertisingConfigModel> list = DBUtil.selectAllAdvertisingConfigModel();
        if (list != null && list.size() != 0) {
            AdvertisingConfigModel model = list.get(0);
            //状态为启动并且正在有效期内则返回数据
            long time = new Date().getTime();
            if (model.getAdvert_switch() == 1
                    && time > model.getCreate_time()
                    && time < model.getExpire_time()) {
                ack.setIconUrl(model.getAddress());
                ack.setJumpArgs(model.getJump_link());
            }
        }
        DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ack.build().toByteArray()).send(ctx, reqHeader);
    }
}
