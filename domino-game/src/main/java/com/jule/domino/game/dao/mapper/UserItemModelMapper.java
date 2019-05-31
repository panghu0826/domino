package com.jule.domino.game.dao.mapper;

import com.jule.domino.game.dao.bean.UserItemModel;
import java.util.List;

public interface UserItemModelMapper {
    int deleteByItemId(String itemId);

    int insertItem(UserItemModel record);

    List<UserItemModel> selectByUserIdItem(String userId);

    List<UserItemModel> selectAll();

    int updateByItemId(UserItemModel record);
}