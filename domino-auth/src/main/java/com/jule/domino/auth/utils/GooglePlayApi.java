package com.jule.domino.auth.utils;

import com.alibaba.fastjson.JSONObject;
import com.jule.domino.auth.config.Config;
import com.jule.core.utils.HttpsUtil;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @xujian 2018-01-31
 * google play 支付相关类需要维护access_token生存周期
 */
public final class GooglePlayApi {
    private static final Logger logger = LoggerFactory.getLogger(GooglePlayApi.class);
    private final static String apiUrl = "https://www.googleapis.com/androidpublisher/v2/applications/%s/purchases/products/%s/tokens/%s?access_token=%s";

    /**
     * {
     * "kind": "androidpublisher#productPurchase",
     * "purchaseTimeMillis": long,
     * "purchaseState": integer,
     * "consumptionState": integer,
     * "developerPayload": string,
     * "orderId": string,
     * "purchaseType": integer
     * }
     *
     * @param packageName
     * @param productId
     * @param token
     * @return
     */
    public static JSONObject verify(String packageName, String productId, String token) {
        try {
            String accessToken = GoogleAccessToken.getToken();
            logger.info("access token = "+accessToken);
            String api = String.format(apiUrl, packageName, productId, token, URLEncoder.encode(accessToken));
            String ret = HttpsUtil.doGet(api, true);
            logger.info("get ret = "+ret);
            if (ret != null) {
                return JSONObject.parseObject(ret);
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * access_token holder
     * org.egret.java.VDD
     */
    private static class GoogleAccessToken {
        private static final Logger logger = LoggerFactory.getLogger(GoogleAccessToken.class);
        private static String token;
        private static long endTime;
        private final static String apiUrl = "https://accounts.google.com/o/oauth2/token";

        public static void main( String[] args ) {
            String client_id = "374281221836-i968ngr0817rg2m4fpoj8mo2qs7d6qp6.apps.googleusercontent.com";
            String client_secret = "DkhcE7jgHoV0Tb-q5Cd_7OvW";
            String refresh_token = "1/fLiKS60HlqN8oN7YcHqDIWK11DDn6vS7IxZDP4-fbKEwyEc9dlFbfw0pJ4GJXb_K";

            List<NameValuePair> list = new ArrayList<>();
            list.add(new BasicNameValuePair("grant_type", "refresh_token"));
            list.add(new BasicNameValuePair("client_id", client_id));
            list.add(new BasicNameValuePair("client_secret", client_secret));
            list.add(new BasicNameValuePair("refresh_token", refresh_token));
            String ret = HttpsUtil.doPostForm(apiUrl, list, Config.PROXY);
            logger.info(ret);
        }

        /**
         * @return
         */
        public static String getToken() {
            long now = System.currentTimeMillis();
            if (endTime == 0l || endTime - now < 20 * 60 * 1000) {
                //提前20分钟刷token
                List<NameValuePair> list = new ArrayList<>();
                list.add(new BasicNameValuePair("grant_type", "refresh_token"));
                list.add(new BasicNameValuePair("client_id", Config.GOOGLE_CLIENT_ID));
                list.add(new BasicNameValuePair("client_secret", Config.GOOGLE_CLIENT_SECRET));
                list.add(new BasicNameValuePair("refresh_token", Config.GOOGLE_CLIENT_REFRESH_TOKEN));
                String ret = HttpsUtil.doPostForm(apiUrl, list, Config.PROXY);
                logger.info("get token = " +ret);
                if (ret != null) {
                    JSONObject jsonObject = JSONObject.parseObject(ret);
                    //更新到期时间点
                    endTime = System.currentTimeMillis() + jsonObject.getIntValue("expires_in") * 1000l;
                    token = jsonObject.getString("access_token");
                    logger.info("google refresh token->" + token);
                }
            }
            return token;
        }

        public static boolean verifyOrder(String orderId,String purchaseData, String sign){
            //验证订单数据
            if (!RSASignature.doCheck(purchaseData,sign,Config.GOOGLE_PAY_PUBLIC_KEY)){
                return false;
            }

            return true;
        }

    }
}
