package com.jule.domino.dispacher.dao.mapper;

import com.jule.domino.base.dao.bean.User;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface UserMapper {
    int deleteByPrimaryKey(String id);

    int insert(User record);

    User selectByPrimaryKey(String id);

    List<User> selectAll();

    int updateByPrimaryKey(User record);

    int updateMoney(@Param("id") String uid, @Param("money") long money);

    int updateLastOffline(@Param("id") String uid,@Param("last_offline")Date date);

    List<User> selectUserByOpenId(String android_id);
}