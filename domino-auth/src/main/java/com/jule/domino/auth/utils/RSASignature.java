package com.jule.domino.auth.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


/**
 * google支付非对称加密较验
 * 
 * @author ran
 */
public class RSASignature {

	private static final Logger logger = LoggerFactory.getLogger(RSASignature.class);

	/**
	 * 签名算法
	 */
	public static final String SIGN_ALGORITHMS = "SHA1WithRSA";

	/**
	 * RSA签名
	 * 
	 * @param content
	 *            待签名数据
	 * @param privateKey
	 *            商户私钥
	 * @param encode
	 *            字符集编码
	 * @return 签名值
	 */
	public static String sign(String content, String privateKey, String encode) {
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
					Base64.getDecoder().decode(privateKey));

			KeyFactory keyf = KeyFactory.getInstance("RSA");
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);

			java.security.Signature signature = java.security.Signature
					.getInstance(SIGN_ALGORITHMS);

			signature.initSign(priKey);
			signature.update(content.getBytes(encode));

			byte[] signed = signature.sign();
			byte[] encodeByte = Base64.getEncoder().encode(signed);

			return String.valueOf(encodeByte);
		} catch (Exception e) {
			logger.error("sign error", e);
		}

		return null;
	}

	public static String sign(String content, String privateKey) {
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
					Base64.getDecoder().decode(privateKey));
			KeyFactory keyf = KeyFactory.getInstance("RSA");
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);
			java.security.Signature signature = java.security.Signature
					.getInstance(SIGN_ALGORITHMS);
			signature.initSign(priKey);
			signature.update(content.getBytes());
			byte[] signed = signature.sign();
			byte[] encodeByte = Base64.getEncoder().encode(signed);
			return String.valueOf(encodeByte);
		} catch (Exception e) {
			logger.error("sign error", e);
		}
		return null;
	}

	/**
	 * RSA验签名检查
	 * 
	 * @param content
	 *            待签名数据
	 * @param sign
	 *            签名值
	 * @param publicKey
	 *            分配给开发商公钥
	 * @param encode
	 *            字符集编码
	 * @return 布尔值
	 */
	public static boolean doCheck(String content, String sign,
			String publicKey, String encode) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			byte[] encodedKey = Base64.getDecoder().decode(publicKey);
			PublicKey pubKey = keyFactory
					.generatePublic(new X509EncodedKeySpec(encodedKey));

			java.security.Signature signature = java.security.Signature
					.getInstance(SIGN_ALGORITHMS);

			signature.initVerify(pubKey);
			signature.update(content.getBytes(encode));

			boolean bverify = signature.verify(Base64.getDecoder().decode(sign));
			return bverify;

		} catch (Exception e) {
			logger.error("check error", e);
		}

		return false;
	}

	/**
	 * 校验sign
	 * @param {json} InappPurchaseData GooglePlay 充值返回值
	 * @param {String} inappDataSignature GooglePlay 充值返回值
	 * @param publicKey
	 * @return
	 */
	public static boolean doCheck(String inappPurchaseData, String inappDataSignature, String publicKey) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			byte[] encodedKey = Base64.getDecoder().decode(publicKey);
			PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
			java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

			signature.initVerify(pubKey);
			signature.update(inappPurchaseData.getBytes());

			boolean bverify = signature.verify(Base64.getDecoder().decode(inappDataSignature));
			return bverify;

		} catch (Exception e) {
			logger.error("check error", e);
		}
		return false;
	}
}
