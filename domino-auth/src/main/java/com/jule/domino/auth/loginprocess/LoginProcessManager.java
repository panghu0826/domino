package com.jule.domino.auth.loginprocess;

import com.jule.domino.auth.utils.PackageUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author xujian
 * 登陆脚本管理器 单列实现
 */
@Slf4j
public class LoginProcessManager {

    private static final Map<String, ILoginProcess> PROCESS_MAP = new ConcurrentSkipListMap<>();

    private static class SingletonHolder {
        protected static final LoginProcessManager instance = new LoginProcessManager();
    }

    public static final LoginProcessManager getInstance() {
        return LoginProcessManager.SingletonHolder.instance;
    }

    private LoginProcessManager() {
        try {
            PackageUtils.getClassName("com.jule.domino.auth.loginprocess.impl").forEach(s -> {
                        try {
                            Class<?> clazz = LoginProcessManager.class.getClassLoader().loadClass(s);
                            try {
                                PROCESS_MAP.put(clazz.getAnnotation(ChannelId.class).name(), (ILoginProcess) clazz.newInstance());
                                log.info(String.format("load->%12s login process->%s", clazz.getAnnotation(ChannelId.class).name(), clazz.getName()));
                            } catch (IllegalAccessException e) {
                                log.error(e.getMessage(),e);
                            } catch (InstantiationException e) {
                                log.error(e.getMessage(),e);
                            }
                        } catch (ClassNotFoundException e) {
                            log.error(e.getMessage(),e);
                        }
                    }
            );

        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

    /**
     * 获取渠道处理类
     *
     * @param channelId
     * @return
     */
    public ILoginProcess getLoginProcess(String channelId) {
        log.debug("登陆渠道处理类-------------------------------："+channelId);
        return PROCESS_MAP.get(channelId);
    }
}
