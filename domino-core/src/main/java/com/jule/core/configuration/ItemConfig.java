package com.jule.core.configuration;

import com.jule.core.utils.MD5Security;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ItemConfig {

    //1.id  2.七天(sevendays)或永久(permanent)或局数(numberofgames) 3.房卡数
    private static Map<String, Map<String, Integer>> itemConfig = new HashMap<>();

    private static Object obj;
    private static String md5val = "";

    private static Thread watcher;

    public static void init() {

        String conf = readFile();

        md5val = MD5Security.compute(conf);

        obj = load(conf);
        itemToString();
        watcher = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(600 * 1000);
                    } catch (InterruptedException e) {
                    }
                    String conf = readFile();
                    String _md5 = MD5Security.compute(conf);
                    if (_md5 != md5val) {
                        synchronized (obj) {
                            obj = load(conf);
                            md5val = _md5;
                            itemToString();
                        }
                    }
                }
            }
        }, "watch_ItemConfig");
        watcher.start();
    }

    public static String readFile() {
        String configJson = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream("./config/ItemConfig.json");
            byte[] data = new byte[in.available()];
            in.read(data);
            configJson = new String(data, "utf-8");
            in.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return configJson;
    }

    private static Object load(String configJson) {
        JSONArray array = JSONArray.fromObject(configJson);
        array.forEach(e -> {
            JSONObject jsonObject = JSONObject.fromObject(e);
            Map<String, Map<String, Integer>> map = (Map) jsonObject;
            map.forEach((k, v) -> {
                JSONObject json = JSONObject.fromObject(v);
                Map<String, Integer> panghu = (Map) json;
                itemConfig.put(k, panghu);
            });
        });
        return itemConfig;
    }

    public static int getItemParameter(int itemId, int itemTime) {
        //七天(sevendays)或永久(permanent)或局数(numberofgames)
        String key = itemTime == 1 ? "sevendays" : itemTime == 2 ? "permanent" : "numberofgames";
        return itemConfig.get(String.valueOf(itemId)).get(key);
    }

    public static void itemToString() {
        itemConfig.forEach((k, v) -> {
            v.forEach((z, x) -> System.out.println("k：" + k + " ,z：" + z + " ,x：" + x));
        });
    }
}
