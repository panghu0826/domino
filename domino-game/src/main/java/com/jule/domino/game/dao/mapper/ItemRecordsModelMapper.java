package com.jule.domino.game.dao.mapper;

import com.jule.domino.game.dao.bean.ItemRecordsModel;
import java.util.List;

public interface ItemRecordsModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ItemRecordsModel record);

    ItemRecordsModel selectByPrimaryKey(Integer id);

    ItemRecordsModel selectItemByItemToken(String itemToken);

    List<ItemRecordsModel> selectItemByUserId(String userId);

    List<ItemRecordsModel> selectAll();

    int updateByPrimaryKey(ItemRecordsModel record);
}