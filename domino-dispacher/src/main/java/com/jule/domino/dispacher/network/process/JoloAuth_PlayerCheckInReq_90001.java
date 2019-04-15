package com.jule.domino.dispacher.network.process;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.domino.base.bean.ItemConfigBean;
import com.jule.domino.base.service.ItemServer;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.config.Config;
import com.jule.domino.dispacher.dao.DBUtil;
import com.jule.domino.dispacher.dao.bean.RewardConfigModel;
import com.jule.domino.dispacher.dao.bean.RewardReceiveRecordModel;
import com.jule.domino.dispacher.network.DispacherFunctionFactory;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class JoloAuth_PlayerCheckInReq_90001 extends Req {
    private final static Logger logger = LoggerFactory.getLogger(JoloAuth_PlayerCheckInReq_90001.class);

    private byte[] blob;

    public JoloAuth_PlayerCheckInReq_90001(int functionId) {
        super(functionId);
    }

    private JoloAuth.JoloAuth_PlayerCheckInReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloAuth_PlayerCheckInReq.parseFrom(blob);
    }

    @Override
    public void processImpl() throws Exception {
        JoloAuth.JoloAuth_PlayerCheckInAck.Builder ack = JoloAuth.JoloAuth_PlayerCheckInAck.newBuilder();
        RewardReceiveRecordModel rrm = DBUtil.selectRewardReceiveRecord(req.getPlayerId());
        List<RewardConfigModel> list = DBUtil.selectAllRewardConfig();
        List<JoloAuth.JoloAuth_CheckInRewardObject> lists = new ArrayList<>();
        JoloAuth.JoloAuth_CheckInRewardObject.Builder cro = JoloAuth.JoloAuth_CheckInRewardObject.newBuilder();

        for (int in = 0; in < 7; in++) {
            String rewardGoodsType = list.get(in).getReward_goods_type();
            int itemId = list.get(in).getReward_goods_id();
            int itemType = 0;
            ItemConfigBean itemConfigBean = null;
            if (rewardGoodsType.equals("item") && itemId > 0) {
                itemConfigBean = ItemServer.OBJ.getTemplateByItemId(Config.GAME_ID, itemId);
                if (itemConfigBean != null) {
                    itemType = itemConfigBean.getItemType();
                }
            }
            cro.setRewardGoodsType(rewardGoodsType);
            cro.setRewardPictureAddress(list.get(in).getReward_picture_address());
            cro.setGuestCheckInReward(list.get(in).getReward_amount());
            cro.setFacebookCheckInReward(list.get(in + 7).getReward_amount());
            if (itemType == 7 || itemType == 8 || itemType == 9) {
                cro.setGuestCheckInReward(itemConfigBean!=null?(int)itemConfigBean.getTimeOut():0);
                cro.setFacebookCheckInReward(itemConfigBean!=null?(int)(itemConfigBean.getTimeOut()*list.get(in + 7).getReward_amount()):0);
            }

            cro.setItemType(itemType);
            lists.add(cro.build());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendar = Calendar.getInstance();//日历对象
        calendar.setTime(new Date());//设置当前日期
        calendar.add(Calendar.DAY_OF_MONTH, -1);//天数加一，为-1的话是天数减1

        if (rrm == null) {
            ack.setWhetherCheckInToo(0);
            ack.setCurrentCheckInDays(1);
            ack.setLoginMode(1);
            ack.addAllCheckInReward(lists);
            DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ack.build().toByteArray()).send(ctx, reqHeader);
        } else if (!sdf.format(rrm.getReceiveTime()).equals(sdf.format(new Date()))) {
            if (sdf.format(rrm.getReceiveTime()).equals(sdf.format(calendar.getTime()))) {
                ack.setCurrentCheckInDays(rrm.getContinuityLoginDay() % 7 + 1);
            } else {
                ack.setCurrentCheckInDays(1);
            }
            ack.setWhetherCheckInToo(0);
            ack.setLoginMode(1);
            ack.addAllCheckInReward(lists);
            DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ack.build().toByteArray()).send(ctx, reqHeader);
        } else {
            DispacherFunctionFactory.getInstance().getResponse(
                    functionId | 0x08000000, ack
                            .setWhetherCheckInToo(1)
                            .addAllCheckInReward(new ArrayList<>()).build().toByteArray()).send(ctx, reqHeader);
        }
    }
}
