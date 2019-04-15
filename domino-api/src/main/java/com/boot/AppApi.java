package com.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 *
 * SpringBoot 启动类
 *
 * @author
 *
 * @since 2018/7/18 18:31
 *
 */
@EnableAutoConfiguration
@SpringBootApplication
public class AppApi {

    public static void main( String[] args ) {
        SpringApplication.run(AppApi.class, args);
    }

}
