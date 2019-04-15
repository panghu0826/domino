package com.jule.domino.auth.service;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.domino.auth.config.Config;
import com.jule.domino.auth.dao.DBUtil;
import com.jule.domino.base.bean.ItemConfigBean;
import com.jule.domino.base.bean.ItemType;
import com.jule.domino.base.dao.bean.Product;
import com.jule.domino.base.service.ItemServer;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 商品缓存
 */
public class ProductionService {
    private static final Logger logger = LoggerFactory.getLogger(ProductionService.class);

    private static Map<String, Map<String, JoloAuth.JoLoCommon_ProtocInfo>> cache = new ConcurrentHashMap<>();

    private static class SingletonHolder {
        protected static final ProductionService instance = new ProductionService();
    }

    public static final ProductionService getInstance() {
        return ProductionService.SingletonHolder.instance;
    }

    @Getter
    private Map<String, ItemConfigBean> headMap = new ConcurrentHashMap<>();

    private ProductionService() {
        discoverData();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> discoverData(), 5, 5, TimeUnit.MINUTES);
    }

    /**
     * 服务监听启动后调用
     */
    private void discoverData() {
        List<Product> list = DBUtil.selectAllData();
        if (list.size() != 0) {
            Map<String, Map<String, JoloAuth.JoLoCommon_ProtocInfo>> __cache = new ConcurrentHashMap<>();
            list.forEach(e -> {
                JoloAuth.JoLoCommon_ProtocInfo info = JoloAuth.JoLoCommon_ProtocInfo.newBuilder()
                        .setPid(e.getPid())
                        .setPrice(e.getPrice())
                        .setTotalReward(e.getTotal_reward())
                        .setBaseReward(e.getBase_reward())
                        .setExtraReward(e.getExtra_reward())
                        .setExtraPercent(e.getExtra_percent())
                        .setTag(e.getTag())
                        .setIdx(e.getPos())
                        .setIcoCount(e.getIco_count())
                        .setPayChannel(e.getPay_channel())
                        .setType(e.getContain_type())
                        .setItemTmpId(e.getContain_item_id())
                        .build();

                if (!__cache.containsKey(e.getPay_channel())) {
                    Map<String, JoloAuth.JoLoCommon_ProtocInfo> map = new LinkedHashMap<>();
                    __cache.put(e.getPay_channel(), map);
                }
                __cache.get(e.getPay_channel()).put(info.getPid(), info);
                logger.info("appId->" + e.getPay_channel() + " load->" + info);
            });

            Map<String, Map<String, JoloAuth.JoLoCommon_ProtocInfo>> old = cache;
            cache = __cache;
            old.clear();
        }
        List<ItemConfigBean> itemConfigBeans = ItemServer.OBJ.getAllTemplates(Config.GAME_ID);
        if (itemConfigBeans != null) {
            itemConfigBeans.forEach(item -> {
                if (item.getItemType() == ItemType.TYPE_ICO) {
                    headMap.put(item.getItemIcon(), item);
                }
            });
        }
    }

    /**
     * @param paychannel
     * @return
     */
    public Map<String, JoloAuth.JoLoCommon_ProtocInfo> getProducts(String paychannel) {
        Map<String, JoloAuth.JoLoCommon_ProtocInfo> map = cache.get(paychannel);
        if (map == null || map.size() == 0) {
            map = cache.get(Config.DEFUALT_PAY_CHANNEL);
        }
        logger.debug("channel = {},defualt = {}, result = {}", paychannel, Config.DEFUALT_PAY_CHANNEL, map);
        return map;
    }

    /**
     * @param paychannel
     * @return
     */
    public Map<String, JoloAuth.JoLoCommon_ProtocInfo> getProducts(String paychannel, String type) {
        Map<String, JoloAuth.JoLoCommon_ProtocInfo> map = cache.get(paychannel);
        if (map == null || map.size() == 0) {
            map = cache.get(Config.DEFUALT_PAY_CHANNEL);
        }
        Map<String, JoloAuth.JoLoCommon_ProtocInfo> tmp = getProductsByType(map, type);
        logger.debug("channel = {},defualt = {}, result = {}", paychannel, Config.DEFUALT_PAY_CHANNEL, tmp);
        return tmp;
    }

    private Map<String, JoloAuth.JoLoCommon_ProtocInfo> getProductsByType(Map<String, JoloAuth.JoLoCommon_ProtocInfo> map, String type) {
        Map<String, JoloAuth.JoLoCommon_ProtocInfo> tmp = new HashMap<>();
        map.forEach((k, v) -> {
            if (v.getType().equals(type)) {
                tmp.put(k, v);
            }

        });
        return tmp;
    }
}
