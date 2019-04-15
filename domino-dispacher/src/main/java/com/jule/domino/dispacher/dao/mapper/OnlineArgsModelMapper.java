package com.jule.domino.dispacher.dao.mapper;

import com.jule.domino.dispacher.dao.bean.OnlineArgsModel;

import java.util.List;

public interface OnlineArgsModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OnlineArgsModel record);

    OnlineArgsModel selectByPrimaryKey(Integer id);

    List<OnlineArgsModel> selectAll();

    List<String> selectByOnlineId(Integer onlineId);

    int updateByPrimaryKey(OnlineArgsModel record);

    List<Integer> selectOnlineIds();
}