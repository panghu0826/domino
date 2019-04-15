package com.boot.fliter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author
 * @since 2018/7/20 16:15
 */
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    RequestInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //全部接口拦截
        registry.addInterceptor(interceptor).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
