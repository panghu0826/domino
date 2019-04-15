package com.jule.domino.dispacher.network.process;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.utils.TimeUtil;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.service.AdConfigService;
import com.jule.domino.dispacher.dao.DBUtil;
import com.jule.domino.dispacher.dao.bean.AdInfoModel;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JoloCommon_AdCountReq_80004 extends Req {
    private byte[] blob;

    public JoloCommon_AdCountReq_80004( int functionId) {
        super(functionId);
    }

    private JoloAuth.JoloCommon_AdCountReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloCommon_AdCountReq.parseFrom(blob);
    }

    @Override
    public void processImpl() throws Exception {
        try {
            log.info("广告消息={}",req.toString());

            User user = DBUtil.selectByPrimaryKey(req.getUserid());
            if (user == null){
                log.info("角色不存在uid={}", req.getUserid());
                sendAcqMsg(JoloAuth.JoloCommon_AdCountAck.newBuilder()
                        .setResult(0)
                        .setTimes(0)
                        .setResultMsg(ErrorCodeEnum.GATE_600001_2.getCode()));
                return;
            }



            //查询角色广告信息
            AdInfoModel model = DBUtil.getAdInfo(req.getUserid());
            if (model == null){
                log.info("玩家角色信息为空uid={}", req.getUserid());
                sendAcqMsg(JoloAuth.JoloCommon_AdCountAck.newBuilder()
                        .setResult(0)
                        .setTimes(0)
                        .setResultMsg(ErrorCodeEnum.DISPACHER_80001_1.getCode()));
                return;
            }

            int left = AdConfigService.OBJ.getMaxTimes() - model.getTimes();
            sendAcqMsg(JoloAuth.JoloCommon_AdCountAck.newBuilder()
                    .setResult(1)
                    .setTimes(left>=0?left:0)
                    .setNextIntvel(TimeUtil.toNextDate()));
        } catch (Exception e) {
            log.info("请求异常，exception={}",e.getMessage());
            sendAcqMsg(JoloAuth.JoloCommon_AdCountAck.newBuilder()
                    .setResult(0)
                    .setTimes(0)
                    .setResultMsg(ErrorCodeEnum.DISPACHER_80001_1.getCode()));
        }
    }

}
