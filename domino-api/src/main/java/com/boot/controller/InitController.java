package com.boot.controller;

import com.boot.config.AppConfig;
import com.jule.domino.base.service.ItemServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * 初始化服务
 *
 * @author
 *
 * @since 2018/7/19 15:07
 *
 */
@RestController
public class InitController extends AbstractController{

    @Autowired
    private AppConfig appConfig;

    @PostConstruct
    public void init(){
        log.info("初始化启动");
        //TODO 需要初始化加載項

        ItemServer.OBJ.init(appConfig.getItemServer(), appConfig.getGameId());
        
    }

}
