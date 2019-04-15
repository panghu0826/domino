package com.boot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * redis 服务类
 *
 * @author
 *
 * @since 2018/7/20 15:44
 *
 */
@Service
public class RedisService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Resource(name = "stringRedisTemplate")
    ValueOperations<String , String> valueOpsStr;

    @Autowired
    RedisTemplate<Object , Object> redisTemplate;

    @Resource(name = "redisTemplate")
    ValueOperations<Object , Object> valueOpsObj;

    /**
     * 指定KEY获取
     * @param key 字符串
     * @return
     */
    public String getValue(String key){
        return valueOpsStr.get(key);
    }

    /**
     * 设置String缓存
     * @param key
     * @param value
     */
    public void set(String key , String value){
        valueOpsStr.set(key, value);
    }

    /**
     * 设置String缓存
     * @param key
     * @param value
     * @param time
     */
    public void set(String key , String value, long time){
        valueOpsStr.set(key, value, time);
    }

    /**
     * 清除缓存
     * @param key
     */
    public void delByKey(String key){
        stringRedisTemplate.delete(key);
    }

    /**
     * 指定KEY获取
     * @param key
     * @return
     */
    public Object getObj(Object key){
        return valueOpsObj.get(key);
    }

    /**
     * 设置缓存
     * @param key
     * @param value
     */
    public void setObj(Object key, Object value){
        valueOpsObj.set(key, value);
    }

    /**
     * 设置缓存
     * @param key
     * @param value
     * @param time
     */
    public void setObj(Object key, Object value, long time){
        valueOpsObj.set(key, value, time);
    }

    /**
     * 设置缓存
     * @param key
     */
    public void delByKey(Object key){
        redisTemplate.delete(key);
    }

    /**
     * 键值对设值
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public <K, V> Boolean set(K key, V value) {
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            connection.set(JSON.toJSONBytes(key), JSON.toJSONBytes(value));
            return true;
        });
    }

    /**
     * 键值对设值和有效时间
     *
     * @param key   键
     * @param value 值
     * @param time  有效时间(单位：秒)
     * @return
     */
    public <K, V> Boolean setEx(K key, V value, long time) {
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            connection.setEx(JSON.toJSONBytes(key), time, JSON.toJSONBytes(value));
            return true;
        });
    }

    /**
     * 查询键值对
     *
     * @param key           键
     * @param typeReference 返回类型
     * @param <K>           键类型
     * @param <R>           返回类型
     * @return
     */
    public <K, R> R get(K key, TypeReference<R> typeReference) {
        byte[] redisValue = redisTemplate.execute((RedisCallback<byte[]>) connection -> connection.get(JSON.toJSONBytes(key)));
        if (redisValue == null){
            return null;
        }
        return JSONObject.parseObject(new String(redisValue), (Type) typeReference);
    }

    /**
     * 存储Hash结构数据(批量)
     *
     * @param outerKey 外键
     * @param map      内键-内值
     * @return
     */
    public <O, I, V> Boolean hSetMap(O outerKey, Map<I, V> map) {
        if (map == null || map.isEmpty()) return false;
        Map<byte[], byte[]> byteMap = new HashMap<>();
        map.forEach((innerKey, innerValue) -> byteMap.put(JSON.toJSONBytes(innerKey), JSON.toJSONBytes(innerValue)));
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            connection.hMSet(JSON.toJSONBytes(outerKey), byteMap);
            return true;
        });
    }

    /**
     * 存储Hash结构数据
     *
     * @param outerKey   外键
     * @param innerKey   内键
     * @param innerValue 值
     * @return
     */
    public <O, I, V> Boolean hSet(O outerKey, I innerKey, V innerValue) {
        Map<I, V> map = new HashMap<>();
        map.put(innerKey, innerValue);
        return this.hSetMap(outerKey, map);
    }

    /**
     * 获取Hash结构Map集合，内键和内值键值对封装成Map集合
     *
     * @param outerKey       外键
     * @param innerKeyType   内键类型
     * @param innerValueType 值类型
     * @return
     */
    public <O, I, V> Map<I, V> hGetMap(O outerKey, TypeReference<I> innerKeyType, TypeReference<V> innerValueType) {
        Map<byte[], byte[]> redisMap = redisTemplate.execute
                ((RedisCallback<Map<byte[], byte[]>>) connection -> connection.hGetAll(JSON.toJSONBytes(outerKey)));
        if (redisMap == null)
        {
            return null;
        }
        Map<I, V> resultMap = new HashMap<>();
        redisMap.forEach((key, value) -> resultMap.put(JSONObject.parseObject
                (new String(key), (Type) innerKeyType), JSONObject.parseObject(new String(value), (Type) innerValueType)));
        return resultMap;
    }

    /**
     * 查询Hash结构的值
     *
     * @param outerKey      外键
     * @param innerKey      内键
     * @param typeReference 值类型
     * @return
     */
    public <O, I, V> V hGet(O outerKey, I innerKey, TypeReference<V> typeReference) {
        byte[] redisResult = redisTemplate.execute((RedisCallback<byte[]>)
                connection -> connection.hGet(JSON.toJSONBytes(outerKey), JSON.toJSONBytes(innerKey)));
        if (redisResult == null) {
            return null;
        }
        return JSONObject.parseObject(new String(redisResult), (Type) typeReference);

    }

    /**
     * 删除键值对
     *
     * @param keys 键
     * @return
     */
    public <K> Long del(List<K> keys) {
        if (keys == null || keys.isEmpty()) return 0L;
        byte[][] keyBytes = new byte[keys.size()][];
        int index = 0;
        for (K key : keys) {
            keyBytes[index] = JSON.toJSONBytes(key);
            index++;
        }
        return redisTemplate.execute((RedisCallback<Long>) connection -> connection.del(keyBytes));
    }

    /**
     * 删除Hash结构内键和值
     *
     * @param outerKey  外键
     * @param innerKeys 内键
     * @return
     */
    public <O, I> Long hDel(O outerKey, List<I> innerKeys) {
        if (innerKeys == null || innerKeys.isEmpty()) return 0L;
        byte[][] innerKeyBytes = new byte[innerKeys.size()][];
        int index = 0;
        for (I key : innerKeys) {
            innerKeyBytes[index] = JSON.toJSONBytes(key);
            index++;
        }
        return redisTemplate.execute((RedisCallback<Long>) connection -> connection.hDel(JSON.toJSONBytes(outerKey), innerKeyBytes));
    }

}
