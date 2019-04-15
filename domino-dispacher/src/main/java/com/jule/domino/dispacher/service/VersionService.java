package com.jule.domino.dispacher.service;


import com.jule.domino.dispacher.dao.DBUtil;
import com.jule.domino.dispacher.dao.bean.VersionConfigModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class VersionService {

    public static final VersionService OBJ = new VersionService();

    private static Map<String ,VersionConfigModel> _versionMap = new ConcurrentHashMap<>();

    public void init(){
        //30分钟定时刷新内存
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(()->loadFromDb(),0,60 * 30, TimeUnit.SECONDS);
    }

    /**
     * 初始化加载内存
     */
    private boolean loadFromDb() {
        log.debug("定时读取版本信息");
        List<VersionConfigModel> list = DBUtil.selectAllVersion();
        if (list == null || list.size() == 0){
            throw new Error("VersionConfigModel is null");
        }

        //清空内存
        _versionMap.clear();

        //重新加载
        list.forEach(e->_versionMap.put(e.getDown_platform(),e));
        return true;
    }

    /**
     * 判断是否可强更
     *
     * @param downPlatform
     * @return true可强更false不可强更
     */
    public String getDownPlatform(String downPlatform) {
        if (!StringUtils.isNotEmpty(downPlatform)) {
            log.error("judageVersion()，downPlatform:{}", downPlatform);
            return "";
        }
        VersionConfigModel version = _versionMap.get(downPlatform);

        if (version == null) {
            return "";
        }
        return version.getDownloadLink();
    }

    /**
     * 判断是否可强更
     *
     * @param version
     * @return true可强更false不可强更
     */
    public boolean judageVersion(String version, String downPlatform) {
        try {
            if (!StringUtils.isNotEmpty(version) || !StringUtils.isNotEmpty(downPlatform)) {
                log.error("judageVersion()，version:{},downPlatform:{}", version, downPlatform);
                return false;
            }
            VersionConfigModel versionConfigModel = _versionMap.get(downPlatform);

            if (versionConfigModel == null) {
                return false;
            }

            String[] v_str = version.split("\\.");// 1.2.0.0
            if (v_str.length < 3) {
                log.error("judageVersion()，version args is error. version:{}", version);
                return true;
            }

            String[] v_str_now = versionConfigModel.getVersion().split("\\.");
            int a = 0;
            if (v_str_now.length >= 4) {
                a = Integer.parseInt(v_str_now[0].trim()) * 1000000 + Integer.parseInt(v_str_now[1].trim()) * 10000
                        + Integer.parseInt(v_str_now[2].trim()) * 100 + Integer.parseInt(v_str_now[3].trim());
            }

            int b = 0;
            if (v_str.length >= 4) {
                b = Integer.parseInt(v_str[0].trim()) * 1000000 + Integer.parseInt(v_str[1].trim()) * 10000
                        + Integer.parseInt(v_str[2].trim()) * 100 + Integer.parseInt(v_str[3].trim());
            }

            if (a > b) {
                return true;
            }
        } catch (Exception ex) {
            log.error("judageVersion(),downPlatform:{},version:{}", downPlatform, version);
            log.error(ex.getMessage(), ex);
        }
        return false;
    }
}
