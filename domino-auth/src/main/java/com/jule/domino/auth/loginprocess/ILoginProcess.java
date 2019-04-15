package com.jule.domino.auth.loginprocess;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.google.protobuf.MessageLite;

import com.jule.core.common.log.LoggerUtils;
import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.utils.RC4;

import com.jule.domino.auth.config.Config;
import com.jule.domino.auth.dao.DBUtil;
import com.jule.domino.auth.dao.bean.Currency;
import com.jule.domino.auth.service.LogService;
import com.jule.domino.auth.utils.ThreadPool;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.GameConst;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public interface ILoginProcess {
    MessageLite process(JoloAuth.JoloCommon_LoginReq req) throws Exception;
    //玩法分类
    List<JoloAuth.JoLoCommon_ServerInfo> list = new ArrayList<>();
    //默认头像
    List<String> DEFUALT_ICONS = Arrays.asList(
            "txn1_png","txn2_png","txn3_png","txn4_png",
            "txv1_png","txv2_png","txv3_png","txv4_png"
    );

    static void init(){
        list.add(JoloAuth.JoLoCommon_ServerInfo.newBuilder().setGameId("71001001").setIp(Config.CLASSIC_SERVER_IP).setPort(Config.CLASSIC_SERVER_PORT).build());
    }

    default List<JoloAuth.JoLoCommon_ServerInfo> getServerinfo(){
        return list;
    }

    /**
     * 建角色操作
     * @param req
     * @param user
     * @return
     */
    default int createUser(JoloAuth.JoloCommon_LoginReq req,User user){
        user.setChannel_id(req.getChannelId());
        user.setClient_version(req.getClientVersion());
        user.setDevice_num(req.getDeviceNum());
        user.setPlatform(req.getPlatform());
        user.setUser_ip(req.getUserIp());
        user.setRegistration_time(new Date());
        user.setLast_login(new Date());
        user.setLast_offline(new Date());
        user.setAndroid_id(req.getUserId());
        user.setMei_code(req.getDeviceNum());
        user.setDown_platform(req.getDownPlatform());
        user.setPackage_name(req.getPackName());
        int count = DBUtil.insert(user);

        //将玩家此操作产生的金额变动记录插入到数据库
        if(Config.CURRENCY) {
            Currency curr = new Currency();
            curr.setTable_id("");
            curr.setPlayer_id(user.getId());
            curr.setNick_name(user.getNick_name());
            curr.setOperation("giving");//'sit_down','stand_up','buy_in','giving','top_up','bet','settlement'
            curr.setAmount(user.getMoney());//操作的货币量
            curr.setGame_order_id("");//游戏唯一订单号
            curr.setBet(0l);//下注
            curr.setPoundage(0l);//手续费
            curr.setWin_jetton(0l);//赢的筹码
            curr.setLose_jetton(0l);//输的筹码
            curr.setJetton(0l);//玩家目前的筹码
            curr.setMoney(user.getMoney());//玩家目前的货币(总钱数减去筹码数)
            curr.setUniversal(req.getChannelId()+"login");
            ThreadPool.pool(curr);//插入操作交给其它线程
        }
        if (count == 1){
            //建角成功
            //发送建角日志
            LogService.OBJ.sendUserCreateLog(user);

            //发送登录日志
            LogService.OBJ.sendUserLoginLog(user);
        }
        return  count;
    }

    default String encry_RC4(String uid){
        //uid不存在，直接返回
        if (StringUtils.isEmpty(uid)){
            return "";
        }
        //加密串
        String encryStr = uid +"_"+ String.valueOf(System.currentTimeMillis());
        String encry = RC4.encry_RC4_string(encryStr,RC4.SECRET_KEY);

        //缓存加密串
        JedisPoolWrap.getInstance().set(GameConst.CACHE_USER_TOKEN+uid,encry,-1);
        return encry;
    }

    /**
     * 随机头像方法
     * @return
     */
   default String randomIcon(){
        String ico = "";
        if (DEFUALT_ICONS == null
                || DEFUALT_ICONS.isEmpty()){
            return ico;
        }
        try {
            Random random = new Random();
            int idx = random.nextInt(DEFUALT_ICONS.size());
            ico = DEFUALT_ICONS.get(idx);
        }catch (Exception e){
            LoggerUtils.error.error("随机头像异常,exception={}",e.getMessage());
        }
        return ico;
    }
}
