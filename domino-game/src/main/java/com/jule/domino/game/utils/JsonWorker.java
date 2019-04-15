package com.jule.domino.game.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author
 * @since 2018/10/22 13:42
 */
public class JsonWorker {

    //单例对象
    public static final JsonWorker OBJ = new JsonWorker();

    //Gson对象
    private static final Gson gson = new GsonBuilder().serializeNulls().create();

    public Gson getGson() {
        return gson;
    }

}
