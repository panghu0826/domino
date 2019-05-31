package com.jule.domino.game.dao.mapper;

import com.jule.domino.game.dao.bean.FriendTableModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FriendTableModelMapper {
    int deleteFriend(@Param("userId") String userId,@Param("friendUserId") String friendUserId);

    int insert(FriendTableModel record);

    FriendTableModel selectByPrimaryKey(Integer id);

    List<FriendTableModel> selectFriendByUserId(String userId);

    FriendTableModel selectByUserIdAndFriendId(@Param("userId") String userId,@Param("friendUserId") String friendUserId);

    List<FriendTableModel> selectAll();

    int updateByUserIdAndFriendId(FriendTableModel record);
}