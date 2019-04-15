package com.jule.domino.gate.utils;

import com.alibaba.fastjson.JSONObject;
import com.jule.core.utils.HttpsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaceBookApi {
    private static final Logger logger = LoggerFactory.getLogger(FaceBookApi.class);
    private static final String url = "https://graph.facebook.com/me?fields=id,name,picture&access_token=";

    /**
     * 获取用户信息
     *
     * @param accessToken
     */
    public static JSONObject getFaceBookUserInfo(String accessToken) {
        String args = "https://graph.facebook.com/me?fields=id,name,picture&access_token=" + accessToken;
        logger.info("->" + args);

        String ret = HttpsUtil.doGet(args, true);

        if (ret != null) {
            return JSONObject.parseObject(ret);
        }

        return null;
    }

    public static void main(String[] args) {
        //{"id":"107312763409568","name":"Jian Xu","picture":{"data":{"height":50,"is_silhouette":true,"url":"https://scontent.xx.fbcdn.net/v/t1.0-1/c15.0.50.50/p50x50/10354686_10150004552801856_220367501106153455_n.jpg?oh=baf3745408876788393e9ca2b7e1dc94&oe=5AEBF02F","width":50}}}
        System.out.println(getFaceBookUserInfo("EAAYbxZC2kPZB0BANkLeOjtVvlGaX3rNhjfKLBesHJgQuOZB6oABe5koKlSsef9WtR6aBhVAbiQ3RJkEiXBzOedJL31Ht4QJ9RNWvitqMkYyldrEqYhlELLWq0hZBLH32UNEHK6NyPwc4cMsmzfQJvklfZBOBTT1gG4vvrCII6WqDzp7ZByLhzlqXPZCYnULb25I8krpQYGVnXcH1Un0l47r"));
    }

}
