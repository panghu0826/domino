package com.jule.domino.dispacher.network.process;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;
import com.jule.core.utils.HttpsUtil;
import com.jule.core.utils.MD5Util;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.service.AdConfigService;
import com.jule.domino.dispacher.config.Config;
import com.jule.domino.dispacher.dao.DBUtil;
import com.jule.domino.dispacher.dao.bean.AdInfoModel;
import com.jule.domino.dispacher.service.LogService;
import com.jule.domino.log.service.LogReasons;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JoloCommon_AdInfoReq_80001 extends Req {
    private byte[] blob;

    public JoloCommon_AdInfoReq_80001( int functionId) {
        super(functionId);
    }

    private JoloAuth.JoloCommon_AdInfoReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloCommon_AdInfoReq.parseFrom(blob);
    }

    @Override
    public void processImpl() throws Exception {
        try {
            log.info("广告请求消息={}",req.toString());

            User user = DBUtil.selectByPrimaryKey(req.getUserid());
            if (user == null){
                log.info("角色不存在uid={}", req.getUserid());
                sendAcqMsg(JoloAuth.JoloCommon_AdInfoAck.newBuilder()
                        .setResult(0)
                        .setTimes(0)
                        .setMoney(0)
                        .setResultMsg(ErrorCodeEnum.GATE_600001_2.getCode()));
                return;
            }

            StringBuffer buffer = new StringBuffer();
            buffer.append(req.getUserid()).append(req.getItemtype()).append(req.getAccount()).append(Config.AD_PUBLIC_KEY);
            //做md5验证
            String signStr = MD5Util.createMD5String(buffer.toString());
            if (!signStr.equals(req.getSign())){
                //验证不通过
                //返回
                log.info("广告md5验证失败,sign={},signstr={}", req.getSign(), signStr);
                sendAcqMsg(JoloAuth.JoloCommon_AdInfoAck.newBuilder()
                        .setResult(0)
                        .setTimes(0)
                        .setMoney(user.getMoney())
                        .setResultMsg(ErrorCodeEnum.DISPACHER_80001_1.getCode()));
                return;
            }

            int maxChip = AdConfigService.OBJ.getMaxChips();
            int maxTime = AdConfigService.OBJ.getMaxTimes();

            //查询角色广告信息
            AdInfoModel model = DBUtil.getAdInfo(req.getUserid());
            if (model == null){
                log.info("玩家角色信息为空uid={}", req.getUserid());
                sendAcqMsg(JoloAuth.JoloCommon_AdInfoAck.newBuilder()
                        .setResult(0)
                        .setTimes(0)
                        .setMoney(user.getMoney())
                        .setResultMsg(ErrorCodeEnum.DISPACHER_80001_1.getCode()));
                return;
            }

            if (model.getTotalmoney() + req.getAccount() > maxChip){
                log.info("广告奖励单次数量超限制account={},limit={}",req.getAccount(), maxChip);
                sendAcqMsg(JoloAuth.JoloCommon_AdInfoAck.newBuilder()
                        .setResult(0)
                        .setTimes(0)
                        .setMoney(user.getMoney())
                        .setResultMsg(ErrorCodeEnum.DISPACHER_80001_1.getCode()));
                return;
            }

            //验证玩家领取次数
            if (model.getTimes() >= maxTime){
                log.info("玩家领取次数超限times={},limit={}",model.getTimes(), maxTime);
                sendAcqMsg(JoloAuth.JoloCommon_AdInfoAck.newBuilder()
                        .setResult(0)
                        .setTimes(0)
                        .setMoney(user.getMoney())
                        .setResultMsg(ErrorCodeEnum.DISPACHER_80001_2.getCode()));
                return;
            }

            double org = user.getMoney();
            user.setMoney(user.getMoney() + req.getAccount());
            DBUtil.updateByPrimaryKey(user);
            StoredObjManager.hset(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + user.getId(), user);

            //通知游戏服
            noticeGame(user.getId(), req.getAccount());

            model.setTimes(model.getTimes() + 1);
            model.setTotalmoney(model.getTotalmoney()+req.getAccount());
            model.setLastTime(System.currentTimeMillis());
            DBUtil.updateAdInfo(model);

            //发送日志
            LogService.OBJ.sendMoneyLog(user, org, user.getMoney(), req.getAccount(), LogReasons.CommonLogReason.AD_GIVE);

            int left = maxTime - model.getTimes();
            sendAcqMsg(JoloAuth.JoloCommon_AdInfoAck.newBuilder()
                    .setResult(1)
                    .setTimes(left > 0 ? left : 0)
                    .setMoney(user.getMoney())
                    .setResultMsg(ErrorCodeEnum.DISPACHER_80001_1.getCode()));
        } catch (Exception e) {
            log.info("请求异常，exception={}",e.getMessage());
            sendAcqMsg(JoloAuth.JoloCommon_AdInfoAck.newBuilder()
                    .setResult(0)
                    .setTimes(0)
                    .setMoney(0)
                    .setResultMsg(ErrorCodeEnum.DISPACHER_80001_1.getCode()));
        }
    }

    private static final String public_key = "CE4239248A82C5C88FA7AB7B7841F274";

    /**
     * 通知游戏服,更新玩家牌桌游戏数据
     * @param userId
     *                  玩家ID
     * @param money
     *                  更新数量
     * @return
     *                  true-更新成功
     */
    public static boolean noticeGame(String userId, int money){
        //请求参数
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("userId", userId));
        list.add(new BasicNameValuePair("money", String.valueOf(money)));
        list.add(new BasicNameValuePair("sign", MD5Util.encodeByMD5(userId+money+public_key)));

        //请求地址
        String restUrl = JedisPoolWrap.getInstance().get(RedisConst.USER_LOGIN_GAME_URL.getProfix());
        String postUrl = "http://" + restUrl + "/api/game/updateChips";

        log.info(MessageFormat.format("玩家uid={0}开始通知桌面刷新筹码money={1},posturl={2}",
                userId,money,postUrl));

        //发起请求、result - 0失败  1成功
        String result = HttpsUtil.doPostForm(postUrl,list,false);
        log.info(MessageFormat.format("玩家uid={0}筹码刷新结果result={1}",userId,result));

        if (StringUtils.isEmpty(result)){
            return false;
        }
        /*long feedback = Long.valueOf(result);

        if (feedback != 0){
            //通客户端更新玩家信息
            JSONObject obj = new JSONObject();
            obj.put("userId", userId);
            obj.put("money", money);
            JedisPoolWrap.getInstance().pub(obj.toString(), RedisChannel.Add_NOTICE.getChannelName());
            log.info(MessageFormat.format("玩家uid={0}发布订阅通知,刷新显示筹码chips={1}",userId,result));
        }*/
        return true;
    }

}
