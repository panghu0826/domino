package com.jule.core.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.HashMap;

public class GsonUtil {
	private static Gson gson = new GsonBuilder().registerTypeAdapter(Object.class, new NaturalDeserializer()).create();

	public static String toJson(Object src) {
		return gson.toJson(src);
	}

	public static <T> T fromJson(String json, Class<T> clazz) {
		return gson.fromJson(json, clazz);
	}

	public static HashMap<String, Object> fromJson(String json, Type type) {
		return gson.fromJson(json, type);
	}

	public static Gson getGson(){
		return gson;
	}
}
