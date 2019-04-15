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

public class JoloAuth_PlayerSeeCheckInReq_90002 extends Req {

    private final static Logger logger = LoggerFactory.getLogger(JoloAuth_PlayerSeeCheckInReq_90002.class);

    public JoloAuth_PlayerSeeCheckInReq_90002(int functionId) {
        super(functionId);
    }

    private JoloAuth.JoloAuth_PlayerSeeCheckInReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloAuth_PlayerSeeCheckInReq.parseFrom(blob);
    }

    @Override
    public void processImpl() throws Exception {
        JoloAuth.JoloAuth_PlayerSeeCheckInAck.Builder ack = JoloAuth.JoloAuth_PlayerSeeCheckInAck.newBuilder();
        List<RewardConfigModel> list = DBUtil.selectAllRewardConfig();
        List<JoloAuth.JoloAuth_CheckInRewardObject> lists = new ArrayList<>();
        JoloAuth.JoloAuth_CheckInRewardObject.Builder cro = JoloAuth.JoloAuth_CheckInRewardObject.newBuilder();

        for(int in = 0;in < 7;in ++) {
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
            cro.setGuestCheckInReward( list.get(in).getReward_amount());
            cro.setFacebookCheckInReward( list.get(in + 7).getReward_amount());
            if (itemType == 7 || itemType == 8 || itemType == 9) {
                cro.setGuestCheckInReward(itemConfigBean!=null?(int)itemConfigBean.getTimeOut():0);
                cro.setFacebookCheckInReward(itemConfigBean!=null?(int)(itemConfigBean.getTimeOut()*list.get(in + 7).getReward_amount()):0);
            }

            cro.setItemType(itemType);
            lists.add(cro.build());
        }
        RewardReceiveRecordModel rrm = DBUtil.selectRewardReceiveRecord(req.getPlayerId());
        logger.debug("{}当前玩家是否签到过{}", req.getPlayerId(), rrm==null? null:rrm.toString());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendar = Calendar.getInstance();//日历对象
        calendar.setTime(new Date());//设置当前日期
        calendar.add(Calendar.DAY_OF_MONTH, -1);//天数加一，为-1的话是天数减1

        if(rrm != null) {
            int days = rrm.getContinuityLoginDay() % 7;
            if (sdf.format(rrm.getReceiveTime()).equals(sdf.format(new Date()))) {
                DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ack
                        .setWhetherCheckInToo(1)
                        .setCurrentCheckInDays(days == 0 ? 7 : days).
                                addAllCheckInReward(lists).build().toByteArray()).send(ctx, reqHeader);
            } else {
                if(sdf.format(rrm.getReceiveTime()).equals(sdf.format(calendar.getTime()))){
                    DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ack
                            .setWhetherCheckInToo(0)
                            .setCurrentCheckInDays(days + 1).
                                    addAllCheckInReward(lists).build().toByteArray()).send(ctx, reqHeader);
                }else{
                    DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ack
                            .setWhetherCheckInToo(0)
                            .setCurrentCheckInDays(1).
                                    addAllCheckInReward(lists).build().toByteArray()).send(ctx, reqHeader);
                }
            }
        }else{ //玩家从没有签到过
            DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ack
                    .setWhetherCheckInToo(0)
                    .setCurrentCheckInDays(0).
                            addAllCheckInReward(new ArrayList<>()).build().toByteArray()).send(ctx, reqHeader);
        }
    }
}
