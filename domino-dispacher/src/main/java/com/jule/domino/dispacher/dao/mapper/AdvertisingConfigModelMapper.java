package com.jule.domino.dispacher.dao.mapper;

import com.jule.domino.dispacher.dao.bean.AdvertisingConfigModel;

import java.util.List;

public interface AdvertisingConfigModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(AdvertisingConfigModel record);

    AdvertisingConfigModel selectByPrimaryKey(Integer id);

    List<AdvertisingConfigModel> selectAll();

    int updateByPrimaryKey(AdvertisingConfigModel record);
}