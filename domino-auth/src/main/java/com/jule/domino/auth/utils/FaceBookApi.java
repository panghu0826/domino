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

public final class FaceBookApi {
    private static final Logger logger = LoggerFactory.getLogger(PackageUtils.class);

    /**
     * 获取用户信息
     *
     * @param accessToken
     */
    public static JSONObject getFaceBookUserInfo(String accessToken) {
        String args = "https://graph.facebook.com/me?fields=" + URLEncoder.encode("id,name,picture") + "&access_token=" + URLEncoder.encode(accessToken);
//        String args = "https://graph.facebook.com/me";
        logger.info("->" + args);

//        Map<java.lang.String, java.lang.String> param = new HashMap<java.lang.String, java.lang.String>();
//        param.put("fields", "id,name,picture");
//        param.put("access_token", accessToken);

        String ret = HttpsUtil.doGet(args, true);

        if (ret != null) {
            return JSONObject.parseObject(ret);
        }

        return null;
    }


    /**
     * 获取用户信息
     * curl -i -X GET \
     * "https://graph.facebook.com/v2.11/1178546678942295?fields=country%2Ccreated_time%2Cid%2Citems%2Crequest_id%2Ctest%2Cuser%2Cactions&access_token=134739547330835%7Cva2foJGEJ68lZcM8U7Xek3LoAgs"
     *
     * @param paymentId
     */
    public static JSONObject getFaceBookPaymentInfo(String paymentId) {
        String args = "https://graph.facebook.com/v2.11/" + paymentId + "?fields=" + URLEncoder.encode("country,created_time,id,items,request_id,test,user,actions") + "&access_token=" + URLEncoder.encode(Config.APP_TOKEN);
        logger.info("->" + args);

        String ret = HttpsUtil.doGet(args, true);
        if (ret != null) {
            return JSONObject.parseObject(ret);
        }

        return null;
    }

    public static void main(String[] args) {
        //{"id":"107312763409568","name":"Jian Xu","picture":{"data":{"height":50,"is_silhouette":true,"url":"https://scontent.xx.fbcdn.net/v/t1.0-1/c15.0.50.50/p50x50/10354686_10150004552801856_220367501106153455_n.jpg?oh=baf3745408876788393e9ca2b7e1dc94&oe=5AEBF02F","width":50}}}
        //JSONObject verify = getFaceBookPaymentInfo("1435898349854019");
        //System.out.println(verify.toString());
        // System.out.println("" + isComplete(verify));
        String js = "{\"object\":\"payments\",\"entry\":[{\"id\":\"1178546678942295\",\"time\":1516681971,\"changed_fields\":[\"actions\"]}]}";
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("grant_type", "authorization_code"));
        list.add(new BasicNameValuePair("code", "4/wHCQQFzS0rjih5-qgLQfYtTnN8hnVkxO5XgG8uxlMcM#"));
        list.add(new BasicNameValuePair("client_id", "110520377179-vral5u6tmuk5t3q29to6nh1qr1j310i0.apps.googleusercontent.com"));
        list.add(new BasicNameValuePair("client_secret", "3uw7UVRrKPiZLBW-BESgh6zU"));
        list.add(new BasicNameValuePair("redirect_uri", "https://dominohappy.joloplay.net/1864348663577642/policy.html"));
        System.out.println(HttpsUtil.doPostForm("https://accounts.google.com/o/oauth2/token", list,true));


//        String args2 = "https://graph.facebook.com/v2.11/" + "paymentId" + "?fields=country,created_time,id,items,request_id,test,user,actions&access_token=134739547330835|va2foJGEJ68lZcM8U7Xek3LoAgs";
//        System.out.println(URLEncoder.encode(args2));
    }

}
