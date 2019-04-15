package com.jule.domino.base.service;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jule.domino.base.bean.Feedback;
import com.jule.domino.base.bean.ItemConfigBean;
import com.jule.domino.base.bean.UnitVO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物品操作服务
 *
 * @author
 *
 * @since 2018/7/31 11:15
 */
@Slf4j
public class ItemServer {

    /**
     * 单例服务
     */
    public static final ItemServer OBJ = new ItemServer();

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    //服务地址
    private static String POST_URL = "http://192.168.0.14:8888";
    //游戏
    private static int gameId = 1001;


    //游戏接口
    //根据物品id获取物品接口
    private static final String GET_UNIT      = "/service/item/getUnit";
    //根据物品类型获取物品接口
    private static final String GET_UNIT_TYPE = "/service/item/getUnitByType";
    //根据玩家ID获取物品接口
    private static final String GET_UNIT_USER = "/service/item/getUnitByUser";
    //判断物品是否足够
    private static final String ENOUGH_UNIT   = "/service/item/enoughUnit";
    //添加物品
    private static final String ADD_UNIT      = "/service/item/addUnit";
    //扣除物品
    private static final String MINUS_UNIT    = "/service/item/minusUnit";
    //初始化道具接口
    private static final String INIT_USER_ITEMS= "/service/item/initUserUnit";

    //cms 接口
    //获取id-name键值对
    private static final String GET_ID_NAME_MAP = "/service/cfg/getNameMap";
    //获取全部物品配置
    private static final String GET_ALL_TEMPLATE= "/service/cfg/getAllTemplates";

    //获取单个物品配置
    private static final String GET_SINGLE_TEMPLATE= "/service/cfg/getTemplateByItemId";


    /**
     * 初始化服务
     * @param url
     */
    public void init(String url, int gameId){
        this.POST_URL = url;
        this.gameId = gameId;
        log.info("道具服务初始化完成 {},gameId ={}", this.POST_URL, this.gameId);

        log.info("测试一下道具服务");
        //this.getIdNameMap(this.gameId);
    }

    /**
     * 获取物品数量信息
     * @param userId
     *                  玩家ID
     * @param gameId
     *                  游戏ID
     * @param itemId
     *                  物品ID
     * @return
     */
    public UnitVO getUnit(int gameId, String userId, int itemId){
        UnitVO unit = new UnitVO();
        try {
            //构建参数
            Map<String, String> params = new HashMap<>();
            params.put("gameId", String.valueOf(gameId));
            params.put("userId", userId);
            params.put("itemId", String.valueOf(itemId));

            //请求服务
            String ret = OkHttp3Post(GET_UNIT , params);
            if (ret == null) {
                unit.setResult(Feedback.HTTP_REQUEST_FAIL);
                return unit;
            }

            //返回值转换对象
            unit = gson.fromJson(ret, UnitVO.class);
            return unit;
        }catch (Exception e){
            log.error("请求getUnit失败,异常e={}",e);
        }
        unit.setResult(Feedback.ERROR);
        return unit;
    }

    /**
     * 获取物品数量信息
     * @param userId
     *                  玩家ID
     * @param gameId
     *                  游戏ID
     * @param itemType
     *                  物品类型
     * @return
     */
    public UnitVO getUnitByType(int gameId, String userId, int itemType){
        UnitVO unit = new UnitVO();
        try {
            //构建参数
            Map<String, String> params = new HashMap<>();
            params.put("gameId",String.valueOf(gameId));
            params.put("userId",userId);
            params.put("itemType",String.valueOf(itemType));

            //请求服务
            String ret = OkHttp3Post(GET_UNIT_TYPE , params);
            if (ret == null) {
                unit.setResult(Feedback.HTTP_REQUEST_FAIL);
                return unit;
            }

            //返回值转换对象
            unit = gson.fromJson(ret, UnitVO.class);
            return unit;
        }catch (Exception e){
            log.error("请求getUnitByType失败,异常e={}",e);
        }
        unit.setResult(Feedback.ERROR);
        return unit;

    }

    /**
     * 获取物品信息
     * @param gameId
     *                  游戏ID
     * @param userId
     *                  玩家ID
     * @return
     */
    public UnitVO getUnitByUser(int gameId, String userId){
        UnitVO unit = new UnitVO();
        try {
            //构建参数
            Map<String, String> params = new HashMap<>();
            params.put("gameId", String.valueOf(gameId));
            params.put("userId", userId);

            //调用服务
            String ret = OkHttp3Post(GET_UNIT_USER , params);
            if (ret == null) {
                unit.setResult(Feedback.HTTP_REQUEST_FAIL);
                return unit;
            }

            //返回值转换对象
            unit = gson.fromJson(ret, UnitVO.class);
            return unit;
        }catch (Exception e){
            log.error("请求getUnitByUser失败,异常e={}",e);
        }
        unit.setResult(Feedback.ERROR);
        return unit;
    }

    /**
     * 判断物品是否足够
     * @param gameId    游戏ID
     * @param userId    角色ID
     * @param itemId    物品ID
     * @param num       物品数量
     * @return
     */
    public boolean enoughUnit(int gameId, String userId, int itemId, int num){
        try {
            //构建参数
            Map<String, String> params = new HashMap<>();
            params.put("gameId", String.valueOf(gameId));
            params.put("userId", userId);
            params.put("itemId", String.valueOf(itemId));
            params.put("num",    String.valueOf(num));

            //请求服务
            String ret = OkHttp3Post(ENOUGH_UNIT , params);
            if (ret == null){
                return false;
            }

            //返回值转换对象
            if ("true".equals(ret)){
                return true;
            }
        }catch (Exception e){
            log.error("请求enoughUnit失败,异常e={}",e);
        }
        return false;
    }


    /**
     * 添加物品
     * @param gameId    游戏ID
     * @param userId    角色ID
     * @param itemId    物品ID
     * @param num       物品数量
     * @param reason    添加原因
     * @return
     */
    public UnitVO addUnit(int gameId, String userId, int itemId, int num, String reason){
        UnitVO unit = new UnitVO();
        try {
            //构建参数
            Map<String, String> params = new HashMap<>();
            params.put("gameId", String.valueOf(gameId));
            params.put("userId", userId);
            params.put("itemId", String.valueOf(itemId));
            params.put("num",    String.valueOf(num));
            params.put("reason", reason);

            //发起请求
            String ret = OkHttp3Post(ADD_UNIT , params);
            if (ret == null) {
                unit.setResult(Feedback.HTTP_REQUEST_FAIL);
                return unit;
            }

            //返回值转换对象
            unit = gson.fromJson(ret, UnitVO.class);
            return unit;
        }catch (Exception e){
            log.error("请求addUnit失败,异常e={}",e);
        }
        unit.setResult(Feedback.ERROR);
        return unit;
    }

    /**
     * 扣除物品
     * @param gameId    游戏ID
     * @param userId    角色ID
     * @param itemId    物品ID
     * @param num       物品数量
     * @param reason    扣除原因
     * @return
     */
    public UnitVO minusUnit(int gameId, String userId, int itemId, int num, String reason){
        UnitVO unit = new UnitVO();
        try {
            //构建参数
            Map<String, String> params = new HashMap<>();
            params.put("gameId", String.valueOf(gameId));
            params.put("userId", userId);
            params.put("itemId", String.valueOf(itemId));
            params.put("num",    String.valueOf(num));
            params.put("reason", reason);

            //请求服务
            String ret =  OkHttp3Post(MINUS_UNIT , params);
            if (ret == null) {
                unit.setResult(Feedback.HTTP_REQUEST_FAIL);
                return unit;
            }

            //返回值转换对象
            unit = gson.fromJson(ret, UnitVO.class);
            return unit;
        }catch (Exception e){
            log.error("请求minusUnit失败,异常e={}",e);
        }
        unit.setResult(Feedback.ERROR);
        return unit;
    }

    /**
     * 请求物品ID-Name键值对
     * @return
     */
    public Map<Integer , String> getIdNameMap(int gameId){
        Map<Integer , String> map = new HashMap<>();
        try {
            //构建参数
            Map<String, String> params = new HashMap<>();
            params.put("gameId", String.valueOf(gameId));

            //请求服务
            String ret =  OkHttp3Post(GET_ID_NAME_MAP , params);
            if (ret == null) {
                log.error("http 请求失败");
                return map;
            }

            //返回值转换对象
            map = gson.fromJson(ret, new TypeToken<Map<Integer,String>>() {}.getType());
            return map;
        }catch (Exception e){
            log.error("请求getIdNameMap失败,异常e={}",e);
        }
        return map;
    }

    /**
     * 获取全部物品配置
     * @return
     */
    public List<ItemConfigBean> getAllTemplates(int gameId){
        List<ItemConfigBean> list = new ArrayList<>();
        try {
            //构建参数
            Map<String, String> params = new HashMap<>();
            params.put("gameId", String.valueOf(gameId));

            //请求服务
            //String ret =  OkHttp3Post(GET_ALL_TEMPLATE , params);
            String ret = "[{\"id\":11,\"itemName\":\"测试荷官\",\"itemIcon\":\"heguan_z_3\",\"itemType\":8,\"timeOut\":3600000,\"details\":\"测试荷官1\",\"price\":0,\"extraPrice\":0,\"tag\":100},{\"id\":13,\"itemName\":\"限时头像\",\"itemIcon\":\"txs1_png\",\"itemType\":7,\"timeOut\":60000,\"details\":\"限时头像\",\"price\":0,\"extraPrice\":0,\"tag\":0},{\"id\":17,\"itemName\":\"hd_item_gun\",\"itemIcon\":\"hd_item_gun\",\"itemType\":12,\"timeOut\":0,\"details\":\"枪\",\"price\":400,\"extraPrice\":0,\"tag\":0},{\"id\":15,\"itemName\":\"feidan\",\"itemIcon\":\"feidan\",\"itemType\":9,\"timeOut\":86400000,\"details\":\"限时礼物\",\"price\":0,\"extraPrice\":0,\"tag\":0},{\"id\":16,\"itemName\":\"test_gift_popular\",\"itemIcon\":\"test_gift_popular\",\"itemType\":12,\"timeOut\":0,\"details\":\"水杯\",\"price\":400,\"extraPrice\":0,\"tag\":0},{\"id\":18,\"itemName\":\"hd_item_egg\",\"itemIcon\":\"hd_item_egg\",\"itemType\":12,\"timeOut\":0,\"details\":\"鸡蛋\",\"price\":400,\"extraPrice\":0,\"tag\":0},{\"id\":19,\"itemName\":\"hd_item_ass\",\"itemIcon\":\"hd_item_ass\",\"itemType\":12,\"timeOut\":0,\"details\":\"蠢驴\",\"price\":400,\"extraPrice\":0,\"tag\":1},{\"id\":20,\"itemName\":\"hd_item_bomb\",\"itemIcon\":\"hd_item_bomb\",\"itemType\":12,\"timeOut\":0,\"details\":\"炸弹\",\"price\":400,\"extraPrice\":0,\"tag\":0},{\"id\":21,\"itemName\":\"hd_item_wine\",\"itemIcon\":\"hd_item_wine\",\"itemType\":12,\"timeOut\":0,\"details\":\"酒\",\"price\":200,\"extraPrice\":0,\"tag\":0},{\"id\":22,\"itemName\":\"hd_item_clock\",\"itemIcon\":\"hd_item_clock\",\"itemType\":12,\"timeOut\":0,\"details\":\"闹钟\",\"price\":200,\"extraPrice\":0,\"tag\":2},{\"id\":23,\"itemName\":\"hd_item_thumb\",\"itemIcon\":\"hd_item_thumb\",\"itemType\":12,\"timeOut\":0,\"details\":\"大拇指\",\"price\":200,\"extraPrice\":0,\"tag\":2},{\"id\":24,\"itemName\":\"heguan_1\",\"itemIcon\":\"heguan_z_1\",\"itemType\":11,\"timeOut\":0,\"details\":\"hg1\",\"price\":400,\"extraPrice\":0,\"tag\":100},{\"id\":25,\"itemName\":\"heguan_2\",\"itemIcon\":\"heguan_z_2\",\"itemType\":11,\"timeOut\":0,\"details\":\"hg2\",\"price\":600,\"extraPrice\":0,\"tag\":100},{\"id\":26,\"itemName\":\"heguan_3\",\"itemIcon\":\"heguan_z_3\",\"itemType\":11,\"timeOut\":0,\"details\":\"hg3\",\"price\":800,\"extraPrice\":0,\"tag\":100},{\"id\":27,\"itemName\":\"heguan_4\",\"itemIcon\":\"heguan_z_4\",\"itemType\":11,\"timeOut\":0,\"details\":\"hg4\",\"price\":1000,\"extraPrice\":0,\"tag\":100},{\"id\":33,\"itemName\":\"txn2_png\",\"itemIcon\":\"txn2_png\",\"itemType\":10,\"timeOut\":0,\"details\":\"\",\"price\":0,\"extraPrice\":0,\"tag\":0},{\"id\":32,\"itemName\":\"txn1_png\",\"itemIcon\":\"txn1_png\",\"itemType\":10,\"timeOut\":0,\"details\":\"txn1_png\",\"price\":0,\"extraPrice\":0,\"tag\":0},{\"id\":34,\"itemName\":\"txn3_png\",\"itemIcon\":\"txn3_png\",\"itemType\":10,\"timeOut\":0,\"details\":\"\",\"price\":0,\"extraPrice\":0,\"tag\":0},{\"id\":35,\"itemName\":\"txn4_png\",\"itemIcon\":\"txn4_png\",\"itemType\":10,\"timeOut\":0,\"details\":\"\",\"price\":0,\"extraPrice\":0,\"tag\":0},{\"id\":36,\"itemName\":\"txv1_png\",\"itemIcon\":\"txv1_png\",\"itemType\":10,\"timeOut\":0,\"details\":\"\",\"price\":0,\"extraPrice\":0,\"tag\":0},{\"id\":37,\"itemName\":\"txv2_png\",\"itemIcon\":\"txv2_png\",\"itemType\":10,\"timeOut\":0,\"details\":\"\",\"price\":0,\"extraPrice\":0,\"tag\":0},{\"id\":38,\"itemName\":\"txv3_png\",\"itemIcon\":\"txv3_png\",\"itemType\":10,\"timeOut\":0,\"details\":\"\",\"price\":0,\"extraPrice\":0,\"tag\":0},{\"id\":39,\"itemName\":\"txv4_png\",\"itemIcon\":\"txv4_png\",\"itemType\":10,\"timeOut\":0,\"details\":\"\",\"price\":0,\"extraPrice\":0,\"tag\":0},{\"id\":40,\"itemName\":\"txv5_png\",\"itemIcon\":\"txv5_png\",\"itemType\":10,\"timeOut\":0,\"details\":\"\",\"price\":0,\"extraPrice\":0,\"tag\":0},{\"id\":41,\"itemName\":\"限时头像\",\"itemIcon\":\"txs2_png\",\"itemType\":7,\"timeOut\":60000,\"details\":\"txs2_png\",\"price\":0,\"extraPrice\":0,\"tag\":0},{\"id\":42,\"itemName\":\"hd_item_xhs\",\"itemIcon\":\"hd_item_xhs\",\"itemType\":9,\"timeOut\":60000,\"details\":\"西红柿\",\"price\":0,\"extraPrice\":0,\"tag\":0}]";
            if (ret == null) {
                log.error("http 请求失败");
                return list;
            }

            //返回值转换对象
            list = gson.fromJson(ret, new TypeToken<List<ItemConfigBean>>() {}.getType());
            return list;
        }catch (Exception e){
            log.error("请求getAllTemplates失败,异常e={}",e);
        }
        return list;
    }

    /**
     * 获取全部物品配置
     * @return
     */
    public ItemConfigBean getTemplateByItemId(int gameId,int itemId){
        ItemConfigBean list = new ItemConfigBean();
        try {
            //构建参数
            Map<String, String> params = new HashMap<>();
            params.put("gameId", String.valueOf(gameId));
            params.put("itemId", String.valueOf(itemId));

            //请求服务
            String ret =  OkHttp3Post(GET_SINGLE_TEMPLATE , params);
            if (ret == null) {
                log.error("http 请求失败");
                return list;
            }

            //返回值转换对象
            list = gson.fromJson(ret, new TypeToken<ItemConfigBean>() {}.getType());
            return list;
        }catch (Exception e){
            log.error("请求getAllTemplates失败,异常e={}",e);
        }
        return list;
    }

    /**
     * 建号初始化玩家道具
     * @param gameId
     * @param uid
     */
    public void initUserItems(int gameId, String uid){
        try {
            //构建参数
            Map<String, String> params = new HashMap<>();
            params.put("gameId", String.valueOf(gameId));
            params.put("userId", uid);

            //请求服务
            String ret =  OkHttp3Post(INIT_USER_ITEMS , params);
        }catch (Exception e){
            log.error("请求getAllTemplates失败,异常e={}",e);
        }
    }

    private String OkHttp3Post(String url, Map<String,String > params){
        Response response = null;
        try {
            OkHttpClient okHttpClient = new OkHttpClient();

            //构建参数
            FormBody.Builder builder = new FormBody.Builder();
            params.forEach((k,v)->builder.add(k,String.valueOf(v)));

            //post请求body
            FormBody body = builder.build();

            //构建请求
            Request request = new Request.Builder().
                    url(POST_URL + url).
                    post(body).
                    build();

            //发送请求
            response = okHttpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                log.error("okhttp post error=" + response.code());
                return null;
            }

            String res = response.body().string();
            log.info("okhttp ret ="+res);

            return res;
        }catch (Exception e){
            log.error("okhttp post error,",e);
        }finally {
            if (response != null){
                response.close();
            }
        }
        return null;
    }

}
