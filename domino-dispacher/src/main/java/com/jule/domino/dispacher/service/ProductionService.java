package com.jule.domino.dispacher.service;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.domino.base.bean.ItemConfigBean;
import com.jule.domino.base.bean.ItemType;
import com.jule.domino.base.dao.bean.Product;
import com.jule.domino.base.service.ItemServer;
import com.jule.domino.dispacher.config.Config;
import com.jule.domino.dispacher.dao.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 商品缓存
 */
public class ProductionService {
    private static final Logger logger = LoggerFactory.getLogger(ProductionService.class);

    private static Map<Integer, ItemConfigBean> GIFT_CONFIG = new HashMap<>(); //所有礼物的配置
    private static Map<Integer, JoloAuth.JoloAuth_ItemInfo> GIFT_ITEM_MAP = new HashMap<>(); //所有礼物的配置
    private static Map<Integer, JoloAuth.JoloAuth_ItemInfo> LIMIT_GIFT_ITEM_MAP = new HashMap<>(); //所有礼物的配置
    private static Map<String, Map<Integer, Product>> cache = new ConcurrentHashMap<>();

    private static Map<Integer, ItemConfigBean> limitItemsMap = new ConcurrentHashMap<>();

    private static Map<Integer, ItemConfigBean> commonItemsMap = new ConcurrentHashMap<>();

    private static class SingletonHolder {
        protected static final ProductionService instance = new ProductionService();
    }

    public static final ProductionService getInstance() {
        return ProductionService.SingletonHolder.instance;
    }

    private ProductionService() {
        discoverData();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> discoverData(), 5, 5, TimeUnit.MINUTES);
    }

    /**
     * @param giftId
     * @return
     */
    public ItemConfigBean getGiftConfig(int giftId) {
        return GIFT_CONFIG.get(giftId);
    }

    /**
     * 服务监听启动后调用
     */
    public boolean discoverData() {
        List<ItemConfigBean> allTemplates = ItemServer.OBJ.getAllTemplates(Config.GAME_ID);
        if (allTemplates == null || allTemplates.size()<=0) {
            logger.error("discoverData(),Did not connected item server");
            throw new Error("Did not connected item server");
        }
        allTemplates.forEach(item -> {
            if (item.getItemType() == ItemType.TYPE_ICO_COMMON || item.getItemType() == ItemType.TYPE_ICO) {
                GIFT_CONFIG.put(item.getId(), item);
                commonItemsMap.put(item.getId(), item);

                GIFT_ITEM_MAP.put(item.getId(), JoloAuth.JoloAuth_ItemInfo.newBuilder().setItemPrice(item.getPrice())
                        .setItemExtraPrice(item.getExtraPrice())
                        .setItemDesc(item.getDetails()).setItemIcon(item.getItemIcon()).setItemId("" + item.getId()).setItemName(item.getItemName())
                        .setItemType(item.getTag()).setBigType(item.getItemType()).build());
            }
        });
        List<Product> list = DBUtil.selectAllData("item");
        if (list != null && list.size() != 0) {
            Map<String, Map<Integer, Product>> __cache = new ConcurrentHashMap<>();
            list.forEach(e -> {
                if (!__cache.containsKey(e.getPay_channel())) {
                    Map<Integer, Product> map = new LinkedHashMap<>();
                    __cache.put(e.getPay_channel(), map);
                }
                __cache.get(e.getPay_channel()).put(e.getContain_item_id(), e);

                GIFT_ITEM_MAP.remove(e.getContain_item_id());
                ItemConfigBean item = commonItemsMap.remove(e.getContain_item_id());
                if (item != null) {
                    limitItemsMap.put(item.getId(), item);
                    LIMIT_GIFT_ITEM_MAP.put(item.getId(), JoloAuth.JoloAuth_ItemInfo.newBuilder().setItemPrice(item.getPrice())
                            .setItemExtraPrice(item.getExtraPrice())
                            .setItemDesc(item.getDetails()).setItemIcon(item.getItemIcon()).setItemId("" + item.getId()).setItemName(item.getItemName())
                            .setItemType(item.getTag()).setPid(e.getPid()).setBigType(item.getItemType()).setUnlockPrice(e.getPrice() + "").build());
                }
                logger.info("appId->" + e.getPay_channel() + " load->" + e);
            });

            Map<String, Map<Integer, Product>> old = cache;
            cache = __cache;
            old.clear();
        }
        return true;
    }

    /**
     * 获取所有配置
     *
     * @return
     */
    public Collection<JoloAuth.JoloAuth_ItemInfo> getAllGiftConfig() {
        return GIFT_ITEM_MAP.values();
    }

    public Collection<JoloAuth.JoloAuth_ItemInfo> getLimitGiftConfig() {
        return LIMIT_GIFT_ITEM_MAP.values();
    }
}
