package com.jule.domino.game.utils.runnable;

import com.jule.domino.base.dao.bean.User;
import com.jule.domino.game.dao.DBUtil;

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
