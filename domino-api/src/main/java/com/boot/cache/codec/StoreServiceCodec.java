package com.boot.cache.codec;


import com.boot.cache.StoredObj;

public interface StoreServiceCodec {
    /**
     * 解码数据
     * @param bytes
     * @param c
     * @return
     */
    public <T extends StoredObj> T decode( byte[] bytes, Class<T> c );
    /**
     * 编码数据
     * @param obj
     * @return
     */
    public <T extends StoredObj> byte[] encode( T obj );
}
