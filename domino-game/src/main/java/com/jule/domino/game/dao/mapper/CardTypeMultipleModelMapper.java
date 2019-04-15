package com.jule.domino.game.dao.mapper;

import com.jule.domino.game.dao.bean.CardTypeMultipleModel;
import java.util.List;

public interface CardTypeMultipleModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CardTypeMultipleModel record);

    CardTypeMultipleModel selectByPrimaryKey(Integer id);

    List<CardTypeMultipleModel> selectAll();

    int updateByPrimaryKey(CardTypeMultipleModel record);
}