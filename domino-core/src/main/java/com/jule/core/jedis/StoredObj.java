package com.jule.core.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StoredObj implements Cacheable {
    private final static Logger logger = LoggerFactory.getLogger(StoredObj.class);
    //StoredObjRef<? extends StoredObj> ref;

    /**
     * 绑定一个StoredObjRef 到自身
     *
     * @param ref
     */
    //void bindRef(StoredObjRef<?> ref) {
    //    this.ref = ref;
    //}
    /**
     * 获取自身的key in redis
     *
     * @return key
     */
    /*String getKey() {
        return this.ref.getObjkey();
    }*/

    /**
     * 删除 该StoredObj
     *

    public boolean remove() {

        if (ref != null) {
            boolean result = ref.remove();
            ref = null;
            return result;
        }
        return false;
    }*/

    /**
     * 保存对象到缓存

    public boolean save() {
        if (ref != null) {
            return JedisPoolWrap.saveObject(JedisConfig.DB_ID,this.getKey(), this);
        }else{
            ref = StoredObjManager.makeObjRef(this,makeKey(getKeyName(),""+getKeyID()));
            if(ref!=null)
                return true;
        }
        return false;
    }*/
    /**
     * 使用指定编解码，保存对象到缓存

    public boolean save(StoreServiceCodec codec) {
        try{
            StoredObjCodec.bindThreadLocalCodec(codec);
            return save();
        }finally{
            StoredObjCodec.releaseThreadLocalCodec();
        }
    }
     */
    /**
     * 保存对象到缓存

    public boolean save(String key) {
        return (StoredObjManager.makeObjRef(this,key) !=null);
    }
     */
    /**
     * 保存对象到缓存
     *
    public boolean save(String key, StoreServiceCodec codec) {
        try{
            StoredObjCodec.bindThreadLocalCodec(codec);
            return save(key);
        }finally{
            StoredObjCodec.releaseThreadLocalCodec();
        }
    }*/
    /**
     * 保存对象到缓存

    public boolean saveInMap(String key) {
        long result = StoredObjManager.makeObjMap(this, key);
        if(result ==1 || result ==0){
            return true;
        }
        return false;
    }
     */
    /**
     * 保存对象到缓存

    public boolean saveInMap(String key, StoreServiceCodec codec) {
        try{
            StoredObjCodec.bindThreadLocalCodec(codec);
            return saveInMap(key);
        }finally{
            StoredObjCodec.releaseThreadLocalCodec();
        }
    }

    public boolean remove(String key){
        return StoredObjManager.deleteExistsObj(key);
    }
     */
    /****
     * 适用于单kv数
     * @param name
     * @param uuid
     * @return

    public static String makeKey(String name,String uuid){
        return JedisPoolWrap.STOREDOBJPROFIX + name + JedisPoolWrap.SPLIT + uuid;
    }*/

    /***
     * 适用于分组数据
     * @param name
     * @param uuid
     * @param id
     * @return

    public static String makeKey(String name,String uuid,long id){
        return JedisPoolWrap.STOREDOBJPROFIX + name + JedisPoolWrap.SPLIT + uuid + JedisPoolWrap.SPLIT+id;
    }
     */

}
