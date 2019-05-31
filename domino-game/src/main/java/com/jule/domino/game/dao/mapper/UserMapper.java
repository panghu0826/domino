package com.jule.domino.game.dao.mapper;

import com.jule.domino.base.dao.bean.User;

import java.util.List;

public interface UserMapper {
    int deleteByPrimaryKey(String id);

    int insert(User record);

    User selectByPrimaryKey(String id);

    User selectBySubChannelId(String sub_channel_id);

    List<User> selectAll();

    int updateByPrimaryKey(User record);

    List<User> selectUserByOpenId(String android_id);

    int accumulationNumberOfGames(String userId);
}