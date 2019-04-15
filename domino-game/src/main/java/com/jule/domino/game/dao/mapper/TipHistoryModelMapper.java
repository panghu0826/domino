package com.jule.domino.game.dao.mapper;

import com.jule.domino.game.dao.bean.TipHistoryModel;

import java.util.List;

public interface TipHistoryModelMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TipHistoryModel record);

    TipHistoryModel selectByPrimaryKey(Long id);

    List<TipHistoryModel> selectAll();

    int updateByPrimaryKey(TipHistoryModel record);
}