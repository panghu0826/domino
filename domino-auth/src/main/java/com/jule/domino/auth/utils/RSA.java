/**
 * 
 */
package com.jule.domino.auth.utils;

import lombok.extern.slf4j.Slf4j;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author issuser
 */
@Slf4j
public class RSA
{

    /**
     * SIGN_ALGORITHMS
     */
    public static final String SIGN_ALGORITHMS = "SHA1WithRSA";
    public static final String SIGN_ALGORITHMS256 = "SHA256WithRSA";

   
    public static String sign(String content, String privateKey, String signtype)
    {
        String charset = "utf-8";
        try
        {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decode(privateKey));
            KeyFactory keyf = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyf.generatePrivate(priPKCS8);

            java.security.Signature signature = null;
            if ("RSA256".equals(signtype)) {
                signature = java.security.Signature.getInstance(SIGN_ALGORITHMS256);
            } else {
                signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
            }

            signature.initSign(priKey);
            signature.update(content.getBytes(charset));

            byte[] signed = signature.sign();

            return Base64.encode(signed);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(),e);
        }

        return "";
    }

    public static boolean rsaDoCheck(Map<String, Object> params, String sign, String publicKey, String signtype)
    {
        String content = RSA.getSignData(params);
        return RSA.doCheck(content, sign, publicKey, signtype);
    }

    public static boolean doCheck(String content, String sign, String publicKey, String signtype)
    {
        try
        {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.decode(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

            java.security.Signature signature = null;
            if ("RSA256".equals(signtype)) {
                signature = java.security.Signature.getInstance(SIGN_ALGORITHMS256);
            } else {
                signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
            }

            signature.initVerify(pubKey);
            signature.update(content.getBytes("utf-8"));

            boolean bverify = signature.verify(Base64.decode(sign));
            return bverify;

        }
        catch (Exception e)
        {
            log.error(e.getMessage(),e);
        }

        return false;
    }

  
    public static String getSignData(Map<String, Object> params)
    {
        StringBuffer content = new StringBuffer();

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++)
        {
            String key = String.valueOf( keys.get(i));
            if ("sign".equals(key) || "signType".equals(key))
            {
                continue;
            }
            String value = String.valueOf(params.get(key));
            if (value != null )
            {
                content.append((i == 0 ? "" : "&") + key + "=" + value);
            }
            else
            {
                content.append((i == 0 ? "" : "&") + key + "=");
            }

        }
        return content.toString();
    }
    
    
    public static String getNoSortSignData(Map<String, Object> params)
    {
        StringBuffer content = new StringBuffer();

        List<String> keys = new ArrayList<String>(params.keySet());

        for (int i = 0; i < keys.size(); i++)
        {
            String key = (String) keys.get(i);
            if ("sign".equals(key))
            {
                continue;
            }
            String value = (String) params.get(key);
            if (value != null)
            {
                content.append((i == 0 ? "" : "&") + key + "=" + value);
            }
            else
            {
                content.append((i == 0 ? "" : "&") + key + "=");
            }

        }
        return content.toString();
    }

   

}
