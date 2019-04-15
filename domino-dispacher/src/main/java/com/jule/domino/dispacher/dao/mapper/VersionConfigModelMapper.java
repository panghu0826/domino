package com.jule.domino.dispacher.dao.mapper;

import com.jule.domino.dispacher.dao.bean.VersionConfigModel;
import java.util.List;

public interface VersionConfigModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(VersionConfigModel record);

    VersionConfigModel selectByPrimaryKey(Integer id);

    VersionConfigModel selectByDownPlatform(String down_platform);

    List<VersionConfigModel> selectAll();

    int updateByPrimaryKey(VersionConfigModel record);
}