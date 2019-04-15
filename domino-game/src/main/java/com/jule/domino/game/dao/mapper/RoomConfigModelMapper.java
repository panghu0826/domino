package com.jule.domino.game.dao.mapper;

import com.jule.domino.game.dao.bean.RoomConfigModel;

import java.util.List;

public interface RoomConfigModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RoomConfigModel record);

    RoomConfigModel selectByPrimaryKey(Integer id);

    List<RoomConfigModel> selectAll();

    int updateByPrimaryKey(RoomConfigModel record);

    int updateOnlineRoles(String onlinerRoles);
}