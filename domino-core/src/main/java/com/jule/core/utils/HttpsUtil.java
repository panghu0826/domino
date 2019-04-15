package com.jule.core.utils;

import com.google.common.base.CharMatcher;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;


public class HttpsUtil {

    public static Logger log = LoggerFactory.getLogger(HttpsUtil.class);

    private static PoolingHttpClientConnectionManager connMgr;
    private static RequestConfig requestConfig;
    private static final int MAX_TIMEOUT = 7000;

    static {
        // 设置连接池  
        connMgr = new PoolingHttpClientConnectionManager();
        // 设置连接池大小  
        connMgr.setMaxTotal(100);
        connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());
        RequestConfig.Builder configBuilder = RequestConfig.custom();
        // 设置连接超时  
        configBuilder.setConnectTimeout(MAX_TIMEOUT);
        // 设置读取超时  
        configBuilder.setSocketTimeout(MAX_TIMEOUT);
        // 设置从连接池获取连接实例的超时  
        configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
        // 在提交请求之前 测试连接是否可用  
        //configBuilder.setStaleConnectionCheckEnabled(true);  
        requestConfig = configBuilder.build();
    }


    /**
     * 发送 GET 请求（HTTP），K-V形式
     *
     * @param url
     * @return
     */
    public static String doGet(String url, boolean isHttps) {
        HttpResponse response = null;

        String result = null;

        CloseableHttpClient httpclient = null;
        if (isHttps) {
            httpclient = createSSLClientDefault();
        } else {
            httpclient = HttpClients.createDefault();
        }
        try {
            HttpGet httpGet = new HttpGet(url);
//            HttpHost proxy = new HttpHost("192.168.0.14", 1080);
//            httpGet.setConfig(RequestConfig.custom().setProxy(proxy).build());

            httpGet.setHeader("http.protocol.content-charset", "UTF-8");
            response = httpclient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, "UTF-8");
            }
            if (statusCode != HttpStatus.SC_OK) {
                log.warn("call url->" + url + "fail reason->" + statusCode + ",body->" + result);
                return null;
            }

        } catch (IOException e) {
            log.error(e.getMessage(),e);
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    log.error(e.getMessage(),e);
                }
            }
        }
        return result;
    }

    public static byte[] doPostProtoc(String url, byte[] array, boolean ssl) {
        CloseableHttpClient httpClient = null;
        if (ssl){
            //使用证书
            httpClient = HttpClients.createDefault();
        }else {
            //不使用证书、信任全部的证书
            httpClient = createSSLClientDefault();
        }

        HttpPost httpPost = new HttpPost(url);

        CloseableHttpResponse response = null;

        byte[] result = null;
        try {
            httpPost.setConfig(requestConfig);
            httpPost.setEntity(new ByteArrayEntity(array));

            response = httpClient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }

            HttpEntity httpEntity = response.getEntity();

            if (httpEntity == null) {
                return null;
            }

            result = EntityUtils.toByteArray(httpEntity);

        } catch (Exception e) {
            log.error("http post "+e.getMessage(), e);
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (Exception e) {
                    log.error("", e);
                }
            }

            try {
                httpClient.close();
            } catch (IOException e) {
                log.error(e.getMessage(),e);
            }
        }
        return result;
    }

    /**
     * 表单post
     * @param url
     * @param array
     * @return
     */
    public static String doPostForm(String url, List<NameValuePair> array,boolean needProxy) {

        CloseableHttpClient httpClient = createSSLClientDefault();//HttpClients.custom().setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();

        HttpPost httpPost = new HttpPost(url);
        if (needProxy){
            HttpHost proxy = new HttpHost("192.168.0.14", 1080);
            httpPost.setConfig(RequestConfig.custom().setProxy(proxy).build());
        }else {
            httpPost.setConfig(RequestConfig.custom().build());
        }

        CloseableHttpResponse response = null;

        String result = null;
        try {

            httpPost.setEntity(new UrlEncodedFormEntity(array));
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity httpEntity = response.getEntity();
            if(httpEntity != null){
                result = EntityUtils.toString(httpEntity);
            }
            if (statusCode != HttpStatus.SC_OK) {
                log.warn("call api url->" + url + ",body->" + result);
                return null;
            }

            //result = EntityUtils.toString(httpEntity);

        } catch (Exception e) {
            log.error("http post ", e);
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (Exception e) {
                    log.error("", e);
                }
            }

            try {
                httpClient.close();
            } catch (IOException e) {
                log.error(e.getMessage(),e);
            }
        }
        return result;
    }



    public static String doPostSslJSON(String url, String json) {

        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory()).setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();

        HttpPost httpPost = new HttpPost(url);

        CloseableHttpResponse response = null;

        String result = null;
        try {
            httpPost.setConfig(requestConfig);

            StringEntity stringEntity = new StringEntity(json, "application/json");

            httpPost.setEntity(stringEntity);

            response = httpClient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }

            HttpEntity httpEntity = response.getEntity();

            if (httpEntity == null) {
                return null;
            }

            result = EntityUtils.toString(httpEntity, "UTF-8");

        } catch (Exception e) {
            log.error(e.getMessage(),e);
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                }
            }
        }
        return result;
    }

    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLConnectionSocketFactory sslsf = null;
        try {

            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    return true;
                }

            }).build();

            sslsf = new SSLConnectionSocketFactory(sslContext);

        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return sslsf;
    }

    private static CloseableHttpClient createSSLClientDefault() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                //信任所有  
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return HttpClients.createDefault();
    }

    public static void main(String[] args) {
	/*String url = "https://accounts.google.com/o/oauth2/token";
	
	Map<String,String> paraMap = new HashMap<String,String>();
	paraMap.put("FacserviceID", "nfeiGC");
	String result = doGet(url,paraMap,true);//doPostSSL(url,paraMap);
	
	
	
	System.out.println(result);*/
	  /* String url = "https://www.googleapis.com/androidpublisher/v2/applications";
	   Map<String,String> param = new HashMap<String,String>();
	   param.put("a", "111");
	   param.put("b", "222");
	   param.put("c", "333");
	   String str = Joiner.on("&").skipNulls().join(param.entrySet());
	
	   url = Joiner.on("?").skipNulls().join(url,str);
	   
	   System.out.println(url);*/

        String strs = "%s/%s/purchases/products/%s/tokens/%s?access_token=%s";
        CharMatcher.noneOf(strs);
    }


}
