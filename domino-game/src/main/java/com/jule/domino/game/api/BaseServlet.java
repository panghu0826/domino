package com.jule.domino.game.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseServlet {

    protected final static Logger logger = LoggerFactory.getLogger(BaseServlet.class);

    /**失败*/
    protected static final int ERR = 0;
    /**成功*/
    protected static final int SUC = 1;

    /**失败*/
    protected static final String ERR_STR = "error";
    /**成功*/
    protected static final String SUC_STR = "success";


    public boolean checkPermission(String tokens, long time) {
        // TODO Auto-generated method stub
        return true;
    }

}
