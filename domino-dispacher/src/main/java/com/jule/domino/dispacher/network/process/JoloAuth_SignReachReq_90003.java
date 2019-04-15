package com.jule.domino.dispacher.network.process;


import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.domino.base.bean.ItemConfigBean;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.service.ItemServer;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.service.LogService;
import com.jule.domino.dispacher.config.Config;
import com.jule.domino.dispacher.dao.bean.RewardConfigModel;
import com.jule.domino.log.service.LogReasons;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.dispacher.dao.DBUtil;
import com.jule.domino.dispacher.dao.bean.RewardReceiveRecordModel;
import com.jule.domino.dispacher.network.DispacherFunctionFactory;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class JoloAuth_SignReachReq_90003 extends Req {
    private final static Logger logger = LoggerFactory.getLogger(JoloAuth_SignReachReq_90003.class);

    public JoloAuth_SignReachReq_90003(int functionId) {
        super(functionId);
    }

    private JoloAuth.JoloAuth_SignReachReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloAuth_SignReachReq.parseFrom(blob);
    }

    @Override
    public void processImpl() throws Exception {
        JoloAuth.JoloAuth_SignReachAck.Builder ack = JoloAuth.JoloAuth_SignReachAck.newBuilder();
        ack.setResult(1).setMoney(0);
        User user = DBUtil.selectByPrimaryKey(req.getPlayerId());
        if (user == null) {
            DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ack
                    .setResult(0)
                    .setResultMsg(ErrorCodeEnum.GATE_600001_2.getCode()).build().toByteArray()).send(ctx, reqHeader);
            return;
        }

        RewardReceiveRecordModel checkIn = DBUtil.selectRewardReceiveRecord(req.getPlayerId());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (checkIn != null && sdf.format(checkIn.getReceiveTime()).equals(sdf.format(new Date()))) {
            logger.error("你今天已经签到过了");
            DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ack
                    .setResult(0)
                    .setResultMsg(ErrorCodeEnum.DISPACHER_90003_1.getCode()).build().toByteArray()).send(ctx, reqHeader);
            return;
        }

        //设置登录方式
        int extraId = 0;
        if ("facebook".equals(user.getChannel_id())) {
            extraId = 7;
        }
        RewardConfigModel rewardConfigModel = null;
        int insertrrm;
        if (checkIn == null) { //此处需加登陆方式判断
            RewardReceiveRecordModel rrm = new RewardReceiveRecordModel();
            rewardConfigModel = DBUtil.selectRewardConfig(1 + extraId);
            rrm.setPlayerId(req.getPlayerId());
            rrm.setRewardType("Auth_currency");
            rrm.setReceiveTime(new Date());
            rrm.setReceiveMode(1);
            rrm.setReceiveAmount(rewardConfigModel.getReward_amount());
            rrm.setReceiveNumber(1);
            rrm.setContinuityLoginDay(1);
            insertrrm = DBUtil.insertRewardReceiveRecord(rrm);
        } else {

            Calendar calendar = Calendar.getInstance();//日历对象
            calendar.setTime(new Date());//设置当前日期
            calendar.add(Calendar.DAY_OF_MONTH, -1);//天数加一，为-1的话是天数减1

            if (sdf.format(checkIn.getReceiveTime()).equals(sdf.format(calendar.getTime()))) {
                rewardConfigModel = DBUtil.selectRewardConfig(checkIn.getContinuityLoginDay() % 7 + 1 + extraId);
                checkIn.setContinuityLoginDay(checkIn.getContinuityLoginDay() + 1);
            } else {
                rewardConfigModel = DBUtil.selectRewardConfig(1 + extraId);
                checkIn.setContinuityLoginDay(1);
            }

            checkIn.setPlayerId(req.getPlayerId());
            checkIn.setRewardType("Auth_currency");
            checkIn.setReceiveTime(new Date());
            checkIn.setReceiveMode(1);
            checkIn.setReceiveAmount(rewardConfigModel.getReward_amount());
            checkIn.setReceiveNumber(checkIn.getReceiveNumber() + 1);
            insertrrm = DBUtil.insertRewardReceiveRecord(checkIn);
        }

        ack.setType(rewardConfigModel.getReward_goods_type());
        if (rewardConfigModel.getReward_goods_type().equals(GameConst.GOODS_GOLD)) {
            addMoney(user, rewardConfigModel, insertrrm, ack);
        } else {
            addItem(user, rewardConfigModel, insertrrm, ack);
        }


    }

    private void addMoney(User user, RewardConfigModel rewardConfigModel, int insertrrm, JoloAuth.JoloAuth_SignReachAck.Builder ack) {
        double score = user.getMoney();

        //保存数据库
        user.setMoney(rewardConfigModel.getReward_amount() + score);
        DBUtil.updateByPrimaryKey(user);

        User userInfo = DBUtil.selectByPrimaryKey(req.getPlayerId());
        /**保存玩家信息到缓存*/
        logger.info("10save userInfo->" + user.toString());
        StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userInfo.getId(), userInfo);

        if (insertrrm == 1) {
            //发送日志
            LogService.OBJ.sendMoneyLog(user, score, rewardConfigModel.getReward_amount() + score, rewardConfigModel.getReward_amount(), LogReasons.CommonLogReason.GAME_SIGN);


            ack.setMoney(DBUtil.getInstance().selectByPrimaryKey(req.getPlayerId()).getMoney());
            DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ack.build().toByteArray()).send(ctx, reqHeader);
        } else {
            logger.error("签到失败");
            DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ack
                    .setResult(0)
                    .setResultMsg(ErrorCodeEnum.DISPACHER_90003_2.getCode()).build().toByteArray()).send(ctx, reqHeader);
        }
    }

    private void addItem(User user, RewardConfigModel rewardConfigModel, int insertrrm, JoloAuth.JoloAuth_SignReachAck.Builder ack) {
        int num = (int) ((long) rewardConfigModel.getReward_amount());
        ItemServer.OBJ.addUnit(Config.GAME_ID, user.getId(), rewardConfigModel.getReward_goods_id(), num, "check in gained");
        if (insertrrm == 1) {
            ItemConfigBean bean = ItemServer.OBJ.getTemplateByItemId(Config.GAME_ID, rewardConfigModel.getReward_goods_id());
            if (bean != null){
                ack.setItemNum(Integer.valueOf(String.valueOf(num * bean.getTimeOut()))).
                        setItemUrl(rewardConfigModel.getReward_picture_address()).
                        setItemType(bean.getItemType());
                DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ack.build().toByteArray()).send(ctx, reqHeader);
                LogService.OBJ.sendItemLog(user, num,bean,LogReasons.CommonLogReason.GAME_SIGN);
            }else {
                logger.error("签到失败");
                DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ack
                        .setResult(0)
                        .setResultMsg(ErrorCodeEnum.DISPACHER_90003_2.getCode()).build().toByteArray()).send(ctx, reqHeader);
            }

        } else {
            logger.error("签到失败");
            DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ack
                    .setResult(0)
                    .setResultMsg(ErrorCodeEnum.DISPACHER_90003_2.getCode()).build().toByteArray()).send(ctx, reqHeader);
        }
    }
}
