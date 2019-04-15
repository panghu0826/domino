package com.jule.domino.room.dao.mapper;

import com.jule.domino.room.dao.bean.DeskConfig;

import java.util.List;

public interface DeskConfigMapper {
    int insert(DeskConfig record);

    List<DeskConfig> selectAll();
}