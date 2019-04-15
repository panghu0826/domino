package com.jule.domino.auth.service;


import com.jule.domino.auth.model.IAction;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 全局单例
 *
 * @author 郭君伟
 */
@Slf4j
public class DispacherService {
    /**
     * url 处理类
     **/
    private static final Map<String, IAction> handlerMap = new HashMap<String, IAction>();

    private static class SingletonHolder {
        protected static final DispacherService instance = new DispacherService();
    }

    public static final DispacherService getInstance() {
        return DispacherService.SingletonHolder.instance;
    }

    private DispacherService() {
        try {
            initHandler();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 初始化Handler
     *
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    private void initHandler() throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle("uri");
        for (String key : bundle.keySet()) {
            handlerMap.put(key, (IAction) (Class.forName(bundle.getString(key)).newInstance()));
            log.info("map uri[" + key + "] -> " + bundle.getString(key));
        }
    }

    /**
     * @param uri
     * @return
     */
    public IAction getAction(String uri) {
        return handlerMap.get(uri);
    }

}
