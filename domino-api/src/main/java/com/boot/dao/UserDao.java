package com.boot.dao;

import com.boot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author
 * @since 2018/7/18 18:52
 */
public interface UserDao extends JpaRepository<User,String> {
    User findByIdEquals(String id);
}
