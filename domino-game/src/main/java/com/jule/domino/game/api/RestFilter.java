package com.jule.domino.game.api;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 拦截器
 *
 * @author
 *
 * @since 2018/9/6 11:00
 */
@Provider
public class RestFilter implements ContainerRequestFilter {

    protected final static Logger logger = LoggerFactory.getLogger(RestFilter.class);

    //防sql注入
    private static String reg = "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|(\\b(select|update|union|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)";

    private static Pattern sqlPattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        UriInfo uriInfo = ctx.getUriInfo();
        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();

        for (String key : params.keySet()){
            List<String> values = params.get(key);
            for (String val : values){
                if (!validateXSS(val)){
                    logger.info("包含xss非法字符串");
                    ctx.abortWith(Response.status(500).build());
                }

                if (!validateChars(val)){
                    logger.info("包含sql注入非法字符串");
                    ctx.abortWith(Response.status(500).build());
                }
            }
        }
    }

    /**
     * 防止xss攻击
     * @param chars
     * @return
     */
    private boolean validateXSS(String chars){
        if (StringUtils.isEmpty(chars)){
            return true;
        }

        if (chars.contains("<") || chars.contains(">") || chars.contains("\\(") || chars.contains("\\)")
                || chars.contains("eval\\((.*)\\)") || chars.contains("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']")
                || chars.contains("script") ){
            logger.error("参数未能通过XSS过滤器：char={}" , chars);
            return false;
        }

        return true;
    }

    /**
     * 防止sql注入
     * @param chars
     * @return
     */
    private boolean validateChars(String chars){
        if (sqlPattern.matcher(chars).find())
        {
            logger.error("参数未能通过过滤器：char={}" , chars);
            return false;
        }
        return true;
    }
}
