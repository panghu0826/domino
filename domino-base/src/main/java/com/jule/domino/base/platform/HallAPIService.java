package com.jule.domino.base.platform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jule.core.utils.HttpsUtil;
import com.jule.domino.base.platform.bean.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大厅接口服务
 * @author
 * @since 2018/11/26 15:50
 */
@Slf4j
public class HallAPIService {
    //单例
    public static final HallAPIService OBJ = new HallAPIService();

    private static final Gson gson = new GsonBuilder().serializeNulls().create();

    private String ACCOUNT_URL = "http://account.api.com:9001/UserService/";


    /**
     * 绑定账号中心地址
     * @param host
     * @return
     */
    public HallAPIService bindAccount(String host){
        this.ACCOUNT_URL = host;
        return this;
    }

    /**
     * 获取用户信息
     * @param openId
     * @return
     */
    public PlatUserBean getById(String openId){
        try {
            String url = this.ACCOUNT_URL+"getByID?id="+openId;
            log.debug("调用接口method=getById 请求地址url={}", url);

            String ret = HttpsUtil.doGet(url, false);
            log.debug("调用接口method=getById 返回ret = {}", ret);

            if (StringUtils.isEmpty(ret)){
                log.error("调用接口method=getById 返回空, openId={}",openId);
                return null;
            }

            ApiRetBean retBean = gson.fromJson(ret, ApiRetBean.class);
            if (retBean == null){
                log.error("调用接口method=getById 返回ret={}, 反序列化失败",ret);
                return null;
            }

            if (retBean.getCode() != 0){
                log.error("调用接口method=getById 返回code != 0");
                return null;
            }

            GetUserRet user = gson.fromJson(retBean.getResult(), GetUserRet.class);
            if (user == null){
                log.error("调用接口method=getById 返回User_obj = null");
                return null;
            }

            return user.getUser_obj();
        }catch (Exception ex){
            log.error("解析接口数据异常",ex);
            return null;
        }

    }

    /**
     * 获取用户信息
     * @param token
     * @return
     */
    public PlatUserBean getByIdentity(String token){
        try {
            String url = this.ACCOUNT_URL+"getByIdentity?identity="+URLEncoder.encode(token,"utf-8");
            log.info("调用接口method=getByIdentity 请求地址url={}", url);

            String ret = HttpsUtil.doGet(url, false);
            log.info("调用接口method=getByIdentity 返回ret = {}", ret);

            if (StringUtils.isEmpty(ret)){
                log.error("调用接口method=getByIdentity 返回空, token={}",token);
                return null;
            }

            ApiRetBean retBean = gson.fromJson(ret, ApiRetBean.class);
            if (retBean == null){
                log.error("调用接口method=getByIdentity 返回ret={}, 反序列化失败",ret);
                return null;
            }

            if (retBean.getCode() != 0){
                log.error("调用接口method=getByIdentity 返回code != 0");
                return null;
            }

            GetUserRet user = gson.fromJson(retBean.getResult(), GetUserRet.class);
            if (user == null){
                log.error("调用接口method=getByIdentity 返回User_obj = null");
                return null;
            }

            return user.getUser_obj();
        }catch (Exception ex){
            log.error("解析接口数据异常",ex);
            return null;
        }
    }

    /**
     * 修改玩家货币
     * @param bean
     * @return
     */
    public double modifyUserAccount(ModifyReqBean bean){
        try {
            String url = this.ACCOUNT_URL+"modifyUserAccount?id=%s&behavior=%s&gold=%s&valid_gold=%s&order_id=%s&game_id=%s&room_id=%s&seat_id=%s&round_id=%s&comment=%s";
            //构建参数
            url = String.format(url,
                    bean.getUser_id(), bean.getBehavior(), bean.getGold(), bean.getValid_gold(),
                    bean.getOrder_id(), bean.getGame_id(), bean.getRoom_id(),
                    bean.getSeat_id(), bean.getRound_id(), bean.getComment());
            log.info("调用接口method=modifyUserAccount,url={}",url);

            //发送请求
            String ret = HttpsUtil.doGet(url, false);
            log.info("调用接口method=modifyUserAccount 返回ret = {}", ret);

            if (StringUtils.isEmpty(ret)){
                log.error("调用接口method=modifyUserAccount 返回空");
                return 0;
            }

            ApiRetBean retBean = gson.fromJson(ret, ApiRetBean.class);
            if (retBean == null){
                log.error("调用接口method=modifyUserAccount 返回ret={}, 反序列化失败",ret);
                return 0;
            }

            if (retBean.getCode() != 0){
                log.error("调用接口method=modifyUserAccount 返回code != 0");
                return 0;
            }

            ModifyRetBean modifyRetBean = gson.fromJson(retBean.getResult(), ModifyRetBean.class);
            if (modifyRetBean == null){
                log.error("调用接口method=modifyUserAccount 返回result = null");
                return 0;
            }

            Balance balance = modifyRetBean.getBalance();
            if (balance == null){
                return 0;
            }

            return balance.getGoldDouble();
        }catch (Exception ex){
            log.error("解析接口数据异常",ex);
            return 0;
        }
    }

    /**
     * 推送游戏记录
     * @param bean
     */
    public void updateGameRecord(RecordReqBean bean){
        try {
            String url = this.ACCOUNT_URL+"updateGameRecord";
            String records = gson.toJson(bean.getRecords());
            log.info("调用接口method=updateGameRecord url：{}",url);
            log.info("调用接口method=updateGameRecord records：{}",records);

            //构建参数
            List<NameValuePair> array = new ArrayList<>();
            array.add(new BasicNameValuePair("records", records));

            //发送请求
            String ret = HttpsUtil.doPostForm(url,array,false);
            log.debug("调用接口method=updateGameRecord,返回={}", ret);
        }catch (Exception ex){
            log.error("解析接口数据异常",ex);
        }
    }


    /**
     * 远程update Icon
     * @param openId
     * @param icon
     */
    public void updateIcon(String openId, String icon){
        String url = this.ACCOUNT_URL+"updateIcon";
        log.debug("调用接口method=updateIcon url：{}",url);
        log.info("调用接口method=updateIcon openId：{}, icon:{}",openId,icon);

        //构建参数
        List<NameValuePair> array = new ArrayList<>();
        array.add(new BasicNameValuePair("id", openId));
        array.add(new BasicNameValuePair("icon", icon));

        //发送请求
        String ret = HttpsUtil.doPostForm(url,array,false);
        log.info("调用接口method=updateIcon,返回={}", ret);
    }

    /**
     * 推送游戏记录
     * @param bean
     */
    public Map<String, UserModifyBean> modifyUserAccountAndUpdateGameRecord(List<ModifyAndRecord> bean){
        //返回对象
        Map<String, UserModifyBean> map = new HashMap<>();
        try {
            String url = this.ACCOUNT_URL+"modifyUserAccountAndUpdateGameRecord";
            String records = gson.toJson(bean);
            log.info("调用接口method=modifyUserAccountAndUpdateGameRecord url：{}",url);
            log.info("调用接口method=modifyUserAccountAndUpdateGameRecord records：{}",records);

            //构建参数
            List<NameValuePair> array = new ArrayList<>();
            array.add(new BasicNameValuePair("data", records));

            //发送请求
            String ret = HttpsUtil.doPostForm(url,array,false);
            log.info("调用接口method=modifyUserAccountAndUpdateGameRecord,返回={}", ret);



            ModifyAndRecordRet retBean = gson.fromJson(ret, ModifyAndRecordRet.class);
            if (retBean == null){
                log.error("调用接口method=modifyUserAccountAndUpdateGameRecord,retBean=null");
                return map;
            }

            if (retBean.getCode() != 0){
                log.error("调用接口method=modifyUserAccountAndUpdateGameRecord,code={}",retBean.getCode());
                return map;
            }

            if (retBean.getResult() == null || retBean.getResult().size() == 0){
                log.error("调用接口method=modifyUserAccountAndUpdateGameRecord,result=null");
                return map;
            }
            return retBean.getResult();
        }catch (Exception ex){
            log.error("解析接口数据异常",ex);
        }
        return map;
    }

    public List<PlayerRecords> getGameRecord(String openId, String gameId, String limit){
        List<PlayerRecords> list = new ArrayList<>();
        try {
            String url = this.ACCOUNT_URL + "getGameRecord";
            log.info("调用接口method=getGameRecord params：url={},openid={},gameid={},limit={}", url,openId,gameId,limit);

            //构建参数
            List<NameValuePair> array = new ArrayList<>();
            array.add(new BasicNameValuePair("id", openId));
            array.add(new BasicNameValuePair("game_id", gameId));
            array.add(new BasicNameValuePair("limit", limit));

            //发送请求
            String ret = HttpsUtil.doPostForm(url, array, false);
            log.info("调用接口method=getGameRecord,返回={}", ret);

            PlayerRecordsRet retBean = gson.fromJson(ret, PlayerRecordsRet.class);
            if (retBean == null){
                log.error("调用接口method=getGameRecord 返回ret={}, 反序列化失败",ret);
                return list;
            }

            if (retBean.getCode() != 0){
                log.error("调用接口method=getGameRecord 返回code != 0");
                return list;
            }

            if (retBean.getResult() == null || retBean.getResult().size() == 0){
                log.error("调用接口method=getGameRecord 返回result = null");
                return list;
            }

            return retBean.getResult();
        }catch (Exception ex){
            log.error("调用接口method=getGameRecord 异常 ex = {}", ex);
        }
        return list;
    }

}
