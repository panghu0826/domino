package com.jule.domino.dispacher.dao.mapper;

import com.jule.domino.dispacher.dao.bean.OnlineConfigModel;
import java.util.List;

public interface OnlineConfigModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OnlineConfigModel record);

    OnlineConfigModel selectByPrimaryKey(Integer id);

    List<OnlineConfigModel> selectAll();

    int updateByPrimaryKey(OnlineConfigModel record);
}