package com.boot.fliter;

import com.boot.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Statement;

/**
 * 拦截器，用于验证和数据安全
 *
 * @author
 *
 * @since 2018/7/20 16:04
 *
 */
@Component
public class RequestInterceptor implements HandlerInterceptor{

    private static final Logger log = LoggerFactory.getLogger(RequestInterceptor.class);

    @Autowired
    private AppConfig config;

    @Override
    public boolean preHandle( HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //只允许指定ip访问服务器
        String remortIP = getRemortIP(request);
        if (config.getAccessip().equals(remortIP)){
            return true;
        }

        log.error("{} ip is mot access ， access ip is {}", remortIP, config.getAccessip());
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
    }

    private String getRemortIP(HttpServletRequest request) {
        if (request.getHeader("x-forwarded-for") == null) {
            return request.getRemoteAddr();
        }
        return request.getHeader("x-forwarded-for");
    }
}
