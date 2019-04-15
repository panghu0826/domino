package com.boot.cache.codec.impl;

import com.alibaba.fastjson.JSON;
import com.boot.cache.StoredObj;
import com.boot.cache.codec.StoreServiceCodec;
import java.io.IOException;

public class StoreServiceJsonCodec implements StoreServiceCodec {
    private static final String SUPPORTED_CHARSET_NAME = "UTF-8";

    /**
     * @param bytes
     * @return new {@link String#String(byte[])} or null if bytes is null.
     */
    public static final String toStr(byte[] bytes){
        String str = null;
        if(null != bytes){
            try {
                str = new String(bytes , SUPPORTED_CHARSET_NAME);
            } catch (Exception e) {
            }
        }
        return str;
    }
    @Override
    public <T extends StoredObj> T decode( byte[] bytes, Class<T> c) {
        T t = null;
        Exception thrown = null;
        try {
            t = (T) JSON.parseObject(toStr(bytes), c);
        } catch (ClassCastException e) {
            thrown = e;
        } finally {
            if (null != thrown)
                throw new RuntimeException("Error decoding byte[] data to instantiate java object - " + "data at key may not have been of this type or even an object", thrown);
        }
        return t;
    }

    /**
     * This helper method will serialize the given StoredObj object of type T to
     * a byte[], suitable for use as a value for a redis key, regardless of the
     * key type.
     *
     * @param <T>
     * @param obj
     * @return
     */
    @Override
    public <T extends StoredObj> byte[] encode(T obj) {
        byte[] bytes = null;
        try {
            bytes = JSON.toJSONString(obj).getBytes(SUPPORTED_CHARSET_NAME);
        } catch (IOException e) {
            throw new RuntimeException("Error serializing object" + obj + " => " + e);
        }
        return bytes;
    }
}
