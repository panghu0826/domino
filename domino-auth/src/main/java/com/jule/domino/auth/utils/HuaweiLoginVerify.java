package com.jule.domino.auth.utils;

import com.alibaba.fastjson.JSON;
import com.jule.domino.auth.config.Config;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.Asserts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.MessageFormat;
import java.util.*;

/**
 * 华为登录验证
 *
 * @author
 *
 * @since 2018/8/13 19:17
 */
public class HuaweiLoginVerify {

    private static final Logger logger = LoggerFactory.getLogger(HuaweiLoginVerify.class);

    // 游戏服务器提供给CP签名接口URL
    public static String requestUri = "https://gss-cn.game.hicloud.com/gameservice/api/gbClientApi";


    private static final String RETURN_CODE_SUCCEED = "0";

    private static final int HTTP_RESPONSE_STATUS_CODE_OK = 200;

    /**
     * @param requestParams 请求参数对
     * @param cpAuthKey     CP侧签名私钥
     */
    public static boolean callGameService(Map<String, String> requestParams,final String cpAuthKey) {
        requestParams.put("cpSign", generateCPSign(requestParams, cpAuthKey));

        // 响应消息中返回参数
        Map<String, Object> responseParamPairs = doPost(requestUri, requestParams);

        if (responseParamPairs.isEmpty()) {
            logger.error("华为登录验证没有返回参数");
            return false;
        } else {
            if (RETURN_CODE_SUCCEED.equalsIgnoreCase(getString("rtnCode", responseParamPairs))) {
                logger.info("rtnCode={},ts={},rtnSign={}" ,getString("rtnCode", responseParamPairs),getString("ts", responseParamPairs),getString("rtnSign", responseParamPairs));

                String form = MessageFormat.format("rtnCode={0}&ts={1}",getString("rtnCode", responseParamPairs), getString("ts", responseParamPairs));
                if (!verify(form, Config.HUAWEI_SIGN_PUBLIC_KEY, getString("rtnSign", responseParamPairs))){
                    logger.error("华为登录验证返回参数非法");
                    return false;
                }

                return true;
            } else {
                logger.info("rtnCode:{}, errMsg:{}", getString("rtnCode", responseParamPairs),getString("errMsg", responseParamPairs));
                return false;
            }
        }
    }

    private static String getString( String key, Map<String, Object> responseParamPairs ) {
        Asserts.notNull(responseParamPairs, "responseParamPairs");
        Object value = responseParamPairs.get(key);

        if (value == null) {
            return null;
        }

        return value.toString();
    }

    public static Map<String, Object> doPost( String url, Map<String, String> paramaters ) {
        HttpPost httpReq = new HttpPost(url);

        // 创建默认的httpClient实例.
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            if (paramaters != null) {
                List<NameValuePair> paramPairs = new ArrayList<NameValuePair>();

                BasicNameValuePair bnv;

                for (Map.Entry<String, String> entry : paramaters.entrySet()) {
                    bnv = new BasicNameValuePair(entry.getKey(), entry.getValue());
                    paramPairs.add(bnv);
                }

                httpReq.setEntity(new UrlEncodedFormEntity(paramPairs, "UTF-8"));
            }

            Map<String, Object> responseParamPairs = new HashMap<>();

            HttpResponse resp = httpclient.execute(httpReq);

            if (null != resp && HTTP_RESPONSE_STATUS_CODE_OK == resp.getStatusLine().getStatusCode()) {
                responseParamPairs = JSON.parseObject(EntityUtils.toString(resp.getEntity()));
            }

            return responseParamPairs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String sign( byte[] data, String privateKey ) {
        try {
            byte[] e = org.apache.commons.codec.binary.Base64.decodeBase64(privateKey);
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(e);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
            Signature signature = Signature.getInstance("SHA256WithRSA");
            signature.initSign(privateK);
            signature.update(data);
            return org.apache.commons.codec.binary.Base64.encodeBase64String(signature.sign());
        } catch (Exception var) {
            logger.error("SignUtil.sign error." + var);
            return "";
        }
    }


    /**
     * 根据参数Map构造排序好的参数串
     *
     * @param params
     * @return
     */
    private static String format( Map<String, String> params) {
        StringBuffer base = new StringBuffer();
        Map<String, String> tempMap = new TreeMap<String, String>(params);

        // 获取计算nsp_key的基础串
        try {
            for (Map.Entry<String, String> entry : tempMap.entrySet()) {
                String k = entry.getKey();
                String v = entry.getValue();
                base.append(k).append("=").append(URLEncoder.encode(v, "UTF-8")).append("&");
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("Encode parameters failed.");
            e.printStackTrace();
        }

        String body = base.toString().substring(0, base.toString().length() - 1);
        // 空格和星号转义
        body = body.replaceAll("\\+", "%20").replaceAll("\\*", "%2A");

        return body;
    }


    public static String generateCPSign( Map<String, String> requestParams, final String cpAuthKey ) {
        // 对消息体中查询字符串按字典序排序并且进行URLCode编码
        String baseStr = format(requestParams);

        // 用CP侧签名私钥对上述编码后的请求字符串进行签名
        String cpSign = sign(baseStr.getBytes(Charset.forName("UTF-8")), cpAuthKey);

        return cpSign;
    }

    /**
     * 校验数字签名
     *
     * @param data      加密的数据，格式为：a=x&b=y  example：rtnCode=0&ts=1500552495471
     * @param publicKey RSA 签名公钥(BASE64编码)
     * @param sign      数字签名：游戏服务端生成的RSA签名
     * @return true : 验证成功; false:验证失败
     */
    public static boolean verify( String data, String publicKey, String sign ) {
        Asserts.notNull(data, "Encrypt data cant not be null.");
        Asserts.notNull(publicKey, "Public key can not be null.");
        Asserts.notNull(sign, "Sign can not be null.");
        try {
            byte[] keyBytes = org.apache.commons.codec.binary.Base64.decodeBase64(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicK = keyFactory.generatePublic(keySpec);
            Signature signature = Signature.getInstance("SHA256WithRSA");
            signature.initVerify(publicK);
            signature.update(data.getBytes("UTF-8"));
            return signature.verify(org.apache.commons.codec.binary.Base64.decodeBase64(sign));
        } catch (Exception e) {
            logger.error("SignUtil.verify error." + e);
            return false;
        }
    }

}
