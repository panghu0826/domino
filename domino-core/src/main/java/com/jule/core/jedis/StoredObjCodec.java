package com.jule.core.jedis;

import com.jule.core.jedis.codec.StoreServiceCodec;
import com.jule.core.jedis.codec.impl.StoreServiceJsonCodec;
import com.jule.core.jedis.codec.impl.StoreServiceProtobufCodec;
import com.jule.core.jedis.codec.impl.StoreServiceProtostuffCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * redis 缓存对象编码类
 *
 * @author yanbin.liang
 *
 */
@Slf4j
public class StoredObjCodec {
    public static final StoreServiceCodec jsonCodec = new StoreServiceJsonCodec();
    public static final StoreServiceCodec protobufCodec = new StoreServiceProtobufCodec();
    public static final StoreServiceCodec protostuffCodec = new StoreServiceProtostuffCodec();
    private static ThreadLocal<StoreServiceCodec> codecThreadLocal = new ThreadLocal<>();
    public final static String SUPPORTED_CHARSET_NAME = "UTF-8";
    /**
     * 绑定codec
     * @param codec
     */
    public static void bindThreadLocalCodec(StoreServiceCodec codec){
        codecThreadLocal.set(codec);
    }

    /**
     * 释放codec
     */
    public static void releaseThreadLocalCodec(){
        codecThreadLocal.remove();
    }

    private static StoreServiceCodec getCodec(){
        StoreServiceCodec codec = codecThreadLocal.get();
        if(codec == null){
            codec = StoredObjCodec.jsonCodec;
        }
        return codec;
    }

    /**
     *
     * @param bytearray
     * @return
     */
    public static final List<String> toStr(List<byte[]> bytearray) {
        List<String> list = new ArrayList<>(bytearray.size());
        for (byte[] b : bytearray)
            if (null != b)
                list.add(toStr(b));
            else
                list.add(null);
        return list;
    }

    /**
     *
     * @param bytearray
     * @return
     */
    public static final Set<String> toStr(Set<byte[]> bytearray) {
        Set<String> list = new HashSet<>(bytearray.size());
        for (byte[] b : bytearray)
            if (null != b)
                list.add(toStr(b));
            else
                list.add(null);
        return list;
    }

    /**
     * @param bytes
     * @return new {@link String#String(byte[])} or null if bytes is null.
     */
    public static final String toStr(byte[] bytes) {
        String str = null;
        if (null != bytes) {
            try {
                str = new String(bytes, SUPPORTED_CHARSET_NAME);

            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage(),e);
            }
        }

        return str;
        // return new String(bytes, SUPPORTED_CHARSET); // Java 1.6 only
    }

    public static final byte[] encode(String value) {
        byte[] bytes = null;
        try {
            bytes = value.getBytes(SUPPORTED_CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
        }
        return bytes;
        // return value.getBytes(SUPPORTED_CHARSET);
    }

    // /**
    // * @param bytes
    // * @return
    // */
    // @Deprecated
    // public static final Integer toInt (byte[] bytes) {
    // return new Integer(toStr (bytes));
    // }

    /**
     * NOTE: Isn't this already in {@link Convert#toLong(byte[])}? This helper
     * method will convert the byte[] to a {@link Long}.
     *
     * @param bytes
     * @return
     */
    // @Deprecated
    public static final Long toLong(byte[] bytes) {
        // return new Long (toStr (bytes));
        return Convert.toLong(bytes);
    }

    public static final List<Long> toLong(List<byte[]> bytearray) {
        List<Long> list = new ArrayList<Long>(bytearray.size());
        for (byte[] b : bytearray)
            list.add(Convert.toLong(b));
        return list;
    }

    public static final List<Double> toDouble(List<byte[]> bytearray) {
        List<Double> list = new ArrayList<Double>(bytearray.size());
        for (byte[] b : bytearray)
            list.add(Convert.toDouble(b));
        return list;
    }

    /**
     * @param bs
     * @return
     */
    public static double toDouble(byte[] bs) {
        return Convert.toDouble(bs);
    }

    @SuppressWarnings("unchecked")
    public static final <T extends StoredObj> Set<T> decode(Set<byte[]> set, Class<? extends StoredObj> c) {
        Set<T> objectList = new HashSet<T>(set.size());
        for (byte[] bytes : set) {
            if (null != bytes) {
                T object = (T) decode(bytes, c);
                objectList.add(object);
            } else {
                objectList.add(null);
            }
        }
        return objectList;
    }

    @SuppressWarnings("unchecked")
    public static final <T extends StoredObj> List<T> decode(List<byte[]> list, Class<? extends StoredObj> c) {
        List<T> objectList = new ArrayList<T>(list.size());
        for (byte[] bytes : list) {
            if (null != bytes) {
                T object = (T) decode(bytes, c);
                objectList.add(object);
            } else {
                objectList.add(null);
            }
        }
        return objectList;
    }

    public static final <T extends StoredObj> T decode(byte[] bytes, Class<T> c) {
        return getCodec().decode(bytes, c);
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
    public static final <T extends StoredObj> byte[] encode(T obj) {
        return getCodec().encode(obj);
    }
}