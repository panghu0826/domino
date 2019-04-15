package com.jule.domino.auth.model;


import com.alibaba.fastjson.JSONObject;
import com.jule.core.jedis.JedisPoolWrap;

import com.jule.core.utils.HttpsUtil;
import com.jule.core.utils.MD5Util;
import com.jule.domino.base.enums.RedisChannel;
import com.jule.domino.base.enums.RedisConst;
import io.netty.channel.ChannelHandlerContext;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *@author xujian
 */
public abstract class BaseAction implements IAction {

    protected static final Logger logger = LoggerFactory.getLogger(BaseAction.class);

    public static final String ORDER_CACHE_PREFOX = "pay_order_prefix";

    private static final String public_key = "CE4239248A82C5C88FA7AB7B7841F274";

    @Override
    public void handleGet(ChannelHandlerContext ctx, Map<String, List<String>> parameter, boolean isKeepAlive) throws Exception {
        ctx.close();
    }

    @Override
    public void handlePost(ChannelHandlerContext ctx, byte[] payload, boolean isKeepAlive) throws Exception {
        ctx.close();
    }

    /**
     * 通知游戏服,更新玩家牌桌游戏数据
     * @param userId
     *                  玩家ID
     * @param money
     *                  更新数量
     * @return
     *                  true-更新成功
     */
    public static boolean noticeGame(String userId, double money, int addmoney){
        //请求参数
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("userId", userId));
        list.add(new BasicNameValuePair("money", String.valueOf(addmoney)));
        list.add(new BasicNameValuePair("sign", MD5Util.encodeByMD5(userId+money+public_key)));

        //请求地址
        String restUrl = JedisPoolWrap.getInstance().get(RedisConst.USER_LOGIN_GAME_URL.getProfix());
        String postUrl = "http://" + restUrl + "/api/game/updateChips";

        logger.info(MessageFormat.format("玩家uid={0}开始通知桌面刷新筹码money={1},posturl={2}",
                userId,money,postUrl));

        //发起请求、result - 0失败  1成功
        String result = HttpsUtil.doPostForm(postUrl,list,false);
        logger.info(MessageFormat.format("玩家uid={0}筹码刷新结果result={1}",userId,result));

        //通知dispacher更新玩家信息
        JSONObject obj = new JSONObject();
        obj.put("userId", userId);
        obj.put("money", money);
        obj.put("addmoney", addmoney);
        JedisPoolWrap.getInstance().pub(obj.toString(), RedisChannel.PAY_NOTICE.getChannelName());
        logger.info(MessageFormat.format("玩家uid={0}发布订阅通知,刷新显示筹码chips={1}",userId,result));
        return true;
    }

}
