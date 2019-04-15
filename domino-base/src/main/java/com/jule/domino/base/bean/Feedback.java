package com.jule.domino.base.bean;

/**
 * @author
 * @since 2018/8/1 17:18
 */
public class Feedback {


    public static int BASE = 0;

    public static final int SUCCESS = 0;

    public static final int ERROR = ++BASE;

    public static final int CONFIG_NULL = ++BASE;

    public static final int ITEM_NOT_ENOUGH = ++BASE;

    public static final int ITEM_NOT_EXIST = ++BASE;

    public static final int PARAM_NULL = ++BASE;

    public static final int HTTP_REQUEST_FAIL = ++BASE;
}
