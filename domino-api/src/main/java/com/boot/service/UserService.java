package com.boot.service;

import com.boot.dao.UserDao;
import com.boot.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author
 * @since 2018/7/18 18:27
 */

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    public User getUser(String id){
        return userDao.findByIdEquals(id);
    }

}
