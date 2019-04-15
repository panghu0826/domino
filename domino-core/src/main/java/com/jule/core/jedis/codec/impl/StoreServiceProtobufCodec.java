package com.jule.core.jedis.codec.impl;

import com.jule.core.jedis.codec.StoreServiceCodec;
import com.jule.core.jedis.StoredObj;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class StoreServiceProtobufCodec implements StoreServiceCodec {

	public <T extends StoredObj> T decode(byte[] bytes, Class<T> c) {
		Exception thrown = null;
		Schema<T> schema = RuntimeSchema.<T>getSchema(c);
		try {
			T t = c.newInstance();
			ProtobufIOUtil.mergeFrom(bytes, t, schema);   
			return t;
		} catch (Exception e) {
			thrown = e;
		} finally {
			if (null != thrown)
				throw new RuntimeException("Error decoding byte[] data to instantiate java object - " + "data at key may not have been of this type or even an object", thrown);
		}
		return null;
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
	@SuppressWarnings("unchecked")
	public <T extends StoredObj> byte[] encode(T obj) {
		byte[] bytes = null;
		Class<T> tc = (Class<T>) obj.getClass();
		Schema<T> schema = RuntimeSchema.<T>getSchema(tc);
		try {
			 LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
			 bytes = ProtobufIOUtil.toByteArray(obj, schema, buffer);
		} catch (Exception e) {
			throw new RuntimeException("Error serializing object" + obj + " => " + e);
		}
		return bytes;
	}

}
