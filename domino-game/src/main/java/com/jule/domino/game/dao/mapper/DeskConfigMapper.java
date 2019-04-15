package com.jule.domino.game.dao.mapper;

import com.jule.domino.game.dao.bean.DeskConfig;

import java.util.List;
@Deprecated
public interface DeskConfigMapper {
    int insert(DeskConfig record);

    List<DeskConfig> selectAll();
}