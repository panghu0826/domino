package com.jule.domino.auth.utils.runnable;

import com.jule.domino.auth.dao.DBUtil;
import com.jule.domino.base.dao.bean.User;

public class UserRunnable implements Runnable {
    private User user;
    public UserRunnable(User user){
        this.user = user;
    }
    @Override
    public void run() {
        DBUtil.updateByPrimaryKey(user);
    }
}
